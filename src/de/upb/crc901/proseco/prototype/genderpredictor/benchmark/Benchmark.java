package de.upb.crc901.proseco.prototype.genderpredictor.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.upb.crc901.proseco.PrototypeProperties;
import de.upb.crc901.proseco.prototype.genderpredictor.GroundingRoutine;
import util.basic.FileUtil;
import util.basic.PerformanceLogger;

public class Benchmark extends Thread {

	private static final PrototypeProperties PROPS = new PrototypeProperties(".." + File.separator + "config" + File.separator + "benchmarkservice.conf");

	private static final File WAITING_TASK_DIR = new File(PROPS.getProperty("waiting_task_dir"));
	private static final File FINISHED_TASK_DIR = new File(PROPS.getProperty("finished_task_dir"));
	private static final File TESTBED_DIR = new File(PROPS.getProperty("testbed_dir"));

	private static final File FVALUE_FILE = new File(TESTBED_DIR.getAbsolutePath() + File.separator + PROPS.getProperty("name_fvaluefile"));
	private static final File CLASSIFIER_MODEL_FILE = new File(TESTBED_DIR.getAbsolutePath() + File.separator + PROPS.getProperty("classifier_model_file"));

	private static final File BENCHMARK_INSTANCES_FILE = new File(PROPS.getProperty("benchmark_instances"));
	private static final File SOURCE_INPUT_FOLDER = new File(PROPS.getProperty("prototype_source_code"));

	private static final String TASK_FILE_EXT = "task";

	private volatile boolean keepRunning = true;

	@Override
	public void run() {
		Thread.currentThread().setName("BenchmarkService Thread");

		try {
			PerformanceLogger.logStart("Uptime");
			System.err.println("Service up and running");

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

					final String taskOutputFolder = FileUtil.readFileAsString(taskFile.getAbsolutePath()).trim();
					if (taskOutputFolder.equals("")) {
						continue;
					}

					final File candidateFolder = new File(taskOutputFolder);
					PerformanceLogger.logStart("PerformBenchmarkForCandidate");
					log("Start to benchmark task " + taskFile.getAbsolutePath() + " for candidate " + candidateFolder.getAbsolutePath());

					// Execute grounding => code assembly + compile + training
					log("Execute Grounding routine...");
					final String[] groundingParams = { candidateFolder.getAbsolutePath(), SOURCE_INPUT_FOLDER.getCanonicalPath(), TESTBED_DIR.getAbsolutePath() };
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
					for (final File testBedFile : TESTBED_DIR.listFiles()) {
						switch (testBedFile.getName()) {
						case "classifier.model":
						case "GenderPredictor.java":
						case "GenderPredictor.class":
						case "f.value":
							final File candidateFile = new File(candidateFolder.getAbsolutePath() + File.separator + testBedFile.getName());

							if (candidateFile.exists()) {
								candidateFile.delete();
							}
							FileUtils.moveFile(testBedFile, candidateFile);
							break;
						}
					}
					FileUtils.moveFile(taskFile, new File(FINISHED_TASK_DIR.getAbsolutePath() + File.separator + taskFile.getName()));
					log("DONE.");

					PerformanceLogger.logEnd("PerformBenchmarkForCandidate");
					log("Finished task " + taskFile.getName());
					numberOfProcessedTasks++;
				}

				if (numberOfProcessedTasks == 0) {
					// wait for new tasks and go to sleep for some millis
					log("No new task...so take a rest and wait for a second.");
					Thread.sleep(1000);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		} finally {
			log("Service shutting down, saving global performance log to file");
			PerformanceLogger.logEnd("Uptime");
			PerformanceLogger.saveGlobalLogToFile(new File("../InternalBenchmark.log"));
		}
	}

	private void computeFValue() {
		double f = 0;
		if (CLASSIFIER_MODEL_FILE.exists()) {
			final ProcessBuilder pb = new ProcessBuilder(TESTBED_DIR.getAbsolutePath() + File.separator + "test.bat", BENCHMARK_INSTANCES_FILE.getAbsolutePath());
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

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(FVALUE_FILE))) {
			bw.write(f + "\n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		final Benchmark b = new Benchmark();
		b.start();

		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			while ((line = br.readLine()) != null) {
				switch (line.trim()) {
				case "q":
					b.keepRunning = false;
					b.join();
					System.exit(0);
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static boolean lastLineBreak = true;

	private static void log(final String msg) {
		log(msg, true);
	}

	private static void log(final String msg, final boolean linebreak) {
		final String prefix = "Benchmark Service: ";
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
