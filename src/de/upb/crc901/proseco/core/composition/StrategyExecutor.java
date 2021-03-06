package de.upb.crc901.proseco.core.composition;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.GlobalConfig;

/**
 * ExecuteStrategiesCommand, searches for strategy subfolders and forking a new process for each strategy. Output and Error streams of these processes are directed to <code>systemlog/systemOut.log</code> and
 * <code>systemlog/systemErr.log</code> files respectively.
 *
 * @author kadirayk, fmohr, wever
 *
 */
public class StrategyExecutor {

	/* logging */
	private static final Logger L = LoggerFactory.getLogger(StrategyExecutor.class);

	/* Global environment properties */
	private static final GlobalConfig GLOBAL_CONFIG = ConfigCache.getOrCreate(GlobalConfig.class);

	private final PROSECOProcessEnvironment executionEnvironment;

	private final Semaphore completionTickets = new Semaphore(0);

	public StrategyExecutor(final PROSECOProcessEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	public void execute(final int timeoutInMS) throws IOException, InterruptedException {
		/* time stamp at the very beginning. */
		long start = System.currentTimeMillis();

		/* Collect all directories for strategies */
		L.debug("Executing strategies in {}", this.executionEnvironment.getStrategyDirectory());
		final File[] strategyDirectories = this.executionEnvironment.getStrategyDirectory().listFiles((f) -> f.isDirectory());

		if (strategyDirectories == null) {
			throw new RuntimeException("Could not find any search strategy!! Canceling request.");
		}

		if (L.isDebugEnabled()) {
			L.debug("Found {} strategies: {}", strategyDirectories.length, Arrays.toString(strategyDirectories));
		}

		/* Setup a thread pool for observing the strategies. */
		ExecutorService pool = Executors.newFixedThreadPool(strategyDirectories.length); // allow all to work in parallel

		int timeoutInSeconds = timeoutInMS / 1000;
		timeoutInSeconds -= 10;

		/* time stamp directly before scheduling the processes for the strategies */
		long preSchedule = System.currentTimeMillis();

		for (final File strategyDirectory : strategyDirectories) {
			String strategyName = strategyDirectory.getName();
			File outputPath = this.executionEnvironment.getSearchStrategyOutputDirectory(strategyName);

			/* Construct command to execute the runner of the strategy */
			String[] commandArguments = new String[5];
			commandArguments[0] = this.executionEnvironment.appendExecutableScriptExtension(new File(strategyDirectory, this.executionEnvironment.getPrototypeConfig().getSearchRunnable())).getAbsolutePath();
			commandArguments[1] = this.executionEnvironment.getProcessDirectory().getAbsolutePath();
			commandArguments[2] = this.executionEnvironment.getSearchInputDirectory().getAbsolutePath();
			commandArguments[3] = outputPath.getAbsolutePath();
			commandArguments[4] = "" + timeoutInSeconds;
			new File(commandArguments[0]).setExecutable(true);

			/* organize log outputs */
			ProcessBuilder pb = new ProcessBuilder(commandArguments);
			if (GLOBAL_CONFIG.debugMode() && GLOBAL_CONFIG.redirectProcessOutputs()) {
				pb = pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
			} else {
				pb = pb.redirectOutput(Redirect.PIPE).redirectError(Redirect.PIPE);
			}
			FileUtils.forceMkdir(outputPath);
			File systemOut = new File(outputPath + File.separator + this.executionEnvironment.getProsecoConfig().getSystemOutFileName());
			File systemErr = new File(outputPath + File.separator + this.executionEnvironment.getProsecoConfig().getSystemErrFileName());
			File systemAll = new File(outputPath + File.separator + this.executionEnvironment.getProsecoConfig().getSystemMergedOutputFileName());

			if (L.isDebugEnabled()) {
				L.debug("Starting process for strategy {}: {}", strategyDirectory, Arrays.toString(commandArguments));
			}
			pool.submit(new ProcessRunnerAndLogWriter(strategyName, pb, systemOut, systemErr, systemAll));
		}

		/* Timestamp after scheduling. */
		long afterSchedule = System.currentTimeMillis();

		L.debug("Started all jobs, waiting {}ms for termination.", timeoutInMS);

		boolean success = this.completionTickets.tryAcquire(strategyDirectories.length, timeoutInMS, TimeUnit.MILLISECONDS);

		long timeAfter = System.currentTimeMillis();

		L.debug("All strategies have finished: {}", success);
		L.debug("Time report\n\tTime to start schedule: {}\n\tTime to schedule: {}\n\tTime waiting for termination: {}", (preSchedule - start), (afterSchedule - preSchedule), (timeAfter - afterSchedule));

		pool.shutdownNow();
		pool.awaitTermination(1, TimeUnit.DAYS);
		L.debug("Thread pool now is shut down.");
	}

	private class ProcessRunnerAndLogWriter implements Runnable {
		private final String strategyName;
		private final ProcessBuilder pb;
		private final File standardOutFile;
		private final File errorOutFile;
		private final File allFile;

		ProcessRunnerAndLogWriter(final String strategyName, final ProcessBuilder pb, final File standardOutFile, final File errorOutFile, final File allFile) {
			this.strategyName = strategyName;
			this.pb = pb;
			this.standardOutFile = standardOutFile;
			this.errorOutFile = errorOutFile;
			this.allFile = allFile;
		}

		@Override
		public void run() {
			Thread t1 = null;
			Thread t2 = null;
			try (final OutputStream stdOutputStream = new FileOutputStream(this.standardOutFile);
					final OutputStream errOutputStream = new FileOutputStream(this.errorOutFile);
					final OutputStream allOutputStream = new FileOutputStream(this.allFile)) {

				/* launc the actual process */
				Process p = this.pb.start();

				/* launch thread that forwards the standard output */
				t1 = new Thread(() -> {
					try (DataInputStream stdOutput = new DataInputStream(p.getInputStream())) {
						int outRead = 0;
						byte[] outBytes = new byte[1024 * 10];
						while (!Thread.currentThread().isInterrupted() && (outRead = stdOutput.read(outBytes)) != -1) {
							stdOutputStream.write(outBytes, 0, outRead);
							allOutputStream.write(outBytes, 0, outRead);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}, "strategy-" + this.strategyName + "-stdout-listener");

				/* launch thread that forwards the error output */
				t2 = new Thread(() -> {
					try (DataInputStream stdOutput = new DataInputStream(p.getErrorStream())) {
						int outRead = 0;
						byte[] outBytes = new byte[1024 * 10];
						while (!Thread.currentThread().isInterrupted() && (outRead = stdOutput.read(outBytes)) != -1) {
							errOutputStream.write(this.maskErrorStreamBytes(outRead, outBytes), 0, outRead + 8);
							allOutputStream.write(this.maskErrorStreamBytes(outRead, outBytes), 0, outRead + 8);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}, "strategy-" + this.strategyName + "-stderr-listener");

				/* wait for the process to terminate. When this happens, offer one ticket for the semaphore */
				t1.start();
				t2.start();
				p.waitFor();
				StrategyExecutor.this.completionTickets.release();
			} catch (InterruptedException e) {
				L.warn("Search execution has been interrupted. Interrupting console listeners ...");
				t1.interrupt();
				t2.interrupt();
				L.warn("Ready");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		/**
		 * Marks beginning and end of error stream lines with <b> $_( </b> and <b> )_$ </b>. To mark with these characters byte values of these characters are appended to beginning and end of the read bytes from the stream <br>
		 * $ : 36 <br>
		 * _ : 95 <br>
		 * ( : 40 <br>
		 * ) = 41 <br>
		 *
		 * @param outputStream
		 * @param allOutStream
		 * @param outRead
		 * @param outBytes
		 */
		private byte[] maskErrorStreamBytes(final int outRead, final byte[] outBytes) {
			byte[] markedBytes = new byte[outBytes.length + 8];
			int index = 0;
			markedBytes[index++] = 36; // $
			markedBytes[index++] = 95; // _
			markedBytes[index++] = 40; // (
			for (int i = 0; i < outRead; i++) {
				markedBytes[index++] = outBytes[i];
			}
			markedBytes[index++] = 41; // )
			markedBytes[index++] = 95; // _
			markedBytes[index++] = 36; // $
			markedBytes[index++] = 13; // CR
			markedBytes[index] = 10; // LF
			return markedBytes;
		}

	}
}
