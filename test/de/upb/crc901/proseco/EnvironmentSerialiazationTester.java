package de.upb.crc901.proseco;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.aeonbits.owner.ConfigCache;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;

public class EnvironmentSerialiazationTester {

	@Test
	public void test() throws Exception {
		PROSECOConfig config = ConfigCache.getOrCreate(PROSECOConfig.class);
		String id = "test-id";
		File serializationFolder = new File("tmp");
		File serializationFile = new File(serializationFolder + File.separator + "env.properties");
		File processFolder = new File(config.getExecutionFolder() + File.separator + id);
		FileUtils.forceMkdir(processFolder);
		FileUtils.forceMkdir(serializationFolder);
		
		PROSECOProcessEnvironment env = new PROSECOProcessEnvironment(config, id);
		ObjectMapper om = new ObjectMapper();
		om.writeValue(serializationFile, env);
		PROSECOProcessEnvironment env2 = om.readValue(serializationFile, PROSECOProcessEnvironment.class);
		assertEquals(env, env2);
	}

}
