package de.upb.crc901.proseco.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

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
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("Could not find config file " + file + ". Assuming default configuration");
		} catch (IOException e) {
			System.err.println("Encountered problem with config file " + file + ". Assuming default configuration. Problem:" + e.getMessage());
		}

		return ConfigFactory.create(DomainConfig.class, props);
	}
}
