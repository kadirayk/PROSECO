package de.upb.crc901.proseco.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified class for configuration values
 * 
 * @author kadirayk, fmohr
 *
 */
public interface DomainConfig extends Mutable {

	@Key(ConfigConstants.INTERVIEW)
	@DefaultValue("interview")
	public String getNameOfInterviewFolder();

	@Key(ConfigConstants.INTERVIEW_RESOURCES)
	@DefaultValue("res")
	public String getNameOfInterviewResourceFolder();

	@Key(ConfigConstants.PROTOTYPE_FOLDER)
	@DefaultValue("prototypes")
	public String getPrototypeFolder();

	@Key(ConfigConstants.INTERVIEW_STATE_FILE)
	@DefaultValue("interview_state.json")
	public String getNameOfInterviewStateFile();

	public static DomainConfig get(String file) {
		return get(new File(file));
	}

	public static DomainConfig get(File file) {
		Properties props = new Properties();
		final Logger logger = LoggerFactory.getLogger(DomainConfig.class);
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.error(String.format("Could not find config file %s. Assuming default configuration", file));
		} catch (IOException e) {
			logger.error(String.format(
					"Encountered problem with config file %s. Assuming default configuration. Problem: %s", file,
					e.getMessage()));
		}

		return ConfigFactory.create(DomainConfig.class, props);
	}
}
