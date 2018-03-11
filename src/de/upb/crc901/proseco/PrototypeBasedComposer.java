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
import de.upb.crc901.proseco.command.InitializeExecutionEnvironmentCommand;
import de.upb.crc901.proseco.command.MovePlaceholderFilesToSourceCommand;
import de.upb.crc901.proseco.command.ShutDownInternalBenchmarkServiceCommand;
import de.upb.crc901.proseco.command.ValidatePrototypeCommand;
import de.upb.crc901.proseco.command.WaitForStrategiesCommand;
import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
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
		MovePlaceholderFilesToSourceCommand movePlaceholderFilesToSourceCommand = new MovePlaceholderFilesToSourceCommand(
				executionEnvironment);
		movePlaceholderFilesToSourceCommand.execute();
		PerformanceLogger.logEnd("movePlaceholderFilesToSource");

		PerformanceLogger.logStart("executeGroundingRoutine");
		ExecuteGroundingRoutineCommand executeGroundingRoutineCommand = new ExecuteGroundingRoutineCommand(
				executionEnvironment);
		executeGroundingRoutineCommand.execute();
		PerformanceLogger.logEnd("executeGroundingRoutine");

		PerformanceLogger.logStart("cleanUp");
		CleanExecutionEnvironmentCommand cleanExecutionEnvironmentCommand = new CleanExecutionEnvironmentCommand();
		cleanExecutionEnvironmentCommand.execute();
		PerformanceLogger.logEnd("cleanUp");

		PerformanceLogger.logEnd("TotalRuntime");

		System.out.println("Execution of PrototypeBasedComposer successful.");

		PerformanceLogger.saveGlobalLogToFile(
				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + "/" + "PBC_performance.log"));

	}

}
