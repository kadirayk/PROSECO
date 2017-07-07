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

	public static void main(final String[] args) {

		if (args.length != 3) {
			// TODO: Correct usage message
			log("Correct usage: ");
			System.exit(-1);
		}

		PerformanceLogger.logStart("TotalRuntime");

		final File placeholderFolder = new File(args[0]);
		final File sourceInputFolder = new File(args[1]);
		final File sourceOutputFolder = new File(args[2]);

		log("Read service source file");
		final File serviceSourceFile = new File(sourceInputFolder.getAbsolutePath() + "/" + SERVICE_SRC_FILE);
		String serviceSourceFileContent = "";
		try (BufferedReader br = new BufferedReader(new FileReader(serviceSourceFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				serviceSourceFileContent += line + "\n";
			}
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		if (!placeholderFolder.exists() || !placeholderFolder.isDirectory()) {
			log("ERROR placeholder folder does not exist or is no directory");
			System.exit(-1);
		}

		if (!sourceOutputFolder.exists() || !sourceOutputFolder.isDirectory()) {
			log("ERROR source folder does not exist or is no directory");
			System.exit(-1);
		}

		log("Start code assembly");
		PerformanceLogger.logStart("CodeAssembly");
		for (final File placeholder : placeholderFolder.listFiles()) {
			if (placeholder.isFile()) {
				final String placeholderVar = "/* $" + placeholder.getName() + "$ */";
				if (serviceSourceFileContent.contains(placeholderVar)) {
					try (final BufferedReader br = new BufferedReader(new FileReader(placeholder))) {
						String placeholderValue = "";
						String line;
						while ((line = br.readLine()) != null) {
							placeholderValue += line + "\n";
						}
						serviceSourceFileContent = serviceSourceFileContent.replace(placeholderVar, placeholderValue);
					} catch (final FileNotFoundException e) {
						e.printStackTrace();
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		log("Finished code assembly");

		final File sourceOutputFile = new File(sourceOutputFolder.getAbsolutePath() + "/" + SERVICE_SRC_FILE);
		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(sourceOutputFile))) {
			bw.write(serviceSourceFileContent);
			bw.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("CodeAssembly");

		PerformanceLogger.logStart("CodeCompilation");
		try {
			final ProcessBuilder pb = new ProcessBuilder(sourceOutputFolder.getAbsolutePath() + "/" + COMPILE_SCRIPT);
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

		PerformanceLogger.logStart("TrainModel");
		try {
			final ProcessBuilder pb = new ProcessBuilder(sourceOutputFolder.getAbsolutePath() + "/" + TRAIN_SCRIPT);
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

		PerformanceLogger.saveGlobalLogToFile(new File("GroundingRoutine.log"));
	}

	private static void log(final String msg) {
		final String prefix = "Grounding Routine: ";
		System.out.println(prefix + msg);
	}

}
