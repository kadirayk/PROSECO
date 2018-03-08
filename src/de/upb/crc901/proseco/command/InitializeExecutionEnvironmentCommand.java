package de.upb.crc901.proseco.command;

import java.io.File;

import org.apache.commons.io.FileUtils;

import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;

public class InitializeExecutionEnvironmentCommand implements Command {
	private File prototypeDirectory;
	private File executionDirectory;
	private ExecutionEnvironment executionEnvironment;

	public InitializeExecutionEnvironmentCommand(String prototypeId, File prototypeDirectory) {
		this.prototypeDirectory = prototypeDirectory;
		this.executionDirectory = new File(Config.EXECUTIONS.getAbsolutePath() + File.separator + prototypeId);

		File benchmarksDirectory = new File(inExecutionEnvironment(Config.BENCHMARKS));
		File groundingDirectory = new File(inExecutionEnvironment(Config.GROUNDING));
		File strategyDirectory = new File(inExecutionEnvironment(Config.STRATEGIES));
		File configDirectory = new File(inExecutionEnvironment(Config.CONFIG));
		File paramsDirectory = new File(inExecutionEnvironment(Config.PARAMS));
		File sourceDirectory = new File(inExecutionEnvironment(Config.SOURCE));
		File libsDirectory = new File(inExecutionEnvironment(Config.LIBS));
		File groundingFile = new File(inExecutionEnvironment(Config.GROUNDING_ROUTINE));
		File interviewDirectory = new File(inExecutionEnvironment(Config.INTERVIEW));
		File interviewResourcesDirectory = new File(
				inExecutionEnvironment(Config.INTERVIEW + File.separator + Config.INTERVIEW_RESOURCES));

		executionEnvironment = new ExecutionEnvironment.Builder(prototypeId, executionDirectory)
				.withBenchmarksDirectory(benchmarksDirectory).withConfigDirectory(configDirectory)
				.withGroundingDirectory(groundingDirectory).withGroundingFile(groundingFile)
				.withInterviewDirectory(interviewDirectory).withInterviewResourcesDirectory(interviewResourcesDirectory)
				.withLibsDirectory(libsDirectory).withParamsDirectory(paramsDirectory)
				.withSourceDirectory(sourceDirectory).withStrategyDirectory(strategyDirectory).build();

	}

	private String inExecutionEnvironment(String fileName) {
		return this.executionDirectory.getAbsolutePath() + File.separator + fileName;
	}

	@Override
	public void execute() throws Exception {
		System.out.print("Copy prototype files to temporary execution directory...");
		FileUtils.copyDirectory(this.prototypeDirectory, this.executionDirectory);
		System.out.println("DONE.");
	}

	public ExecutionEnvironment getExecutionEnvironment() {
		return this.executionEnvironment;
	}

}
