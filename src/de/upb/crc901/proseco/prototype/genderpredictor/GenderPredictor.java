package de.upb.crc901.proseco.prototype.genderpredictor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Crop;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Resize;
import Catalano.Imaging.Filters.Photometric.SelfQuocientImage;
import Catalano.Imaging.Texture.BinaryPattern.ImprovedLocalBinaryPattern;
import Catalano.Imaging.Tools.ImageHistogram;
import jaicore.ml.WekaUtil;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class GenderPredictor implements Classifier, Serializable {

	private static final String CLASSIFIER_OUT = "classifier.model";
	private static final int ILBP_GRANULARITY = 5;

	private Classifier c;

	public GenderPredictor() {
		super();
	}

	public GenderPredictor(final String model) {
		try (ObjectInputStream br = new ObjectInputStream(new BufferedInputStream(new FileInputStream(model)))) {
			this.c = (Classifier) br.readObject();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		// TODO correct usage message
		if (args.length != 2) {
			System.out.println("ERROR: incorrect number of arguments.");
		}

		if (args[0].equals("-t")) {
			buildPredictor(new File(args[1]));
		} else if (args[0].equals("-q")) {
			final GenderPredictor predictor = new GenderPredictor(CLASSIFIER_OUT);
			System.out.println(predictor.getPrediction(new File(args[1])));
		} else if (args[0].equals("-f")) {
			final GenderPredictor predictor = new GenderPredictor(CLASSIFIER_OUT);
			try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(args[1])))) {
				final Instances test = (Instances) ois.readObject();
				final double f = predictor.computeFValue(test);
				System.out.println("f=" + f);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private double computeFValue(final Instances testInstances) throws Exception {
		double fValue = 0;

		for (int i = 0; i < testInstances.numInstances(); i++) {
			final double pred = this.classifyInstance(testInstances.instance(i));
			if (testInstances.classAttribute().value((int) testInstances.instance(i).classValue()).equals(testInstances.classAttribute().value((int) pred))) {
				fValue += 1;
			}
		}
		fValue /= testInstances.numInstances();
		return fValue;
	}

	private static void buildPredictor(final File instancesFile) {
		final GenderPredictor p = new GenderPredictor();

		System.out.print("Read in instances and build classifier...");
		try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(instancesFile)))) {
			final Instances train = (Instances) ois.readObject();
			p.buildClassifier(train);
			System.out.println("DONE.");
		} catch (final Exception e) {
			e.printStackTrace();
		}

		/* store the trained classifier in the file */
		System.out.print("Store trained classifier...");
		try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(CLASSIFIER_OUT)))) {
			bw.writeObject(p.c);
			System.out.println("DONE.");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void buildClassifier(final Instances train) throws Exception {
		final Map<String, Integer> labelCount = WekaUtil.getNumberOfInstancesPerClass(train);
		final int maxInstances = Math.min(labelCount.get("male"), labelCount.get("female"));
		int males = 0;
		int females = 0;
		final List<Instance> list = new ArrayList<>(train);
		Collections.shuffle(list);
		for (final Instance inst : list) {
			final boolean isMale = (inst.classValue() == 0);
			if (isMale) {
				males++;
			} else {
				females++;
			}

			if (!(isMale && males <= maxInstances || !isMale && females <= maxInstances)) {
				train.remove(inst);
			}
		}

		/* create classifier object */
		/** ## PLACE COMPOSITION CODE HERE ## **/
		/* $buildclassifier$ */

		try {
			this.c.buildClassifier(train);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public double classifyInstance(final Instance arg0) throws Exception {
		return this.c.classifyInstance(arg0);
	}

	@Override
	public double[] distributionForInstance(final Instance arg0) throws Exception {
		return this.c.distributionForInstance(arg0);
	}

	@Override
	public Capabilities getCapabilities() {
		return this.c.getCapabilities();
	}

	public static Instances getEmptyDataset() {
		final Instances data = new Instances("images", getILBPAttributes(), 0);
		data.setClassIndex(data.numAttributes() - 1);
		return data;
	}

	private static ArrayList<Attribute> getILBPAttributes() {
		final int n = 511 * ILBP_GRANULARITY * ILBP_GRANULARITY; // 511 is the numbe of features in each square
		final ArrayList<Attribute> attributes = new ArrayList<>(n + 1);
		for (int i = 0; i < n; i++) {
			attributes.add(new Attribute("p" + i));
		}
		attributes.add(new Attribute("gender", Arrays.asList(new String[] { "male", "female" })));
		return attributes;
	}

	public String getPrediction(final File query) {
		try {
			final Instances dataset = getEmptyDataset();
			addInstanceFromImageWithCatalanoToDataset(query, dataset, null);
			return Math.round(this.c.classifyInstance(dataset.firstInstance())) == 1 ? "male" : "female";
		} catch (final Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void addInstanceFromImageWithCatalanoToDataset(final File imageFile, final Instances dataset, final String classValue) {
		/* create matrix representation of image */
		FastBitmap fb = new FastBitmap(imageFile.getAbsolutePath());
		final int min = Math.min(fb.getWidth(), fb.getHeight());
		new Grayscale().applyInPlace(fb);
		new SelfQuocientImage(17, 3).applyInPlace(fb);
		new Crop(0, 0, min, min).ApplyInPlace(fb);
		new Resize(250, 250).applyInPlace(fb);

		// JOptionPane.showMessageDialog(null, fb.toIcon(), "Result", JOptionPane.PLAIN_MESSAGE);

		final int[][] matrix = fb.toMatrixGrayAsInt();

		/* go through boxes and compute ilbp */
		final Instance inst = new DenseInstance(ILBP_GRANULARITY * ILBP_GRANULARITY * 511 + 1);
		inst.setDataset(dataset);
		int f = 0;

		/* compute ilbp histogram for each square */
		final int length = Math.min(fb.getWidth(), fb.getHeight());
		final int stepSize = (int) Math.floor(length * 1f / ILBP_GRANULARITY);
		System.out.println(length + "-> " + stepSize);
		for (int xSquare = 0; xSquare < ILBP_GRANULARITY; xSquare++) {
			for (int ySquare = 0; ySquare < ILBP_GRANULARITY; ySquare++) {

				/* determine the submatrix of this square */
				final int[][] exerpt = new int[stepSize][stepSize];
				for (int i = 0; i < stepSize; i++) {
					for (int j = 0; j < stepSize; j++) {
						exerpt[i][j] = matrix[xSquare * stepSize + i][ySquare * stepSize + j];
					}
				}

				/* create fast bitmap and apply ilbp */
				fb = new FastBitmap(exerpt);
				final ImprovedLocalBinaryPattern ilbp = new ImprovedLocalBinaryPattern();
				final ImageHistogram hist = ilbp.ComputeFeatures(fb);
				final int[] attributesForSquare = hist.getValues();
				for (final int val : attributesForSquare) {
					inst.setValue(f++, val);
					// JOptionPane.showMessageDialog(null, fb.toIcon(), "Result", JOptionPane.PLAIN_MESSAGE);
				}
			}
		}

		/* if there is a class assigned */
		if (classValue != null) {
			inst.setValue(f, classValue);
		}
		dataset.add(inst);
	}
}