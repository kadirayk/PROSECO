
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Texture.BinaryPattern.IBinaryPattern;
import Catalano.Imaging.Texture.BinaryPattern.LocalBinaryPattern;
import Catalano.Imaging.Tools.ImageHistogram;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Source code template to be used as a image classifier. It defines an API for the execution of the
 * prototype, leaving vacancies for the ML pipeline building blocks.
 *
 * @author mwever
 */
public class ImageClassifier extends AbstractClassifierPrototype implements Classifier, Serializable {

  /**
   * Auto generated serial version UID.
   */
  private static final long serialVersionUID = 6837682242474705246L;
  /**
   * Name of the file, where to serialize the learned model.
   */
  private static final String CLASSIFIER_OUT = "classifier.model";
  /**
   * Name of the file, where to serialize the preprocessed (to instances) raw data.
   */
  private static final String INSTANCES_OUT = "instances.serialized";

  private static final int ILBP_GRANULARITY = 5;

  /**
   * Object variable for the feature extractor (to be defined).
   */
  private static IBinaryPattern bp = null;

  /**
   * Object variable for the classifier (to be defined).
   */
  private Classifier c;

  /**
   * Object variable for the Instances meta data
   */
  private Instances metadata;

  public ImageClassifier() {
    super();
  }

  public ImageClassifier(final String model) {
    try (ObjectInputStream br = new ObjectInputStream(new BufferedInputStream(new FileInputStream(model)))) {
      this.c = (Classifier) br.readObject();
      this.metadata = (Instances) br.readObject();
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void buildClassifier(final Instances train) throws Exception {
    // create classifier object
    /* $classifierdef$ */
    try {
      this.c.buildClassifier(train);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Draws numberOfInstances many files from the zip file. 0 means all files.
   *
   * @param data
   * @param numberOfInstances
   */
  public static void buildInstances(final File data, int numberOfInstances) {
    final Path folder = Paths.get("tmp");
    unzipDataFile(data, folder);

    /* read labels */
    final Map<String, String> labels = getLabelMap(folder);

    File[] fileArray = folder.toFile().listFiles(new FileFilter() {
      @Override
      public boolean accept(final File pathname) {
        if (!FilenameUtils.isExtension(pathname.getName(), "jpg")) {
          return false;
        }

        String label = labels.get(pathname.getName());
        if (label == null || label.equals("")) {
          return false;
        }
        return true;
      }
    });

    List<String> classes = new LinkedList<>(new HashSet<>(labels.values()));
    /* read images and assign labels */
    final Instances dataset = getEmptyDataset(classes, getILBPAttributes(classes));

    if (numberOfInstances <= 0) {
      numberOfInstances = fileArray.length;
    }

    int numberOfInstancesToDraw = Math.min(fileArray.length - numberOfInstances, numberOfInstances);
    Set<File> sampledFiles = new HashSet<>();
    Random r = new Random();

    while (sampledFiles.size() < numberOfInstancesToDraw) {
      int indexToAdd = r.nextInt(fileArray.length);
      sampledFiles.add(fileArray[indexToAdd]);
    }

    boolean addSampledFiles = numberOfInstancesToDraw == numberOfInstances;

    final AtomicInteger i = new AtomicInteger(0);
    Arrays.stream(fileArray).parallel().forEach(f -> {
      if ((addSampledFiles && sampledFiles.contains(f)) || (!addSampledFiles && !sampledFiles.contains(f))) {
        processDataAndAddToDataset(f, dataset, labels.get(f.getName()));
        System.out.print("[add item " + (i.incrementAndGet()) + "]");
      }
    });

    try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(INSTANCES_OUT)))) {
      FileUtils.deleteDirectory(folder.toFile());
      bw.writeObject(dataset);
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Computes the accuracy of the classifier with respect to the given test instances.
   *
   * @param testInstances
   *          Instances to make predictions for with the built classifier.
   * @return Returns the accuracy
   * @throws Exception
   */
  private double computeAccuracy(final Instances testInstances) throws Exception {
    int correctPredictions = 0;

    for (int i = 0; i < testInstances.numInstances(); i++) {
      double pred = this.classifyInstance(testInstances.instance(i));
      if (testInstances.classAttribute().value((int) testInstances.instance(i).classValue()).equals(testInstances.classAttribute().value((int) pred))) {
        correctPredictions++;
      }
    }
    double accuracy = correctPredictions * 1f / testInstances.numInstances();
    return accuracy;
  }

  private static void buildPredictor(final File instancesFile) {
    final ImageClassifier classifier = new ImageClassifier();

    Instances metadata = null;
    log("Read in instances and build classifier...");
    try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(instancesFile)))) {
      final Instances train = (Instances) ois.readObject();
      classifier.buildClassifier(train);
      metadata = new Instances(train, 0);
    } catch (final Exception e) {
      e.printStackTrace();
    }

    /* store the trained classifier in the file */
    log("Store trained classifier...");
    try (ObjectOutputStream bw = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(CLASSIFIER_OUT)))) {
      bw.writeObject(classifier.c);
      bw.writeObject(metadata);
      System.out.println("DONE.");
    } catch (final IOException e) {
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

  private static ArrayList<Attribute> getILBPAttributes(final List<String> classes) {

    /* compute number of features */
    int numberOfFeatures;
    if (bp instanceof LocalBinaryPattern || bp instanceof Catalano.Imaging.Texture.BinaryPattern.GradientLocalBinaryPattern
        || bp instanceof Catalano.Imaging.Texture.BinaryPattern.LocalGradientCoding || bp instanceof Catalano.Imaging.Texture.BinaryPattern.MultiblockLocalBinaryPattern) {
      numberOfFeatures = 256;
    } else if (bp instanceof Catalano.Imaging.Texture.BinaryPattern.CenterSymmetricLocalBinaryPattern) {
      numberOfFeatures = 16;
    } else {
      numberOfFeatures = 511;
    }
    
    final int n = numberOfFeatures * ILBP_GRANULARITY * ILBP_GRANULARITY; // 511 is the number of
                                                                          // features in each square
    final ArrayList<Attribute> attributes = new ArrayList<>(n + 1);
    for (int i = 0; i < n; i++) {
      attributes.add(new Attribute("p" + i));
    }
    attributes.add(new Attribute("class", classes));
    return attributes;
  }

  public String getPrediction(final File query) {
    try {
      List<String> classes = new LinkedList<>();

      final Instances dataset = getEmptyDataset(classes, getILBPAttributes(classes));
      processDataAndAddToDataset(query, dataset, null);
      return this.metadata.classAttribute().value((int) Math.round(this.c.classifyInstance(dataset.firstInstance())));
    } catch (final Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static Instance applyBP(final FastBitmap fb, final Instances dataset, final String classValue) {
    /* go through boxes and compute ilbp */

    final int[][] matrix = fb.toMatrixGrayAsInt();
    List<Integer> attributeVals = new ArrayList<>();

    /* compute ilbp histogram for each square */
    final int length = Math.min(fb.getWidth(), fb.getHeight());
    final int stepSize = (int) Math.floor(length * 1f / ILBP_GRANULARITY);
    for (int xSquare = 0; xSquare < ILBP_GRANULARITY; xSquare++) {
      for (int ySquare = 0; ySquare < ILBP_GRANULARITY; ySquare++) {

        /* determine the submatrix of this square */
        final int[][] excerpt = new int[stepSize][stepSize];
        for (int i = 0; i < stepSize; i++) {
          for (int j = 0; j < stepSize; j++) {
            excerpt[i][j] = matrix[xSquare * stepSize + i][ySquare * stepSize + j];
          }
        }

        /* create fast bitmap and apply ilbp */
        FastBitmap fb2 = new FastBitmap(excerpt);
        final ImageHistogram hist = bp.ComputeFeatures(fb2);
        final int[] attributesForSquare = hist.getValues();
        for (final int val : attributesForSquare) {
          attributeVals.add(val);
          // JOptionPane.showMessageDialog(null, fb.toIcon(), "Result", JOptionPane.PLAIN_MESSAGE);
        }
      }
    }

    /* now create instance object */
    final Instance inst = new DenseInstance(attributeVals.size() + 1);
    inst.setDataset(dataset);

    /* set attribute values */
    for (int i = 0; i < attributeVals.size(); i++) {
      inst.setValue(i, attributeVals.get(i));
    }

    /* if there is a class assigned */
    try {
      inst.setValue(attributeVals.size(), classValue);
    } catch (IllegalArgumentException e) {
      System.out.println("Class value: " + classValue);
      e.printStackTrace();
    }

    return inst;
  }

  public static void processDataAndAddToDataset(final File imageFile, final Instances dataset, final String classValue) {
    /* create matrix representation of image */
    FastBitmap fb = new FastBitmap(imageFile.getAbsolutePath());
    // Placeholder for applying image filters to the fast bitmap object
    final int min = Math.min(fb.getWidth(), fb.getHeight());
    new Catalano.Imaging.Filters.Grayscale().applyInPlace(fb);
    /* $imagefilter$ */

    Instance inst = applyBP(fb, dataset, classValue);
    dataset.add(inst);
  }

  private static void printUsageErrorMsg() {
    log("ERROR: incorrect number of arguments.\n\t-i path_to_data_zip [number_of_instances_to_build] | for preprocessing data to instances \n\t-t path_to_arff_file | for training a new predictor \n\t-q path_to_file | "
        + "for using an already learned predictor \n\t -acc path_to_arff_file | for computing the accuracy of a learned predictor on the given dataset");
  }

  public static void main(final String[] args) {
    if (args.length < 2) {
      printUsageErrorMsg();
      return;
    }

    /* setup feature extractor */
    /* $featureextraction$ */

    switch (args[0]) {
      case "-t": {
        buildPredictor(new File(args[1]));
        break;
      }
      case "-q": {
        final ImageClassifier predictor = new ImageClassifier(CLASSIFIER_OUT);
        System.out.println(predictor.getPrediction(new File(args[1])));
        break;
      }
      case "-i": {
        if (args.length > 2) {
          buildInstances(new File(args[1]), Integer.parseInt(args[2]));
        } else {
          buildInstances(new File(args[1]), 0);
        }
        break;
      }
      case "-acc": {
        final ImageClassifier predictor = new ImageClassifier(CLASSIFIER_OUT);
        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(args[1])))) {
          final Instances test = (Instances) ois.readObject();
          double accuracy = predictor.computeAccuracy(test);
          System.out.println("acc=" + accuracy);
        } catch (Exception e) {
          e.printStackTrace();
        }
        break;
      }
      default: {
        printUsageErrorMsg();
        break;
      }
    }
  }

  private static void log(final String msg) {
    System.out.println("Gender Predictor: " + msg);
  }
}