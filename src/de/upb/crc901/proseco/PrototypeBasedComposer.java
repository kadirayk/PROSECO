package de.upb.crc901.proseco;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.view.core.model.Interview;
import jaicore.basic.FileUtil;
import jaicore.basic.PerformanceLogger;

public class PrototypeBasedComposer {
  private static final Logger logger = LoggerFactory.getLogger(PrototypeBasedComposer.class);

  private static final PrototypeProperties PROPS = new PrototypeProperties("config/PrototypeBasedComposer.conf");

  private static final boolean FINAL_CLEAN_UP = Boolean.parseBoolean(PROPS.getProperty("pbc.final_clean_up"));

  private static final File PROTOTYPES = new File(PROPS.getProperty("pbc.prototypes_path"));
  private static final File EXECUTIONS = new File(PROPS.getProperty("pbc.executions_path"));

  private static final String BENCHMARKS = PROPS.getProperty("pbc.benchmarks_path");
  private static final String STRATEGIES = PROPS.getProperty("pbc.strategies_path");
  private static final String SOURCE = PROPS.getProperty("pbc.source_path");
  private static final String CONFIG = PROPS.getProperty("pbc.config_path");
  private static final String GROUNDING = PROPS.getProperty("pbc.grounding_path");
  private static final String PARAMS = PROPS.getProperty("pbc.params_path");
  private static final String LIBS = PROPS.getProperty("pbc.libs_path");

  private static final String OUTPUT_DIR = PROPS.getProperty("pbc.output_directory");

  /** Base folder for matching the availability of prototypes */

  private static final String INTERNAL_BENCHMARK_FOLDER = "benchmarks/";

  private static final String DATAFILE_NAME = "data.zip";
  private static final String STRATEGY_RUNNABLE = "run.bat";
  private static final String GROUNDING_ROUTINE = "groundingroutine.bat";
  private static final String INIT_CONFIGURATION_EXEC = "initconfiguration.bat";
  private static final String BENCHMARK_SERVICE = "benchmarkService.bat";
  private static final String EXEC_FINAL_TEST = "src/test.bat";

  private final File prototypeDirectory;
  private File groundingDirectory;
  private File strategyDirectory;
  private File configDirectory;
  private File paramsDirectory;
  private File sourceDirectory;
  private File benchmarksDirectory;
  private File libsDirectory;

  private File groundingFile;

  /**
   * Start the PrototypeBasedComposer with the given command line arguments.
   *
   * @param args
   *          Command line arguments consisting of a prototype name and a path to the data zip file.
   */
  public static void main(final String[] args) {
    Thread.currentThread().setName("PrototypeBasedComposer");
    final String prototypeName;
    final String dataFilePath;

    // Ensure required arguments for initializing the prototype based composition process
    if (args.length != 2) {
      System.out.println("Provided arguments do not match the usage requirements.");
      System.out.println("Correct usage: java PrototypeBasedComposer [prototype name] [path to data file]");
      System.exit(1);
    }

    // copy prototype name from arguments
    prototypeName = args[0];
    // copy data file path from arguments
    dataFilePath = args[1];

    new PrototypeBasedComposer(prototypeName, dataFilePath);
  }
  
  public static void run(Interview interview) {
	    Thread.currentThread().setName("PrototypeBasedComposer");
	    final String prototypeName;
	    final String dataFilePath;
	    
	    prototypeName = interview.getQuestionByPath("step1.q1").getAnswer();
	    dataFilePath = interview.getQuestionByPath("step4.q1").getAnswer();

	    new PrototypeBasedComposer(prototypeName, dataFilePath);
	  }

  private final String dataFilePath;
  private final String prototypeName;

  private File executionDirectory;
  private File executionDataZip;

  private Process internalBenchmarkService;

  List<Process> strategyProcessList = new LinkedList<>();

