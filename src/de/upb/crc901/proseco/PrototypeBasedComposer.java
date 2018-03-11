package de.upb.crc901.proseco;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.command.ExecuteStrategiesCommand;
import de.upb.crc901.proseco.command.InitializeExecutionEnvironmentCommand;
import de.upb.crc901.proseco.command.BootUpInternalBenchmarkServiceCommand;
import de.upb.crc901.proseco.command.ShutDownInternalBenchmarkServiceCommand;
import de.upb.crc901.proseco.command.ValidatePrototypeCommand;
import de.upb.crc901.proseco.command.WaitForStrategiesCommand;
import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;
import jaicore.basic.FileUtil;
import jaicore.basic.PerformanceLogger;

public class PrototypeBasedComposer {
	private static final Logger logger = LoggerFactory.getLogger(PrototypeBasedComposer.class);

	/** Base folder for matching the availability of prototypes */

	private final File prototypeDirectory;

	private ExecutionEnvironment executionEnvironment;

	private final String prototypeName;
	private final String prototypeId;

	private Process internalBenchmarkService;

	List<Process> strategyProcessList = new LinkedList<>();

	public static void run(String prototypeId) throws Exception {
		Thread.currentThread().setName("PrototypeBasedComposer");

		if (!StringUtils.isEmpty(prototypeId)) {
			new PrototypeBasedComposer(prototypeId);
		} else {
			System.out.println("Prototype is not given");
			System.exit(1);
		}
	}

