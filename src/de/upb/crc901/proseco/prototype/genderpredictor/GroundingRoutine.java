package de.upb.crc901.proseco.prototype.genderpredictor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import jaicore.basic.PerformanceLogger;

public class GroundingRoutine {

	private static final String SERVICE_SRC_FILE = "GenderPredictor.java";
	private static final String COMPILE_SCRIPT = "compile.bat";
	private static final String TRAIN_SCRIPT = "train.bat";

	private final File placeHolderDir;
	private final File sourceInputDir;
	private final File sourceOutputDir;
	private final File serviceSourceFile;

	private String serviceSourceFileContent;

	public GroundingRoutine(final File placeHolderDir, final File sourceInputDir, final File sourceOutputDir) {
		this.placeHolderDir= placeHolderDir;
		this.sourceInputDir = sourceInputDir;
		this.sourceOutputDir = sourceOutputDir;
		this.serviceSourceFile = new File(this.sourceInputDir.getAbsolutePath() + "/" + SERVICE_SRC_FILE);

		log("Read service source file");
		this.serviceSourceFileContent = "";
		try (BufferedReader br = new BufferedReader(new FileReader(this.serviceSourceFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				this.serviceSourceFileContent += line + "\n";
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		if (!placeHolderDir.exists() || !placeHolderDir.isDirectory()) {
			log("ERROR placeholder folder does not exist or is no directory");
			System.exit(-1);
		}

		if (!sourceOutputDir.exists() || !sourceOutputDir.isDirectory()) {
			log("ERROR source folder does not exist or is no directory");
			System.exit(-1);
		}
	}

	public void codeAssembly() {
		log("Start code assembly");
		PerformanceLogger.logStart("CodeAssembly");
		for (final File placeholder : this.placeHolderDir.listFiles()) {
			if (placeholder.isFile()) {
				final String placeholderVar = "/* $" + placeholder.getName() + "$ */";
				if (this.serviceSourceFileContent.contains(placeholderVar)) {
					try (final BufferedReader br = new BufferedReader(new FileReader(placeholder))) {
						String placeholderValue = "";
						String line;
						while ((line = br.readLine()) != null) {
							placeholderValue += line + "\n";
						}
						this.serviceSourceFileContent = this.serviceSourceFileContent.replace(placeholderVar, placeholderValue);
					} catch (final FileNotFoundException e) {
						e.printStackTrace();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		log("Finished code assembly");

		final File sourceOutputFile = new File(this.sourceOutputDir.getAbsolutePath() + "/" + SERVICE_SRC_FILE);
		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(sourceOutputFile))) {
			bw.write(this.serviceSourceFileContent);
			bw.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("CodeAssembly");
	}

	public void compile() {
		PerformanceLogger.logStart("CodeCompilation");
		try {
			final ProcessBuilder pb = new ProcessBuilder(this.sourceOutputDir.getAbsolutePath() + "/" + COMPILE_SCRIPT);
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.INHERIT);

			final Process compileProcess = pb.start();
			compileProcess.waitFor();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("CodeCompilation");
	}

	public void trainModel() {
		PerformanceLogger.logStart("TrainModel");
		try {
			final ProcessBuilder pb = new ProcessBuilder(this.sourceOutputDir.getAbsolutePath() + "/" + TRAIN_SCRIPT);
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.INHERIT);

			final Process trainProcess = pb.start();

			trainProcess.waitFor();

		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("TrainModel");
	}

	public static void main(final String[] args) {
		long startTime;
		if (args.length != 3) {
			// TODO: Correct usage message
			log("Correct usage: ");
			System.exit(-1);
		}

		PerformanceLogger.logStart("TotalRuntime");

		final File placeholderFolder = new File(args[0]);
		final File sourceInputFolder = new File(args[1]);
		final File sourceOutputFolder = new File(args[2]);

		GroundingRoutine gr = new GroundingRoutine(placeholderFolder, sourceInputFolder, sourceOutputFolder);

		/* Assemble the code by substituting the placeholders */
		startTime = System.currentTimeMillis();
		gr.codeAssembly();
		runtimeLog(sourceOutputFolder, "assembly="+(System.currentTimeMillis()-startTime)+"ms");

		/* Compile the assembled code */
		startTime = System.currentTimeMillis();
		gr.compile();
		runtimeLog(sourceOutputFolder, "compiling="+(System.currentTimeMillis()-startTime)+"ms");

		startTime = System.currentTimeMillis();
		gr.trainModel();
		runtimeLog(sourceOutputFolder, "training="+(System.currentTimeMillis()-startTime)+"ms");

		PerformanceLogger.saveGlobalLogToFile(new File("GroundingRoutine.log"));
	}

	private static void runtimeLog(final File outputFolder, final String logMessage) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFolder.getAbsolutePath() + File.separator + "runtime.value", true))) {
			bw.write(logMessage+"\n");
		} catch(IOException ioE) {
			ioE.printStackTrace();
		}
	}

	private static void log(final String msg) {
		final String prefix = "Grounding Routine: ";
		System.out.println(prefix + msg);
	}

}
