package de.upb.crc901.proseco;

import java.io.File;

/**
 * ExecutionEnvironment, is the directory where an instance of the selected
 * prototype is created.
 * 
 * @author kadirayk
 *
 */
public class ExecutionEnvironment {
	private final String prototypeId;
	private final File executionDirectory;
	private final File benchmarksDirectory;
	private final File groundingDirectory;
	private final File strategyDirectory;
	private final File configDirectory;
	private final File paramsDirectory;
	private final File sourceDirectory;
	private final File libsDirectory;
	private final File interviewDirectory;
	private final File interviewResourcesDirectory;
	private final File groundingFile;

	/**
	 * Builder, helps ExecutionEnvironment to be initiated with the selected
	 * parameters
	 * 
	 * @author kadirayk
	 *
	 */
	public static class Builder {
		private final String prototypeId;
		private final File executionDirectory;
		private File benchmarksDirectory;
		private File groundingDirectory;
		private File strategyDirectory;
		private File configDirectory;
		private File paramsDirectory;
		private File sourceDirectory;
		private File libsDirectory;
		private File interviewDirectory;
		private File interviewResourcesDirectory;
		private File groundingFile;

		public Builder(String prototypeId, File executionDirectory) {
			this.prototypeId = prototypeId;
			this.executionDirectory = executionDirectory;
		}

		public Builder withBenchmarksDirectory(File benchmarksDirectory) {
			this.benchmarksDirectory = benchmarksDirectory;
			return this;
		}

		public Builder withGroundingDirectory(File groundingDirectory) {
			this.groundingDirectory = groundingDirectory;
			return this;
		}

		public Builder withStrategyDirectory(File strategyDirectory) {
			this.strategyDirectory = strategyDirectory;
			return this;
		}

		public Builder withConfigDirectory(File configDirectory) {
			this.configDirectory = configDirectory;
			return this;
		}

		public Builder withParamsDirectory(File paramsDirectory) {
			this.paramsDirectory = paramsDirectory;
			return this;
		}

		public Builder withSourceDirectory(File sourceDirectory) {
			this.sourceDirectory = sourceDirectory;
			return this;
		}

		public Builder withLibsDirectory(File libsDirectory) {
			this.libsDirectory = libsDirectory;
			return this;
		}

		public Builder withInterviewDirectory(File interviewDirectory) {
			this.interviewDirectory = interviewDirectory;
			return this;
		}

		public Builder withInterviewResourcesDirectory(File interviewResourcesDirectory) {
			this.interviewResourcesDirectory = interviewResourcesDirectory;
			return this;
		}

		public Builder withGroundingFile(File groundingFile) {
			this.groundingFile = groundingFile;
			return this;
		}

		public ExecutionEnvironment build() {
			return new ExecutionEnvironment(this);
		}

	}

	public ExecutionEnvironment(Builder builder) {
		executionDirectory = builder.executionDirectory;
		prototypeId = builder.prototypeId;
		benchmarksDirectory = builder.benchmarksDirectory;
		groundingDirectory = builder.groundingDirectory;
		strategyDirectory = builder.strategyDirectory;
		configDirectory = builder.configDirectory;
		paramsDirectory = builder.paramsDirectory;
		sourceDirectory = builder.sourceDirectory;
		libsDirectory = builder.libsDirectory;
		interviewDirectory = builder.interviewDirectory;
		interviewResourcesDirectory = builder.interviewResourcesDirectory;
		groundingFile = builder.groundingFile;
	}

	public String getPrototypeId() {
		return prototypeId;
	}

	public File getExecutionDirectory() {
		return executionDirectory;
	}

	public File getBenchmarksDirectory() {
		return benchmarksDirectory;
	}

	public File getGroundingDirectory() {
		return groundingDirectory;
	}

	public File getStrategyDirectory() {
		return strategyDirectory;
	}

	public File getConfigDirectory() {
		return configDirectory;
	}

	public File getParamsDirectory() {
		return paramsDirectory;
	}

	public File getSourceDirectory() {
		return sourceDirectory;
	}

	public File getLibsDirectory() {
		return libsDirectory;
	}

	public File getInterviewDirectory() {
		return interviewDirectory;
	}

	public File getInterviewResourcesDirectory() {
		return interviewResourcesDirectory;
	}

	public File getGroundingFile() {
		return groundingFile;
	}

}
