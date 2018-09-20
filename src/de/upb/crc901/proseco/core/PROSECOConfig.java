package de.upb.crc901.proseco.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.ConfigFactory;
import org.aeonbits.owner.Mutable;

public interface PROSECOConfig extends Mutable, Accessible {
	
	/* phase 1: identify prototype */
	public static final String PROTOTYPES_PATH = "proseco.prototypes_path";
	public static final String EXECUTIONS_PATH = "proseco.executions_path";
	
	/* behavior where to put outputs */
	public static final String OUTPUT_DIR = "proseco.output_directory";
	
	/* where to store log information */
	public static final String SYSTEM_OUT_FILE = "proseco.system_out_file";
	public static final String SYSTEM_ERR_FILE = "proseco.system_err_file";
	public static final String SYSTEM_ALL_FILE = "proseco.system_all_file";
	public static final String SERVICE_LOG_FILE = "proseco.service_log_file";
	
	/* whether or not PROSECO composition should clean up the working directory at the end */
	public static final String FINAL_CLEAN_UP = "proseco.final_clean_up";
	
	
	@Key(PROTOTYPES_PATH)
	@DefaultValue("prototypes")
	public File getPathToPrototypes();
	
	@Key(EXECUTIONS_PATH)
	@DefaultValue("execution")
	public File getExecutionFolder();
	
	@Key(FINAL_CLEAN_UP)
	@DefaultValue("true")
	public boolean isFinalCleanupEnabled();
	
	@Key(OUTPUT_DIR)
	@DefaultValue("out")
	public String getNameOfOutputFolder();
	
	@Key(SERVICE_LOG_FILE)
	@DefaultValue("service_log")
	public String getNameOfServiceLogFile();
	
	@Key(SYSTEM_OUT_FILE)
	@DefaultValue("console.out")
	public String getSystemOutFileName();
	
	@Key(SYSTEM_ERR_FILE)
	@DefaultValue("console.err")
	public String getSystemErrFileName();
	
	@Key(SYSTEM_ALL_FILE)
	@DefaultValue("console.all")
	public String getSystemMergedOutputFileName();
	
	public static PROSECOConfig get(String file) throws FileNotFoundException, IOException {
		return get(new File(file));
	}
	
	public static PROSECOConfig get(File file) throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(file));
		return ConfigFactory.create(PROSECOConfig.class, props);
	}
}
