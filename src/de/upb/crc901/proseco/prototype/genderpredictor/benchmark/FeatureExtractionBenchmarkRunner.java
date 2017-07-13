package de.upb.crc901.proseco.prototype.genderpredictor.benchmark;

import java.io.File;

import de.upb.crc901.proseco.prototype.genderpredictor.GroundingRoutine;

public class FeatureExtractionBenchmarkRunner extends AbstractBenchmarkRunner {

	public FeatureExtractionBenchmarkRunner(final BenchmarkTask pTask, final GroundingRoutine pGroundingRoutine, final File taskTempFolder) {
		super(pTask, pGroundingRoutine,taskTempFolder);
	}

	@Override
	public void run() {
		this.getGroundingRoutine().codeAssembly();

		this.getGroundingRoutine().compile();

	}



}
