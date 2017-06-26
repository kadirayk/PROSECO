package de.upb.crc901.proseco.prototype.genderpredictor;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Crop;
import Catalano.Imaging.Filters.Grayscale;
import Catalano.Imaging.Filters.Resize;
import Catalano.Imaging.Filters.Photometric.SelfQuocientImage;
import Catalano.Imaging.Texture.BinaryPattern.ImprovedLocalBinaryPattern;
import Catalano.Imaging.Tools.ImageHistogram;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class GenderPredictorInstancesUtil {

	/* create a naive 125x125 feature vector */
	private static final int TARGET_WIDTH = 125;
	private static final int TARGET_HEIGHT = 125;

	private static final int ILBP_GRANULARITY = 5;

	public static Instances getInstances(final File data) {
		final Path folder = Paths.get("tmp");
		unzipPhotos(data, folder);

		/* read labels */
		final Map<String, String> labels = new HashMap<>();
		try {
			final BufferedReader br = new BufferedReader(new FileReader(new File(folder.toFile().getAbsolutePath() + File.separator + "labels.txt")));
			String line;
			while ((line = br.readLine()) != null) {
				final String[] split = line.split(",");
				if (split.length == 2 && !split[1].isEmpty()) {
					labels.put(split[0], split[1]);
				}
			}
			br.close();
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		/* read images and assign labels */
		final Instances dataset = getEmptyDataset();
		final AtomicInteger i = new AtomicInteger(0);
		try (Stream<Path> paths = Files.walk(folder)) {
			paths.filter(Files::isRegularFile).forEach(f -> {
				if (f.toFile().getName().equals("labels.txt")) {
					return;
				}
				if (labels.containsKey(f.toFile().getName())) {
					addInstanceFromImageWithCatalanoToDataset(f.toFile(), dataset, labels.get(f.toFile().getName()));
					System.out.print("[add item " + (i.incrementAndGet()) + "]");
				} else {
					System.out.println("\n[ignore item " + (i.incrementAndGet()) + "]");
				}
			});
		} catch (final IOException e) {
			e.printStackTrace();
		}

		try {
			FileUtils.deleteDirectory(folder.toFile());
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return dataset;
	}

	public static void addInstanceFromImageWithCatalanoToDataset(final File imageFile, final Instances dataset, final String classValue) {

		/* create matrix representation of image */
		FastBitmap fb = new Catalano.Imaging.FastBitmap(imageFile.getAbsolutePath());
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

	private static void unzipPhotos(final File zipFile, final Path outputFolder) {
		final byte[] buffer = new byte[1024];

		try {

			// create output directory is not exists
			if (!outputFolder.toFile().exists()) {
				outputFolder.toFile().mkdir();
			}

			// get the zip file content
			final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				final String fileName = ze.getName();
				final File newFile = new File(outputFolder.toFile().getAbsolutePath() + File.separator + fileName);

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				final FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

		} catch (final IOException ex) {
			ex.printStackTrace();
		}
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

	private static ArrayList<Attribute> getPixelWiseAttributes() {
		final ArrayList<Attribute> attributes = new ArrayList<>(TARGET_WIDTH * TARGET_HEIGHT);
		for (int i = 0; i < TARGET_WIDTH * TARGET_HEIGHT; i++) {
			attributes.add(new Attribute("p" + i));
		}
		attributes.add(new Attribute("gender"));
		return attributes;
	}

	private static Instance createInstanceFromImagePixelWise(final File imageFile, final double classValue) {
		try {
			final BufferedImage image = ImageIO.read(imageFile);
			final int widthStepSize = (int) Math.ceil(image.getWidth() / TARGET_WIDTH * 1.0);
			final int heightStepSize = (int) Math.ceil(image.getHeight() / TARGET_HEIGHT * 1.0);
			int f = 0;
			final Instance inst = new DenseInstance(TARGET_WIDTH * TARGET_HEIGHT + 1);
			for (int width = 0; width < TARGET_WIDTH; width++) {
				for (int height = 0; height < TARGET_HEIGHT; height++) {
					final int realWidth = Math.min(width * widthStepSize, image.getWidth() - 1);
					final int realHeight = Math.min(height * heightStepSize, image.getHeight() - 1);
					inst.setValue(f++, image.getRGB(realWidth, realHeight));
				}
			}
			inst.setValue(f, classValue);
			return inst;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
