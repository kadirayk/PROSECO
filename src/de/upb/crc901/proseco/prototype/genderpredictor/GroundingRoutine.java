package de.upb.crc901.proseco.prototype.genderpredictor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Random;

import jaicore.basic.PerformanceLogger;
import jaicore.ml.WekaUtil;
import weka.core.Instances;

public class GroundingRoutine {

	private static final String SERVICE_SRC_FILE = "GenderPredictor.java";
	private static final String COMPILE_SCRIPT = "compile.bat";
	private static final String TRAIN_SCRIPT = "train.bat";
	private static final String BUILD_INSTANCES_SCRIPT = "buildInstances.bat";

	private static final String INSTANCES_PT_OUT = "instances.serialized";

	private static final String TRAINING_INSTANCES_FILE = "train.serialized";
	private static final String CONT_TRAINING_INSTANCES_FILE = "contTrain.serialized";
	private static final String VALIDATION_INSTANCES_FILE = "validation.serialized";
	private static final String TEST_INSTANCES_FILE = "test.serialized";

	private static final double VALIDATION_INSTANCES_FRACTION = 0.25;
	private static final double TEST_INSTANCES_FRACTION = 0.25;
	
	private final File placeHolderDir;
	private final File sourceInputDir;
	private final File sourceOutputDir;
	private final File serviceSourceFile;

	private String serviceSourceFileContent;

	public GroundingRoutine(final File placeHolderDir, final File sourceInputDir, final File sourceOutputDir) {
		this.placeHolderDir = placeHolderDir;
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
						this.serviceSourceFileContent = this.serviceSourceFileContent.replace(placeholderVar,
								placeholderValue);
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
		System.out.print("Compile solution ...");
		PerformanceLogger.logStart("CodeCompilation");
		try {
			final ProcessBuilder pb = new ProcessBuilder(this.sourceOutputDir.getAbsolutePath() + "/" + COMPILE_SCRIPT);
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.INHERIT);

			final Process compileProcess = pb.start();
			compileProcess.waitFor();
		} catch (final IOException e) {
			System.out.println(" FAIL");
			e.printStackTrace();
		} catch (final InterruptedException e) {
			System.out.println(" FAIL");
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("CodeCompilation");
		System.out.println(" DONE");
	}

	public void trainModel(final File trainingData) {
		PerformanceLogger.logStart("TrainModel");
		System.out.print("Train model ...");
		try {
			final ProcessBuilder pb = new ProcessBuilder(this.sourceOutputDir.getAbsolutePath() + "/" + TRAIN_SCRIPT,
					trainingData.getAbsolutePath());
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
		System.out.println(" DONE");
	}
	
	/**
	 *
	 * @param numberOfInstancesToBuild
	 *            zero or negative value to build all instances
	 */
	public void buildInstances(final File dataFile, final int numberOfInstancesToBuild) {
		PerformanceLogger.logStart("BuildInstances");
		try {
			final ProcessBuilder pb = new ProcessBuilder(
					this.sourceOutputDir.getAbsolutePath() + File.separator + BUILD_INSTANCES_SCRIPT,
					dataFile.getCanonicalPath());
			pb.redirectError(Redirect.INHERIT);
			pb.redirectOutput(Redirect.INHERIT);

			System.out.println("Start building instances...");
			final Process buildInstancesProcess = pb.start();
			buildInstancesProcess.waitFor();
			System.out.println("DONE");

			if (numberOfInstancesToBuild <= 0) {
				try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
						this.sourceOutputDir.getAbsolutePath() + File.separator + INSTANCES_PT_OUT))) {
					Instances allInstances = (Instances) ois.readObject();
					if (allInstances.size() == 0) {
						System.exit(0);
					}

					List<Instances> stratifiedInstances = WekaUtil.getStratifiedSplit(allInstances, new Random(123),
							(1 - VALIDATION_INSTANCES_FRACTION - TEST_INSTANCES_FRACTION),
							VALIDATION_INSTANCES_FRACTION);

					final Instances trainingInstances = stratifiedInstances.get(0);
					final Instances validationInstances = stratifiedInstances.get(1);
					final Instances testInstances = stratifiedInstances.get(2);

					final Instances contTrainingInstances = new Instances(trainingInstances);
					contTrainingInstances.addAll(validationInstances);

					System.out.print("GroundingRoutine: Serialize instances...");
					writeInstances(trainingInstances,
							new File(this.sourceOutputDir + File.separator + TRAINING_INSTANCES_FILE));
					writeInstances(validationInstances,
							new File(this.sourceOutputDir + File.separator + VALIDATION_INSTANCES_FILE));
					writeInstances(testInstances,
							new File(this.sourceOutputDir + File.separator + TEST_INSTANCES_FILE));
					writeInstances(contTrainingInstances,
							new File(this.sourceOutputDir + File.separator + CONT_TRAINING_INSTANCES_FILE));
					System.out.println("DONE.");

					/* save examples as ARFF */
					File arffTrainExport = new File(this.sourceOutputDir + File.separator + "train.arff");
					try (BufferedWriter bw = new BufferedWriter(new FileWriter(arffTrainExport))) {
						bw.write(trainingInstances.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}
					File arffExport = new File(this.sourceOutputDir + File.separator + "allInstances.arff");
					try (BufferedWriter bw = new BufferedWriter(new FileWriter(arffExport))) {
						bw.write(allInstances.toString());
					} catch (IOException e) {
						e.printStackTrace();
					}

				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		PerformanceLogger.logEnd("BuildInstances");
	}

	private static void writeInstances(final Instances instances, final File file) {
		if (file.getParentFile() != null) {
			file.getParentFile().mkdirs();
		}

		try (final ObjectOutputStream objectStream = new ObjectOutputStream(new FileOutputStream(file))) {
			objectStream.writeObject(instances);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
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
		runtimeLog(sourceOutputFolder, "assembly=" + (System.currentTimeMillis() - startTime) + "ms");

		/* Compile the assembled code */
		startTime = System.currentTimeMillis();
		gr.compile();
		runtimeLog(sourceOutputFolder, "compiling=" + (System.currentTimeMillis() - startTime) + "ms");

		startTime = System.currentTimeMillis();
		gr.trainModel(new File(".." + File.separator + "params" + File.separator + "classifierdef" + File.separator
				+ "instances.serialized"));
		runtimeLog(sourceOutputFolder, "training=" + (System.currentTimeMillis() - startTime) + "ms");

		PerformanceLogger.saveGlobalLogToFile(new File("GroundingRoutine.log"));
	}

	private static void runtimeLog(final File outputFolder, final String logMessage) {
		try (BufferedWriter bw = new BufferedWriter(
				new FileWriter(outputFolder.getAbsolutePath() + File.separator + "runtime.value", true))) {
			bw.write(logMessage + "\n");
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	private static void log(final String msg) {
		final String prefix = "Grounding Routine: ";
		System.out.println(prefix + msg);
	}

}
