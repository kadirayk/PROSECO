package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.PrototypeConfig;

/**
 * ExecuteStrategiesCommand, searches for strategy subfolders and forking a new
 * process for each strategy. Output and Error streams of these processes are
 * directed to <code>systemlog/systemOut.log</code> and
 * <code>systemlog/systemErr.log</code> files respectively.
 * 
 * @author kadirayk, fmohr
 *
 */
public class StrategyExecutor {
	private final PROSECOConfig prosecoConfig;
	private final PrototypeConfig prototypeConfig;
	private final PROSECOProcessEnvironment executionEnvironment;
	private final List<Process> strategyProcessList;

	public StrategyExecutor(PROSECOProcessEnvironment executionEnvironment) {
		this.prosecoConfig = executionEnvironment.getProsecoConfig();
		this.prototypeConfig = executionEnvironment.getPrototypeConfig();
		this.executionEnvironment = executionEnvironment;
		strategyProcessList = new LinkedList<>();
	}

	public void execute() throws Exception {
		final File[] strategySubFolders = executionEnvironment.getStrategyDirectory().listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isDirectory();
			}
		});

		List<String> interviewResources = getInterviewResourcesForStrategy();

		for (final File strategyFolder : strategySubFolders) {
			System.out.print("Starting process for strategy " + strategyFolder + "...");
			interviewResources.add(0, strategyFolder.getAbsolutePath() + File.separator + prototypeConfig.getSearchRunnable());
			File systemOut = new File(strategyFolder.getAbsolutePath() + File.separator + prosecoConfig.getSystemOutFileName());
			File systemErr = new File(strategyFolder.getAbsolutePath() + File.separator + prosecoConfig.getSystemErrFileName());
			File systemAll = new File(strategyFolder.getAbsolutePath() + File.separator + prosecoConfig.getSystemMergedOutputFileName());

			String[] commandArguments = interviewResources.stream().toArray(String[]::new);
			final ProcessBuilder pb = new ProcessBuilder(commandArguments).directory(executionEnvironment.getProcessDirectory()).redirectOutput(Redirect.PIPE)
					.redirectError(Redirect.PIPE);

			try {
				final Process p = pb.start();
				System.out.println("Process started, initializing stream output ...");
				InputStream inputStream = p.getInputStream();
				InputStream errorStream = p.getErrorStream();
				streamToFile(inputStream, errorStream, systemOut, systemErr, systemAll);
				this.strategyProcessList.add(p);
				System.out.println("DONE.");
			} catch (final IOException e) {
				System.err.println("Could not create process for strategy " + strategyFolder.getName());
				e.printStackTrace();
			}
		}
	}

	private void streamToFile(InputStream inputStream, InputStream errorStream, File outFile, File errFile,
			File allFile) {

		ExecutorService executor = Executors.newFixedThreadPool(2);

		Runnable outputWorker = new StreamToFile(inputStream, outFile, allFile, false);
		executor.execute(outputWorker);

		Runnable errWorker = new StreamToFile(errorStream, errFile, allFile, true);
		executor.execute(errWorker);

		executor.shutdown();
		while (!executor.isTerminated()) {

		}

	}

	public static class StreamToFile implements Runnable {

		private InputStream stream;
		private File file;
		private File allFile;
		private boolean isMarked;

		StreamToFile(InputStream stream, File file, File allFile, boolean isMarked) {
			this.stream = stream;
			this.file = file;
			this.allFile = allFile;
			this.isMarked = isMarked;
		}

		@Override
		public void run() {
			OutputStream outputStream = null;
			OutputStream allOutStream = null;
			try {
				outputStream = new FileOutputStream(file, true);
				allOutStream = new FileOutputStream(allFile, true);
				int outRead = 0;
				byte[] outBytes = new byte[1024];
				while ((outRead = stream.read(outBytes)) != -1) {
					if (isMarked) {
						writeErrorStream(outputStream, allOutStream, outRead, outBytes);
					} else {
						outputStream.write(outBytes, 0, outRead);
						allOutStream.write(outBytes, 0, outRead);
					}

				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
				if (allOutStream != null) {
					try {
						allOutStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}
		}

		/**
		 * Marks beginning and end of error stream lines with <b> $_( </b> and
		 * <b> )_$ </b>. To mark with these characters byte values of these
		 * characters are appended to beginning and end of the read bytes from
		 * the stream <br>
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
		private void writeErrorStream(OutputStream outputStream, OutputStream allOutStream, int outRead,
				byte[] outBytes) throws IOException {
			byte[] markedBytes = new byte[1024];
			markedBytes[0] = 36; // $
			markedBytes[1] = 95; // _
			markedBytes[2] = 40; // (
			for (int i = 3; i < outRead + 3; i++) {
				markedBytes[i] = outBytes[i - 3];
			}
			outRead++;
			markedBytes[outRead++] = 41; // )
			markedBytes[outRead++] = 95; // _
			markedBytes[outRead++] = 36; // $
			markedBytes[outRead++] = 13; // CR
			markedBytes[outRead++] = 10; // LF
			outputStream.write(markedBytes, 0, outRead);
			allOutStream.write(markedBytes, 0, outRead);
		}

	}

	public List<Process> getStrategyProcessList() {
		return strategyProcessList;
	}

	private List<String> getInterviewResourcesForStrategy() {
		List<String> commandArgumentList = new ArrayList<>();

		final File[] interviewResources = executionEnvironment.getInterviewResourcesDirectory()
				.listFiles(new FileFilter() {
					@Override
					public boolean accept(final File file) {
						return file.isFile();
					}
				});

		for (File resource : interviewResources) {
			commandArgumentList.add(resource.getAbsolutePath());
		}
		return commandArgumentList;
	}

}
