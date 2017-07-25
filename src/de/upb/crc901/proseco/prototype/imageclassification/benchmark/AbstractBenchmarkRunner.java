package de.upb.crc901.proseco.prototype.imageclassification.benchmark;

import java.io.File;

import de.upb.crc901.proseco.prototype.imageclassification.GroundingRoutine;

public abstract class AbstractBenchmarkRunner {

	private final BenchmarkTask task;
	private final GroundingRoutine groundingRoutine;
	private final File taskTempFolder;

	protected AbstractBenchmarkRunner(final BenchmarkTask pTask, final GroundingRoutine pGroundingRoutine, final File taskTempFolder) {
		this.task = pTask;
		this.groundingRoutine = pGroundingRoutine;
		this.taskTempFolder = taskTempFolder;
	}

	protected BenchmarkTask getTask() {
		return this.task;
	}

	protected GroundingRoutine getGroundingRoutine() {
		return this.groundingRoutine;
	}

	protected File getTaskTempFolder() {
		return this.taskTempFolder;
	}

	public abstract void run();

}
