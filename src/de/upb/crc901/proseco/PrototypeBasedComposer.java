package de.upb.crc901.proseco;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.command.BootUpInternalBenchmarkServiceCommand;
import de.upb.crc901.proseco.command.CleanExecutionEnvironmentCommand;
import de.upb.crc901.proseco.command.ExecuteGroundingRoutineCommand;
import de.upb.crc901.proseco.command.ExecuteStrategiesCommand;
import de.upb.crc901.proseco.command.GetPrototypeDirectoryCommand;
import de.upb.crc901.proseco.command.InitializeExecutionEnvironmentCommand;
import de.upb.crc901.proseco.command.MovePlaceholderFilesToSourceCommand;
import de.upb.crc901.proseco.command.ShutDownInternalBenchmarkServiceCommand;
import de.upb.crc901.proseco.command.WaitForStrategiesCommand;
import jaicore.basic.PerformanceLogger;

/**
 * 
 * PrototypeBasedComposer realizes the service construction of the selected
 * prototype. After a prototype is found for the user's input, the construction
 * steps for the prototype is executed.
 * 
 * <br><br>
 * <img src="doc-files/PrototypeBasedComposer.png">
 *
 */
public class PrototypeBasedComposer {
	private static final Logger logger = LoggerFactory.getLogger(PrototypeBasedComposer.class);

	/** Base folder for matching the availability of prototypes */
	private File prototypeDirectory;

	private ExecutionEnvironment executionEnvironment;

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

	/**
	 * Instantiate a new PrototypeBasedComposer for executing the prototype
	 * composition pipeline.
	 *
	 * @param prototypeId
	 *            prototypeId consists of prototypeName-Id ie.
	 *            imageClassification-E34A778BF1
	 * @throws Exception
	 */
	public PrototypeBasedComposer(final String prototypeId) throws Exception {
		setPrototypeDirectory(prototypeId);

		initializeExecutionEnvironment(prototypeId);

		bootUpInternalBenchmarkService();

		executeStrategies();

		waitForStrategies();

		shutDownInternalBenchmarkService();

		movePlaceholderFilesToSource();

		executeGroundingRoutine();

		cleanExecutionEnvironment();

	}

	/**
	 * Finds the prototype definition with the given prototypeId
	 * 
	 * @param prototypeId
	 * @throws Exception
	 */
	private void setPrototypeDirectory(String prototypeId) throws Exception {
		String prototypeName = getPrototypeNameFromPrototypeId(prototypeId);
		GetPrototypeDirectoryCommand validatePrototypeCommand = new GetPrototypeDirectoryCommand(prototypeName);
		validatePrototypeCommand.execute();
		this.prototypeDirectory = validatePrototypeCommand.getPrototypeDirectory();
	}

	/**
	 * Initializes Execution Environment of the prototype instance. Copies files
	 * from the prototype definition to prototype instance directory
	 * 
	 * @param prototypeId
	 * @throws Exception
	 */
	private void initializeExecutionEnvironment(final String prototypeId) throws Exception {
		PerformanceLogger.logStart("TotalRuntime");
		// create instance copy of the chosen prototype
		InitializeExecutionEnvironmentCommand initializeExecutionEnvironmentCommand = new InitializeExecutionEnvironmentCommand(
				prototypeId, prototypeDirectory);
		initializeExecutionEnvironmentCommand.execute();
		this.executionEnvironment = initializeExecutionEnvironmentCommand.getExecutionEnvironment();
	}

	/**
	 * Starts prototype's BenchmarkService process
	 * 
	 * @throws Exception
	 */
	private void bootUpInternalBenchmarkService() throws Exception {
		PerformanceLogger.logStart("bootUpInternalBenchmarkService");
		BootUpInternalBenchmarkServiceCommand bootUpInternalBenchmarkServiceCommand = new BootUpInternalBenchmarkServiceCommand(
				executionEnvironment);
		bootUpInternalBenchmarkServiceCommand.execute();
		this.internalBenchmarkService = bootUpInternalBenchmarkServiceCommand.getInternalBenchmarkService();
		PerformanceLogger.logEnd("bootUpInternalBenchmarkService");
	}

