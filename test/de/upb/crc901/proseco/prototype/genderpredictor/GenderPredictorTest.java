package de.upb.crc901.proseco.prototype.genderpredictor;

import java.io.File;

import org.junit.Test;

public class GenderPredictorTest {

	@Test
	public void buildInstancesTest() {
		File dataZip = new File("testrsc/featureExtractionBenchmark/data.zip");
		// build instances
		GenderPredictor.buildInstances(dataZip, 6);
	}

}
