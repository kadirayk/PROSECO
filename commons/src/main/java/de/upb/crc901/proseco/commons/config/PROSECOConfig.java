package de.upb.crc901.proseco.commons.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

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
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println(
					"Could not find config file " + file.getAbsolutePath() + ". Assuming default configuration");
		} catch (IOException e) {
			System.err.println("Encountered problem with config file " + file
					+ ". Assuming default configuration. Problem:" + e.getMessage());
		}
		return ConfigFactory.create(PROSECOConfig.class, props);
	}
}