  /**
   * Instantiate a new PrototypeBasedComposer for executing the prototype composition pipeline.
   *
   * @param prototypeName
   *          The name of the prototype which shall be used.
   * @param dataFilePath
   *          Path to the zip file containing the training data.
   * @param testDataFilePath
   * @param validateDataFilePath
   */
  public PrototypeBasedComposer(final String prototypeName, final String dataFilePath) {
    this.prototypeName = prototypeName;
    this.dataFilePath = dataFilePath;

    this.prototypeDirectory = new File(PROTOTYPES.getAbsolutePath() + File.separator + this.prototypeName);

    final File dataFile = new File(this.dataFilePath);

    if (!this.prototypeDirectory.exists() || !this.prototypeDirectory.isDirectory()) {
      System.out.println("No such prototype available.");
      System.exit(1);
    }

    if (!dataFile.exists() || !dataFile.isFile() || !FilenameUtils.getExtension(dataFilePath).equals("zip")) {
      System.out.println(FilenameUtils.getExtension(dataFilePath));
      System.out.println("Data file does not exists or has incorrect extension. Only zip files are allowed.");
      System.exit(1);
    }

    try {
      PerformanceLogger.logStart("TotalRuntime");
      // create instance copy of the chosen prototype
      this.initializeExecutionEnvironment(prototypeName, dataFile);

      PerformanceLogger.logStart("bootUpInternalBenchmarkService");
      this.bootUpInternalBenchmarkService();
      PerformanceLogger.logEnd("bootUpInternalBenchmarkService");

      this.strategyProcessList = new LinkedList<>();
      PerformanceLogger.logStart("executeStrategies");
      this.executeStrategies();

      this.waitForStrategiesToTerminate();
      PerformanceLogger.logEnd("executeStrategies");

      // shutdown internal benchmark, since strategies already terminated
      PerformanceLogger.logStart("shutdownInternalBenchmarkService");
      this.shutdownInternalBenchmarkService();
      PerformanceLogger.logEnd("shutdownInternalBenchmarkService");

      PerformanceLogger.logStart("movePlaceholderFilesToSource");
      this.movePlaceholderFilesToSource();
      PerformanceLogger.logEnd("movePlaceholderFilesToSource");

      PerformanceLogger.logStart("executeGroundingRoutine");
      this.executeGroundingRoutine();
      PerformanceLogger.logEnd("executeGroundingRoutine");

      PerformanceLogger.logStart("cleanUp");
      this.clean();
      PerformanceLogger.logEnd("cleanUp");

      PerformanceLogger.logEnd("TotalRuntime");

      System.out.println("Execution of PrototypeBasedComposer successful.");

      PerformanceLogger.saveGlobalLogToFile(new File(this.executionDirectory.getAbsolutePath() + "/" + "PBC_performance.log"));

    } catch (final IOException e) {
      System.out.println("Could not create temporary folder for prototype instance");
      e.printStackTrace();
    }

  }

  private void executeFinalTest() {
    System.out.print("Execute final test...");
    try {
      final Process finalTest = new ProcessBuilder(this.executionDirectory + File.separator + EXEC_FINAL_TEST).start();
      finalTest.waitFor();
    } catch (final InterruptedException e) {
      System.err.println("Final test process failed");
      e.printStackTrace();
    } catch (final IOException e1) {
      System.err.println("Could not start process for final test execution.");
      e1.printStackTrace();
    }
    System.out.println("DONE.");
  }

  private void clean() {
    if (FINAL_CLEAN_UP) {
      System.out.print("Clean up execution directory...");

      try {
        // delete working directories
        FileUtils.deleteDirectory(this.benchmarksDirectory);
        FileUtils.deleteDirectory(this.configDirectory);
        FileUtils.deleteDirectory(this.groundingDirectory);
        FileUtils.deleteDirectory(this.paramsDirectory);
        FileUtils.deleteDirectory(this.strategyDirectory);
        FileUtils.deleteDirectory(this.libsDirectory);

        this.executionDataZip.delete();
        new File(this.executionDirectory.getAbsolutePath() + File.separator + "contTrainingInstances.serialized").delete();
        new File(this.executionDirectory.getAbsolutePath() + File.separator + "testInstances.serialized").delete();

        final String[] filesInMainDir = { "GroundingRoutine.jar", "InitConfiguration.jar", "initconfiguration.bat", "groundingroutine.bat", "src/contTrainingInstances.serialized",
            "src/testInstances.serialized", "src/compile.bat", "src/train.bat" };
        for (final String filename : filesInMainDir) {
          Files.delete(new File(this.executionDirectory.getAbsolutePath() + File.separator + filename).toPath());
        }
      } catch (final IOException e) {
        e.printStackTrace();
      }
      for (final File placeholderFile : this.sourceDirectory.listFiles()) {
        if (placeholderFile.isFile() && FilenameUtils.getExtension(placeholderFile.getAbsolutePath()).equals("ph")) {
          try {
            Files.delete(placeholderFile.toPath());
          } catch (final IOException e) {
            System.out.println("Could not delete placeholder file : " + placeholderFile.getAbsolutePath());
            e.printStackTrace();
          }
        }
      }
      System.out.println("DONE.");
    }
  }

