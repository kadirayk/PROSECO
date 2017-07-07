package de.upb.crc901.proseco.prototype.genderpredictor.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.upb.crc901.proseco.PrototypeProperties;
import de.upb.crc901.proseco.prototype.genderpredictor.GroundingRoutine;
import jaicore.basic.FileUtil;
import jaicore.basic.PerformanceLogger;

public class Benchmark extends Thread {

	private static final PrototypeProperties PROPS = new PrototypeProperties(".." + File.separator + "config" + File.separator + "benchmarkservice.conf");

	private static final File WAITING_TASK_DIR = new File(PROPS.getProperty("waiting_task_dir"));
	private static final File FINISHED_TASK_DIR = new File(PROPS.getProperty("finished_task_dir"));
	private static final File TESTBED_DIR = new File(PROPS.getProperty("testbed_dir"));

	private static final String FVALUE_FILE = PROPS.getProperty("name_fvaluefile");
	private static final String CLASSIFIER_MODEL_FILE = PROPS.getProperty("classifier_model_file");

	private static final File BENCHMARK_INSTANCES_FILE = new File(PROPS.getProperty("benchmark_instances"));
	private static final File SOURCE_INPUT_FOLDER = new File(PROPS.getProperty("prototype_source_code"));

	private static final int NUMBER_OF_THREADS = Integer.parseInt(PROPS.getProperty("number_of_threads"));
	private static final String TASK_FILE_EXT = "task";

	private volatile boolean keepRunning = true;
	private final List<String> processedFileNames;
	private final Lock fileNameListLock;
	private File taskTempFolder;

	public Benchmark(final String pName, final List<String> processedFileNames, final Lock fileNameListLock) {
		super(pName);
		this.processedFileNames = processedFileNames;
		this.fileNameListLock = fileNameListLock;
	}

	@Override
	public void run() {

		try {
			PerformanceLogger.logStart("Uptime");

			System.out.println(Thread.currentThread().getName() + ": Thread is running.");

			while (this.keepRunning) {
				final File[] fileList = WAITING_TASK_DIR.listFiles();

				if (fileList == null) {
					continue;
				}

				int numberOfProcessedTasks = 0;
				for (final File taskFile : fileList) {
					if (taskFile.isDirectory() || !FilenameUtils.isExtension(taskFile.getAbsolutePath(), TASK_FILE_EXT)) {
						continue;
					}

					this.fileNameListLock.lock();
					try {
						if(this.processedFileNames.contains(taskFile.getName())) {
							continue;
						} else {
							this.processedFileNames.add(taskFile.getName());
						}
					}finally {
						this.fileNameListLock.unlock();
					}

					final String taskOutputFolder = FileUtil.readFileAsString(taskFile.getAbsolutePath()).trim();
					if (taskOutputFolder.equals("")) {
						continue;
					}


					final File candidateFolder = new File(taskOutputFolder);

					this.taskTempFolder = new File(TESTBED_DIR.getAbsoluteFile() + "_" + candidateFolder.getName());
					FileUtils.copyDirectory(TESTBED_DIR, this.taskTempFolder);

					PerformanceLogger.logStart("PerformBenchmarkForCandidate");
					log("Start to benchmark task " + taskFile.getAbsolutePath() + " for candidate " + candidateFolder.getAbsolutePath());

					// Execute grounding => code assembly + compile + training
					log("Execute Grounding routine...");
					final String[] groundingParams = { candidateFolder.getAbsolutePath(), SOURCE_INPUT_FOLDER.getCanonicalPath(), this.taskTempFolder.getAbsolutePath() };
					GroundingRoutine.main(groundingParams);
					log("Grounding routine finished.");

					// Test trained instance against validation set
					PerformanceLogger.logStart("computeFValue");
					log("Compute f value for current testbed...", false);
					this.computeFValue();
					log("DONE");
					PerformanceLogger.logEnd("computeFValue");

					// move task specific files to task directory
					log("Benchmark Service: Move files...", false);
					for (final File testBedFile : this.taskTempFolder.listFiles()) {
						switch (testBedFile.getName()) {
						case "classifier.model":
						case "GenderPredictor.java":
						case "GenderPredictor.class":
						case "f.value":
							final File candidateFile = new File(candidateFolder.getAbsolutePath() + File.separator + testBedFile.getName());

							if (candidateFile.exists()) {
								candidateFile.delete();
							}
							FileUtils.copyFile(testBedFile, candidateFile);
							break;
						}
					}
					FileUtils.copyFile(taskFile, new File(FINISHED_TASK_DIR.getAbsolutePath() + File.separator + taskFile.getName()));
					log("DONE.");

					FileUtils.deleteDirectory(this.taskTempFolder);
					PerformanceLogger.logEnd("PerformBenchmarkForCandidate");
					log("Finished task " + taskFile.getName());
					numberOfProcessedTasks++;
				}

				if (numberOfProcessedTasks == 0) {
					// wait for new tasks and go to sleep for some millis
					Thread.sleep(1000);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			log("Woke up by interrupt.");
		} finally {
			log("Service shutting down, saving global performance log to file");
			PerformanceLogger.logEnd("Uptime");
			PerformanceLogger.saveGlobalLogToFile(new File("../InternalBenchmark.log"));
		}
	}

	@Override
	public void interrupt() {
		this.keepRunning = false;
	}

	private void computeFValue() {
		double f = 0;
		if (new File(this.taskTempFolder + File.separator + CLASSIFIER_MODEL_FILE).exists()) {
			final ProcessBuilder pb = new ProcessBuilder(this.taskTempFolder.getAbsolutePath() + File.separator + "test.bat", BENCHMARK_INSTANCES_FILE.getAbsolutePath());
			pb.redirectError(Redirect.INHERIT);

			try {
				final Process fValueProcess = pb.start();
				try (BufferedReader br = new BufferedReader(new InputStreamReader(fValueProcess.getInputStream()), 1)) {
					final String fValueString = br.readLine();
					if (fValueString != null) {

						final String[] fValueStringSplit = fValueString.split("=");
						if (fValueStringSplit.length == 2) {
							f = Double.parseDouble(fValueStringSplit[1]);
						}
					}
				}
				fValueProcess.waitFor();
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.taskTempFolder + File.separator + FVALUE_FILE))) {
			bw.write(f + "\n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static boolean lastLineBreak = true;

	public static void main(final String[] args) {
		ExecutorService threadPool = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
		List<String> taskFilenameList = new LinkedList<>();
		Lock taskFileLock = new ReentrantLock(true);

		IntStream.range(0, NUMBER_OF_THREADS).forEach(x -> threadPool.submit(new Benchmark("BenchmarkWorker#" + x, taskFilenameList, taskFileLock)));

		System.err.println("Service up and running");
		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			while ((line = br.readLine()) != null) {
				switch (line.trim()) {
				case "q":
					threadPool.shutdownNow();
					threadPool.awaitTermination(2000, TimeUnit.MILLISECONDS);
					System.exit(0);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void log(final String msg) {
		log(msg, true);
	}

	private static void log(final String msg, final boolean linebreak) {
		final String prefix = "[" + Thread.currentThread().getName() + "] Benchmark Service: ";
		String printString;
		if (lastLineBreak) {
			printString = prefix + msg;
		} else {
			printString = msg;
		}

		if (linebreak) {
			System.out.println(printString);
		} else {
			System.out.print(printString);
		}
		lastLineBreak = linebreak;
	}
}
