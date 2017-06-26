package de.upb.crc901.proseco;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.upb.crc901.proseco.prototype.genderpredictor.GenderPredictor;
import de.upb.crc901.proseco.prototype.genderpredictor.GenderPredictorInstancesUtil;
import util.basic.FileUtil;
import util.ml.WekaUtil;
import weka.classifiers.Evaluation;
import weka.core.Instances;

public class Service {

	public static void main(final String[] args) throws Exception {

		// createDataArchive(1000, 0);

		// GenderPredictor.buildPredictor();

		/* predict */
		// writeData();
		// System.exit(0);
		final List<Instances> split = readData();

		final Instances data = new Instances(split.get(0));
		data.addAll(split.get(1));
		final GenderPredictor p = new GenderPredictor();
		final Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(p, data, 10, new Random(0));
		System.out.println(eval.pctCorrect());
	}

	public static void createDataArchive(final int limit, final int angleTolerance) throws Exception {
		final List<List<String>> matrix = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			matrix.addAll(FileUtil.readFileAsMatrix("faces/fold_" + i + "_data.txt", "\t"));
		}
		final Map<String, String> labels = new HashMap<>();
		final Map<String, Integer> seenEntities = new HashMap<>();
		int males = 0, females = 0;
		for (final List<String> line : matrix) {
			if (!seenEntities.containsKey(line.get(0))) {
				seenEntities.put(line.get(0), 0);
			}
			final int occurrences = seenEntities.get(line.get(0));
			try {
				if (Math.abs(Integer.parseInt(line.get(9))) > angleTolerance) {
					continue;
				}
				if (occurrences > 0) {
					continue;
				}
			} catch (final NumberFormatException e) {
				continue;
			}
			seenEntities.put(line.get(0), occurrences + 1);
			final String filename = "faces/faces/" + line.get(0) + File.separator + "coarse_tilt_aligned_face." + line.get(2) + "." + line.get(1);
			final String label = line.get(4);
			if (label.equals("m")) {
				males++;
				if (males > limit) {
					continue;
				}
			} else if (label.equals("f")) {
				females++;
				if (females > limit) {
					continue;
				}
			} else {
				continue;
			}

			labels.put(filename, label);
		}

		/* create file with labels */
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File("labels.txt")))) {
			for (final String filename : labels.keySet()) {
				bw.write(new File(filename).getName() + "," + (labels.get(filename).equals("m") ? "male" : "female") + "\n");
			}
		}

		/* zip files */
		final Collection<String> filesToZip = new ArrayList<>();
		filesToZip.addAll(labels.keySet());
		filesToZip.add("labels.txt");
		FileUtil.zipFiles(filesToZip, "data.zip");
	}

	public static void writeData() {
		final Instances data = GenderPredictorInstancesUtil.getInstances(new File("data.zip"));
		final List<Instances> split = WekaUtil.getStratifiedSplit(data, new Random(0), .6f);
		try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("split.data")))) {
			bw.writeObject(split);
			bw.close();
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<Instances> readData() {
		List<Instances> split = null;
		try (ObjectInputStream br = new ObjectInputStream(new BufferedInputStream(new FileInputStream("split.data")))) {
			split = (List<Instances>) br.readObject();
			br.close();
		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return split;
	}
}