  private void initConfigurationRoutine() throws IOException {
    // execute script file for initial configuration process
    System.out.print("Execute initial configuration process...");
    final ProcessBuilder pb = new ProcessBuilder(this.executionDirectory.getAbsolutePath() + "/" + INIT_CONFIGURATION_EXEC);
    pb.redirectOutput(Redirect.INHERIT);
    pb.redirectError(Redirect.INHERIT);

    final Process initConfigProcess = pb.start();

    try {
      initConfigProcess.waitFor();
    } catch (final InterruptedException e) {
      System.out.println("Initial configuration process failed.");
      e.printStackTrace();
      System.exit(-1);
    }
    System.out.println("DONE.");
  }

  private void bootUpInternalBenchmarkService() {
    System.out.print("Boot up internal benchmark service...");

    final File benchmarkExec = new File(this.executionDirectory.getAbsolutePath() + "/" + INTERNAL_BENCHMARK_FOLDER + BENCHMARK_SERVICE);
    final ProcessBuilder pb = new ProcessBuilder(benchmarkExec.getAbsolutePath()).redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);

    try {
      this.internalBenchmarkService = pb.start();

      try (BufferedReader br = new BufferedReader(new InputStreamReader(this.internalBenchmarkService.getErrorStream()))) {
        String line;
        boolean serviceUpAndRunning = false;
        while (!serviceUpAndRunning && ((line = br.readLine()) != null)) {
          if (line.contains("Service up and running")) {
            serviceUpAndRunning = true;
          }
        }
      }
      System.out.println("DONE.");

    } catch (final IOException e) {
      System.err.println("ERROR: Could not boot benchmark service.");
      System.exit(1);
    }
  }

  /**
   * Search for strategy subfolders and forking a new process for each strategy.
   */
  private void executeStrategies() {

    final File[] strategySubFolders = this.strategyDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(final File file) {
        return file.isDirectory();
      }
    });

    for (final File strategyFolder : strategySubFolders) {
      System.out.print("Starting process for strategy " + strategyFolder.getName() + "...");
      final ProcessBuilder pb = new ProcessBuilder(strategyFolder.getAbsolutePath() + "/" + STRATEGY_RUNNABLE, this.executionDataZip.getAbsolutePath())
          .redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
      try {
        final Process p = pb.start();
        this.strategyProcessList.add(p);
        System.out.println("DONE.");
      } catch (final IOException e) {
        System.out.println("Could not create process for strategy " + strategyFolder.getName());
        e.printStackTrace();
      }
    }

  }

  private void shutdownInternalBenchmarkService() {
    System.out.print("Shutdown internal benchmark service...");
    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(this.internalBenchmarkService.getOutputStream()))) {
      bw.write("q\n");
    } catch (final IOException e1) {
      e1.printStackTrace();
    }

    try {
      this.internalBenchmarkService.waitFor();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("DONE.");
  }

  /**
   * Move all placeholder files created by the winning strategy to the source folder as inputs for the
   * grounding routine.
   */
  private void movePlaceholderFilesToSource() {
    System.out.print("Copy placeholder files of winning strategies to src folder " + this.groundingDirectory + " ...");

    // Pick the output of the winning strategy
    String winningStrategyName = "";
    double fValue = 0.0;
    for (final File strategy : this.strategyDirectory.listFiles()) {
      if (!strategy.isDirectory()) {
        continue;
      }

      final File fValueFile = new File(strategy.getAbsolutePath() + File.separator + OUTPUT_DIR + File.separator + "f.value");
      if (!fValueFile.exists()) {
        System.out.println("f.value file was not found for strategy; ignoring it: " + fValueFile.getAbsolutePath());
        continue;
      }
      Double parsedValue = 0.0;
      try {
        parsedValue = Double.parseDouble(FileUtil.readFileAsString(fValueFile.getAbsolutePath()));
      } catch (final NumberFormatException e) {
        e.printStackTrace();
      } catch (final IOException e) {
        e.printStackTrace();
      }

      if (parsedValue >= fValue) {
        winningStrategyName = strategy.getName();
        fValue = parsedValue;
      }
    }

    if (winningStrategyName.isEmpty()) {
      throw new IllegalStateException("Name of the winning strategyg is not filled.");
    }

    final File winningStrategy = new File(this.strategyDirectory + File.separator + winningStrategyName + File.separator + OUTPUT_DIR);
    for (final File strategyFile : winningStrategy.listFiles()) {
      if (strategyFile.isFile()) {
        final File groundingFolderFile = new File(this.groundingDirectory.getAbsolutePath() + File.separator + strategyFile.getName());
        try {
          FileUtils.copyFile(strategyFile, groundingFolderFile);
        } catch (final IOException e) {
          System.out
              .println("\nCould not move placeholder file " + strategyFile.getName() + " from " + strategyFile.getAbsolutePath() + " to " + groundingFolderFile.getAbsolutePath());
          e.printStackTrace();
        }
      }
    }

    System.out.println("DONE.");
  }

  private void executeGroundingRoutine() {
    final ProcessBuilder pb = new ProcessBuilder(this.groundingFile.getAbsolutePath());
    System.out.print("Execute grounding process...");
    Process p;
    try {
      p = pb.start();
      while (p.isAlive()) {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    } catch (final IOException e1) {
      e1.printStackTrace();
    }
    System.out.println("DONE.");
  }

  /**
   * Wait (busy waiting) for the processes to terminate that have been started to execute the
   * different strategies.
   *
   */
  private void waitForStrategiesToTerminate() {
    System.out.println("PBC: Wait for strategies to terminate.");
    boolean oneRunning = true;
    while (oneRunning) {
      oneRunning = false;

      for (final Process p : this.strategyProcessList) {
        if (p.isAlive()) {
          oneRunning = true;
          break;
        }
      }
      try {
        Thread.sleep(2000);
      } catch (final InterruptedException e) {
        e.printStackTrace();
      }
    }
    System.out.println("PBC: Wait for strategies to terminate.");
  }

  /**
   * Initialize the folder structure for the matched prototype name and copy all the files into the
   * execution folder. Afterwards execute the initial configuration routine of the prototype.
   *
   * @param prototypeName
   * @param prototypeFolder
   * @param dataFile
   * @param testFile
   * @param valFile
   * @throws IOException
   */
  private void initializeExecutionEnvironment(final String prototypeName, final File dataFile) throws IOException {
    // copy prototype skeleton and data zip to temporary execution folder
    System.out.print("Copy prototype files to temporary execution directory...");

    // set execution directory
    this.executionDirectory = new File(EXECUTIONS.getAbsolutePath() + File.separator + prototypeName + "-" + System.currentTimeMillis());
    // set data zip file for execution
    this.executionDataZip = new File(this.executionDirectory.getAbsolutePath() + "/" + DATAFILE_NAME);

    // initialize variables for working directories
    this.benchmarksDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + BENCHMARKS);
    this.groundingDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + GROUNDING);
    this.strategyDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + STRATEGIES);
    this.configDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + CONFIG);
    this.paramsDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + PARAMS);
    this.sourceDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + SOURCE);
    this.libsDirectory = new File(this.executionDirectory.getAbsolutePath() + File.separator + LIBS);

    this.groundingFile = new File(this.executionDirectory.getAbsolutePath() + File.separator + GROUNDING_ROUTINE);

    // copy prototype files into execution directory
    FileUtils.copyDirectory(this.prototypeDirectory, this.executionDirectory);
    FileUtils.copyFile(dataFile, this.executionDataZip);

    System.out.println("DONE.");

  }

}
