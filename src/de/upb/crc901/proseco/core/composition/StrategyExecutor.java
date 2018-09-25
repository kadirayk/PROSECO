package de.upb.crc901.proseco.core.composition;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.PrototypeConfig;

/**
 * ExecuteStrategiesCommand, searches for strategy subfolders and forking a new process for each strategy. Output and Error streams of these processes are directed to <code>systemlog/systemOut.log</code> and
 * <code>systemlog/systemErr.log</code> files respectively.
 * 
 * @author kadirayk, fmohr
 *
 */
public class StrategyExecutor {
	private final PROSECOConfig prosecoConfig;
	private final PrototypeConfig prototypeConfig;
	private final PROSECOProcessEnvironment executionEnvironment;
	private final Semaphore completionTickets = new Semaphore(0);

	public StrategyExecutor(PROSECOProcessEnvironment executionEnvironment) {
		this.prosecoConfig = executionEnvironment.getProsecoConfig();
		this.prototypeConfig = executionEnvironment.getPrototypeConfig();
		this.executionEnvironment = executionEnvironment;
	}

	public void execute(int timeoutInMS) throws IOException, InterruptedException {
		long start = System.currentTimeMillis();
		System.out.println("Executing strategies in " + executionEnvironment.getStrategyDirectory());
		final File[] strategySubFolders = executionEnvironment.getStrategyDirectory().listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isDirectory();
			}
		});
		System.out.println(" go ...");
		
		ExecutorService pool = Executors.newFixedThreadPool(strategySubFolders.length); // allow all to work in parallel
		final int timeoutInSeconds = timeoutInMS / 1000 - 20;
		long preSchedule = System.currentTimeMillis();

		for (final File strategyFolder : strategySubFolders) {
			String strategyName = strategyFolder.getName();

			String[] commandArguments = new String[5];
			commandArguments[0] = strategyFolder.getAbsolutePath() + File.separator + prototypeConfig.getSearchRunnable();
			commandArguments[1] = executionEnvironment.getProcessDirectory().getAbsolutePath();
			commandArguments[2] = executionEnvironment.getSearchInputDirectory().getAbsolutePath();
			String outputPath = executionEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + strategyName;
			commandArguments[3] = outputPath;
			commandArguments[4] = "" + timeoutInSeconds;
			final ProcessBuilder pb = new ProcessBuilder(commandArguments).redirectOutput(Redirect.PIPE).redirectError(Redirect.PIPE);
			System.out.print("Starting process for strategy " + strategyFolder + ": " + Arrays.toString(commandArguments));

			FileUtils.forceMkdir(new File(outputPath));
			File systemOut = new File(outputPath + File.separator + prosecoConfig.getSystemOutFileName());
			File systemErr = new File(outputPath + File.separator + prosecoConfig.getSystemErrFileName());
			File systemAll = new File(outputPath + File.separator + prosecoConfig.getSystemMergedOutputFileName());
			pool.submit(new ProcessRunnerAndLogWriter(strategyName, pb, systemOut, systemErr, systemAll));
		}
		long afterSchedule = System.currentTimeMillis();

		System.out.println("Started all jobs, waiting " + timeoutInMS + "ms for termination.");
		boolean success = completionTickets.tryAcquire(strategySubFolders.length, timeoutInMS, TimeUnit.MILLISECONDS);
		long timeAfter = System.currentTimeMillis();
		System.out.println("All strategies have finished: " + success);
		System.out.println("Time report:\n\tTime to start schedule: " + (preSchedule - start) + "\n\tTime to schedule: " + (afterSchedule - preSchedule) + "\n\tTime waiting for termination: " + (timeAfter - afterSchedule));
		pool.shutdownNow();
		pool.awaitTermination(1, TimeUnit.DAYS);
	}

	private class ProcessRunnerAndLogWriter implements Runnable {

		private final String strategyName;
		private final ProcessBuilder pb;
		private final File standardOutFile;
		private final File errorOutFile;
		private final File allFile;
		private Process p;

		ProcessRunnerAndLogWriter(String strategyName, ProcessBuilder pb, File standardOutFile, File errorOutFile, File allFile) throws IOException {
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
			try (final OutputStream stdOutputStream = new FileOutputStream(standardOutFile);
					final OutputStream errOutputStream = new FileOutputStream(errorOutFile);
					final OutputStream allOutputStream = new FileOutputStream(allFile)) {
				
				/* launc the actual process */
				p = pb.start();

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
				}, "strategy-" + strategyName + "-stdout-listener");

				/* launch thread that forwards the error output */
				t2 = new Thread(() -> {
					try (DataInputStream stdOutput = new DataInputStream(p.getErrorStream())) {
						int outRead = 0;
						byte[] outBytes = new byte[1024 * 10];
						while (!Thread.currentThread().isInterrupted() && (outRead = stdOutput.read(outBytes)) != -1) {
							errOutputStream.write(maskErrorStreamBytes(outRead, outBytes), 0, outRead + 8);
							allOutputStream.write(outBytes, 0, outRead);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}, "strategy-" + strategyName + "-stderr-listener");
				
				/* wait for the process to terminate. When this happens, offer one ticket for the semaphore */
				t1.run();
				t2.run();
				p.waitFor();
				StrategyExecutor.this.completionTickets.release();
			} catch (InterruptedException e) {
				System.out.println("Search execution has been interrupted. Interrupting console listeners ...");
				t1.interrupt();
				t2.interrupt();
				System.out.println("Ready");
			}
			catch (Exception e1) {
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
		 * @throws IOException
		 */
		private byte[] maskErrorStreamBytes(int outRead, byte[] outBytes) throws IOException {
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
			markedBytes[index++] = 10; // LF
			return markedBytes;
		}

	}
}
