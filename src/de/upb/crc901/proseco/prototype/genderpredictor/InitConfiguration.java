package de.upb.crc901.proseco.prototype.genderpredictor;

import static de.upb.crc901.proseco.prototype.genderpredictor.GenderPredictorInstancesUtil.getInstances;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;

import de.upb.crc901.proseco.PrototypeProperties;
import jaicore.basic.PerformanceLogger;
import weka.core.Instances;

public class InitConfiguration {

	private static final PrototypeProperties PROPS = new PrototypeProperties("config/initconfiguration.conf");

	private static final String BASE_FOLDER = PROPS.getProperty("output_base_folder");
	private static final File TRAINING_INSTANCES_FILE = new File(BASE_FOLDER + PROPS.getProperty("training_instances_out"));
	private static final File CONT_TRAINING_INSTANCES_FILE = new File(BASE_FOLDER + PROPS.getProperty("cont_training_instances_out"));
	private static final File VALIDATION_INSTANCES_FILE = new File(BASE_FOLDER + PROPS.getProperty("validation_instances_out"));
	private static final File TEST_INSTANCES_FILE = new File(BASE_FOLDER + PROPS.getProperty("test_instances_out"));

	private static final double VALIDATION_INSTANCES_FRACTION = Double.parseDouble(PROPS.getProperty("validation_instances_fraction"));
	private static final double TEST_INSTANCES_FRACTION = Double.parseDouble(PROPS.getProperty("test_instances_fraction"));

	private static final File INPUT_DATA_ZIP = new File(PROPS.getProperty("input_data_zip"));

	public static void main(final String[] args) {
		PerformanceLogger.logStart("TotalInit");
		if (!INPUT_DATA_ZIP.exists()) {
			System.out.println("No data file available. Exit.");
			System.out.println(-1);
		}

		PerformanceLogger.logStart("PreprocessInstances");
		System.out.print("InitConfiguration: Start preprocessing instances...");
		final Instances allInstances = getInstances(INPUT_DATA_ZIP);
		Collections.shuffle(allInstances);
		System.out.println("DONE.");
		PerformanceLogger.logEnd("PreprocessInstances");

		final int validationSetSize = (int) (allInstances.size() * VALIDATION_INSTANCES_FRACTION);
		final int testSetSize = (int) (allInstances.size() * TEST_INSTANCES_FRACTION);
		final int trainSetSize = allInstances.size() - validationSetSize - testSetSize;

		final Instances trainingInstances = new Instances(allInstances, 0, trainSetSize);
		final Instances validationInstances = new Instances(allInstances, trainSetSize, validationSetSize);
		final Instances testInstances = new Instances(allInstances, trainSetSize + validationSetSize, testSetSize);

		final Instances contTrainingInstances = new Instances(trainingInstances);
		contTrainingInstances.addAll(validationInstances);

		System.out.print("InitConfiguration: Serialize instances...");
		new File(BASE_FOLDER).mkdirs();
		writeInstances(trainingInstances, TRAINING_INSTANCES_FILE);
		writeInstances(validationInstances, VALIDATION_INSTANCES_FILE);
		writeInstances(testInstances, TEST_INSTANCES_FILE);
		writeInstances(contTrainingInstances, CONT_TRAINING_INSTANCES_FILE);
		System.out.println("DONE.");

		File arffExport = new File(BASE_FOLDER + "allInstances.arff");
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(arffExport))) {
			bw.write(allInstances.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}

		PerformanceLogger.logEnd("TotalInit");
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

}
