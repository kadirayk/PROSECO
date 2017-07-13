package de.upb.crc901.proseco.prototype.genderpredictor.benchmark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import de.upb.crc901.proseco.PrototypeProperties;
import de.upb.crc901.proseco.prototype.genderpredictor.GroundingRoutine;
import jaicore.basic.PerformanceLogger;

public class ClassifierBenchmarkRunner extends AbstractBenchmarkRunner {
	private static final PrototypeProperties PROPS = new PrototypeProperties(".." + File.separator + "config" + File.separator + "benchmarkservice.conf");
	private static final String CLASSIFIER_MODEL_FILE = PROPS.getProperty("classifier_model_file");

	private static final String FVALUE_FILE = PROPS.getProperty("name_fvaluefile");
	private static final File BENCHMARK_INSTANCES_FILE = new File(PROPS.getProperty("benchmark_instances"));

	public ClassifierBenchmarkRunner(final BenchmarkTask pTask, final GroundingRoutine pGroundingRoutine, final File taskTempFolder  ) {
		super(pTask, pGroundingRoutine, taskTempFolder);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getTaskTempFolder().getAbsolutePath() + File.separator + "runtime.value"))) {
			bw.write(this.getTask().getCandidateFolder().getName()+"\n");
		} catch(IOException ioE) {
			ioE.printStackTrace();
		}
	}

	@Override
	public void run() {
		this.getGroundingRoutine().codeAssembly();

		this.getGroundingRoutine().compile();

		this.getGroundingRoutine().trainModel();

		// Test trained instance against validation set
		PerformanceLogger.logStart("computeFValue");
		log("Compute f value for current testbed...", false);
		long startTime = System.currentTimeMillis();
		this.computeFValue();
		log("DONE");
		PerformanceLogger.logEnd("computeFValue");

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getTaskTempFolder().getAbsolutePath() + File.separator + "runtime.value",true))) {
			bw.write("fValueComputation="+(System.currentTimeMillis()-startTime)+"ms\n");
		} catch(IOException ioE) {
			ioE.printStackTrace();
		}
	}

	private void computeFValue() {
		double f = 0;
		if (new File(this.getTaskTempFolder().getAbsolutePath() + File.separator + CLASSIFIER_MODEL_FILE).exists()) {
			final ProcessBuilder pb = new ProcessBuilder(this.getTaskTempFolder().getAbsolutePath() + File.separator + "test.bat", BENCHMARK_INSTANCES_FILE.getAbsolutePath());
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

		try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.getTaskTempFolder() + File.separator + FVALUE_FILE))) {
			bw.write(f + "\n");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static void log(final String msg) {
		log(msg, true);
	}

	private static boolean lastLineBreak = true;
	private static void log(final String msg, final boolean linebreak) {
		final String prefix = "[" + Thread.currentThread().getName() + "] BenchmarkService>ClassifierBenchmarkRunner: ";
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
