package de.upb.crc901.proseco.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface PROSECOConfig extends Mutable, Accessible {

	@Key(ConfigConstants.DOMAINS_PATH)
	@DefaultValue("domains")
	public File getDirectoryForDomains();

	@Key(ConfigConstants.PROCESS_PATH)
	@DefaultValue("processes")
	public File getDirectoryForProcesses();

	@Key(ConfigConstants.FINAL_CLEAN_UP)
	@DefaultValue("true")
	public boolean isFinalCleanupEnabled();

	@Key(ConfigConstants.OUTPUT_DIR)
	@DefaultValue("out")
	public String getNameOfOutputFolder();

	@Key(ConfigConstants.SERVICE_LOG_FILE)
	@DefaultValue("service_log")
	public String getNameOfServiceLogFile();

	@Key(ConfigConstants.SYSTEM_OUT_FILE)
	@DefaultValue("console.out")
	public String getSystemOutFileName();

	@Key(ConfigConstants.SYSTEM_ERR_FILE)
	@DefaultValue("console.err")
	public String getSystemErrFileName();

	@Key(ConfigConstants.SYSTEM_ALL_FILE)
	@DefaultValue("console.all")
	public String getSystemMergedOutputFileName();

	public static PROSECOConfig get(String file) {
		return get(new File(file));
	}

	public static PROSECOConfig get(File file) {
		Properties props = new Properties();
		final Logger logger = LoggerFactory.getLogger(PROSECOConfig.class);
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			logger.error(String.format("Could not find config file %s. Assuming default configuration", file));
		} catch (IOException e) {
			logger.error(String.format(
					"Encountered problem with config file %s. Assuming default configuration. Problem: %s", file,
					e.getMessage()));
		}
		return ConfigFactory.create(PROSECOConfig.class, props);
	}
}