	protected PrintStream outputFile(String name) {
		try {
			return new PrintStream(new BufferedOutputStream(new FileOutputStream(name)), true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Instantiate a new PrototypeBasedComposer for executing the prototype
	 * composition pipeline.
	 *
	 * @param prototypeName
	 *            The name of the prototype which shall be used.
	 */
	public PrototypeBasedComposer(final String prototypeId) throws Exception {
		this.prototypeName = prototypeId.split("-")[0];
		this.prototypeId = prototypeId;

		ValidatePrototypeCommand validatePrototypeCommand = new ValidatePrototypeCommand(this.prototypeName);
		validatePrototypeCommand.execute();
		this.prototypeDirectory = validatePrototypeCommand.getPrototypeDirectory();

		PerformanceLogger.logStart("TotalRuntime");
		// create instance copy of the chosen prototype
		InitializeExecutionEnvironmentCommand initializeExecutionEnvironmentCommand = new InitializeExecutionEnvironmentCommand(
				prototypeId, prototypeDirectory);
		initializeExecutionEnvironmentCommand.execute();
		this.executionEnvironment = initializeExecutionEnvironmentCommand.getExecutionEnvironment();

		PerformanceLogger.logStart("bootUpInternalBenchmarkService");
		BootUpInternalBenchmarkServiceCommand bootUpInternalBenchmarkServiceCommand = new BootUpInternalBenchmarkServiceCommand(
				executionEnvironment);
		bootUpInternalBenchmarkServiceCommand.execute();
		this.internalBenchmarkService = bootUpInternalBenchmarkServiceCommand.getInternalBenchmarkService();
		PerformanceLogger.logEnd("bootUpInternalBenchmarkService");

		PerformanceLogger.logStart("executeStrategies");
		ExecuteStrategiesCommand executeStrategiesCommand = new ExecuteStrategiesCommand(executionEnvironment);
		executeStrategiesCommand.execute();
		this.strategyProcessList = executeStrategiesCommand.getStrategyProcessList();

		WaitForStrategiesCommand waitForStrategiesCommand = new WaitForStrategiesCommand(strategyProcessList);
		waitForStrategiesCommand.execute();
		PerformanceLogger.logEnd("executeStrategies");

		// shutdown internal benchmark, since strategies already terminated
		PerformanceLogger.logStart("shutdownInternalBenchmarkService");
		ShutDownInternalBenchmarkServiceCommand shutDownInternalBenchmarkServiceCommand = new ShutDownInternalBenchmarkServiceCommand(
				this.internalBenchmarkService);
		shutDownInternalBenchmarkServiceCommand.execute();
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

		PerformanceLogger.saveGlobalLogToFile(
				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + "/" + "PBC_performance.log"));

	}

	private void executeFinalTest() {
		System.out.print("Execute final test...");
		try {
			final Process finalTest = new ProcessBuilder(
					executionEnvironment.getExecutionDirectory() + File.separator + Config.EXEC_FINAL_TEST).start();
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
		if (Config.FINAL_CLEAN_UP) {
			System.out.print("Clean up execution directory...");

			try {
				// delete working directories
				FileUtils.deleteDirectory(executionEnvironment.getBenchmarksDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getConfigDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getGroundingDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getParamsDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getStrategyDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getLibsDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getInterviewDirectory());

				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + File.separator
						+ "contTrainingInstances.serialized").delete();
				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + File.separator
						+ "testInstances.serialized").delete();

				final String[] filesInMainDir = { "GroundingRoutine.jar", "InitConfiguration.jar",
						"initconfiguration.bat", "groundingroutine.bat", "src/contTrainingInstances.serialized",
						"src/testInstances.serialized", "src/compile.bat", "src/train.bat" };
				for (final String filename : filesInMainDir) {
					Files.delete(new File(
							executionEnvironment.getExecutionDirectory().getAbsolutePath() + File.separator + filename)
									.toPath());
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			for (final File placeholderFile : executionEnvironment.getSourceDirectory().listFiles()) {
				if (placeholderFile.isFile()
						&& FilenameUtils.getExtension(placeholderFile.getAbsolutePath()).equals("ph")) {
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
		final ProcessBuilder pb = new ProcessBuilder(
				executionEnvironment.getExecutionDirectory().getAbsolutePath() + "/" + Config.INIT_CONFIGURATION_EXEC);
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

	private List<String> getInterviewResourcesForStrategy() {
		List<String> commandArgumentList = new ArrayList<>();

		final File[] interviewResources = executionEnvironment.getInterviewResourcesDirectory()
				.listFiles(new FileFilter() {
					@Override
					public boolean accept(final File file) {
						return file.isFile();
					}
				});

		for (File resource : interviewResources) {
			commandArgumentList.add(resource.getAbsolutePath());
		}
		return commandArgumentList;
	}

	/**
	 * Move all placeholder files created by the winning strategy to the source
	 * folder as inputs for the grounding routine.
	 */
	private void movePlaceholderFilesToSource() {
		System.out.print("Copy placeholder files of winning strategies to src folder "
				+ executionEnvironment.getGroundingDirectory() + " ...");

		// Pick the output of the winning strategy
		String winningStrategyName = "";
		double fValue = 0.0;
		for (final File strategy : executionEnvironment.getStrategyDirectory().listFiles()) {
			if (!strategy.isDirectory()) {
				continue;
			}

			final File fValueFile = new File(
					strategy.getAbsolutePath() + File.separator + Config.OUTPUT_DIR + File.separator + "f.value");
			if (!fValueFile.exists()) {
				System.out.println(
						"f.value file was not found for strategy; ignoring it: " + fValueFile.getAbsolutePath());
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

		final File winningStrategy = new File(executionEnvironment.getStrategyDirectory() + File.separator
				+ winningStrategyName + File.separator + Config.OUTPUT_DIR);
		for (final File strategyFile : winningStrategy.listFiles()) {
			if (strategyFile.isFile()) {
				final File groundingFolderFile = new File(
						executionEnvironment.getGroundingDirectory() + File.separator + strategyFile.getName());
				try {
					FileUtils.copyFile(strategyFile, groundingFolderFile);
				} catch (final IOException e) {
					System.out.println("\nCould not move placeholder file " + strategyFile.getName() + " from "
							+ strategyFile.getAbsolutePath() + " to " + groundingFolderFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		System.out.println("DONE.");
	}

	private void executeGroundingRoutine() {
		final ProcessBuilder pb = new ProcessBuilder(executionEnvironment.getGroundingFile().getAbsolutePath());
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

}
