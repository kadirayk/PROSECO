package de.upb.crc901.proseco.core;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.composition.StrategyExecutor;

public class StrategyExecutorTester {

	@Test
	public void testStrategyExecutor() throws IOException, InterruptedException {
		File processFolder = new File("processes/automl-e1c0589f52/");
		PROSECOProcessEnvironment processEnvironment = new PROSECOProcessEnvironment(processFolder);
		StrategyExecutor executor = new StrategyExecutor(processEnvironment);
		executor.execute(60000);

	}

}