	/**
	 * Executes search strategies of the prototype
	 * 
	 * @throws Exception
	 */
	private void executeStrategies() throws Exception {
		PerformanceLogger.logStart("executeStrategies");
		ExecuteStrategiesCommand executeStrategiesCommand = new ExecuteStrategiesCommand(executionEnvironment);
		executeStrategiesCommand.execute();
		this.strategyProcessList = executeStrategiesCommand.getStrategyProcessList();
	}

	/**
	 * Waits for strategies to complete
	 * 
	 * @throws Exception
	 */
	private void waitForStrategies() throws Exception {
		WaitForStrategiesCommand waitForStrategiesCommand = new WaitForStrategiesCommand(strategyProcessList);
		waitForStrategiesCommand.execute();
		PerformanceLogger.logEnd("executeStrategies");
	}

	/**
	 * Stops prototype's BenchmarkService process
	 * 
	 * @throws Exception
	 */
	private void shutDownInternalBenchmarkService() throws Exception {
		// shutdown internal benchmark, since strategies already terminated
		PerformanceLogger.logStart("shutdownInternalBenchmarkService");
		ShutDownInternalBenchmarkServiceCommand shutDownInternalBenchmarkServiceCommand = new ShutDownInternalBenchmarkServiceCommand(
				this.internalBenchmarkService);
		shutDownInternalBenchmarkServiceCommand.execute();
		PerformanceLogger.logEnd("shutdownInternalBenchmarkService");
	}

	/**
	 * Moves placeholder files of the winning strategy to prototype instance's
	 * src folder.
	 * 
	 * @throws Exception
	 */
	private void movePlaceholderFilesToSource() throws Exception {
		PerformanceLogger.logStart("movePlaceholderFilesToSource");
		MovePlaceholderFilesToSourceCommand movePlaceholderFilesToSourceCommand = new MovePlaceholderFilesToSourceCommand(
				executionEnvironment);
		movePlaceholderFilesToSourceCommand.execute();
		PerformanceLogger.logEnd("movePlaceholderFilesToSource");
	}

	/**
	 * Executes grounding routine of the prototype instance
	 * 
	 * @throws Exception
	 */
	private void executeGroundingRoutine() throws Exception {
		PerformanceLogger.logStart("executeGroundingRoutine");
		ExecuteGroundingRoutineCommand executeGroundingRoutineCommand = new ExecuteGroundingRoutineCommand(
				executionEnvironment);
		executeGroundingRoutineCommand.execute();
		PerformanceLogger.logEnd("executeGroundingRoutine");
	}

	/**
	 * Clean up the working directory after search is done
	 * 
	 * @throws Exception
	 */
	private void cleanExecutionEnvironment() throws Exception {
		PerformanceLogger.logStart("cleanUp");
		CleanExecutionEnvironmentCommand cleanExecutionEnvironmentCommand = new CleanExecutionEnvironmentCommand();
		cleanExecutionEnvironmentCommand.execute();
		PerformanceLogger.logEnd("cleanUp");

		PerformanceLogger.logEnd("TotalRuntime");

		System.out.println("Execution of PrototypeBasedComposer successful.");

		PerformanceLogger.saveGlobalLogToFile(
				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + "/" + "PBC_performance.log"));
	}

	/**
	 * Extract prototypeName of prototypeName-Id pair
	 * 
	 * @param prototypeId
	 * @return
	 */
	private String getPrototypeNameFromPrototypeId(String prototypeId) {
		String prototypeName = null;
		if (prototypeId != null) {
			String[] prototypeNameIdPair = prototypeId.split("-");
			if (prototypeNameIdPair.length > 0) {
				prototypeName = prototypeNameIdPair[0];
			}
		}
		return prototypeName;
	}

}
