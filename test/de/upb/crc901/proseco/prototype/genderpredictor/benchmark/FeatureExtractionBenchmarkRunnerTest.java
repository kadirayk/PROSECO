package de.upb.crc901.proseco.prototype.genderpredictor.benchmark;

import java.io.File;

import org.junit.Test;

import de.upb.crc901.proseco.prototype.genderpredictor.GroundingRoutine;
import de.upb.crc901.proseco.prototype.genderpredictor.benchmark.BenchmarkTask.EBuildPhase;
import de.upb.crc901.proseco.prototype.genderpredictor.benchmark.BenchmarkTask.EDataFraction;
import de.upb.crc901.proseco.prototype.genderpredictor.benchmark.featureextraction.FeaturekNNEvaluator;

public class FeatureExtractionBenchmarkRunnerTest {

	@Test
	public void runTest() {
		File taskTempFolder = new File("testrsc/featureExtractionBenchmark/taskTemp");
		File placeholderFolder = new File("testrsc/featureExtractionBenchmark/placeholder");
		File sourceFolder = new File("testrsc/featureExtractionBenchmark/src/");
		// File dataFile = new File("testrsc/featureExtractionBenchmark/data.zip");
		File dataFile = new File("lfwgender_dataset.zip");

		for (int i = 0; i < 1; i++) {
			long startTime = System.currentTimeMillis();
			BenchmarkTask benchmarkTask = new BenchmarkTaskBuilder().setCandidateFolder(placeholderFolder)
					.setBuildPhase(EBuildPhase.FEATURE_EXTRACTION).setDataFraction(EDataFraction.FULL).build();

			GroundingRoutine groundingRoutine = new GroundingRoutine(placeholderFolder, sourceFolder, taskTempFolder);

			// create new feature extraction benchmark runner
			new FeatureExtractionBenchmarkRunner(benchmarkTask, groundingRoutine, taskTempFolder, dataFile,
					new FeaturekNNEvaluator()).run();
			System.out.println(
					"FeatureExtractionBenchmarkRunner test done [" + (System.currentTimeMillis() - startTime) + "ms]");
		}

	}

}
