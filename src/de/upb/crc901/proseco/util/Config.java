package de.upb.crc901.proseco.util;

import java.io.File;

import de.upb.crc901.proseco.PrototypeProperties;

/**
 * Unified class for configuration values
 * 
 * @author kadirayk
 *
 */
public class Config {

	public static final PrototypeProperties PROPS = new PrototypeProperties("config/PrototypeBasedComposer.conf");

	public static final boolean FINAL_CLEAN_UP = Boolean.parseBoolean(PROPS.getProperty("pbc.final_clean_up"));

	public static final File PROTOTYPES = new File(PROPS.getProperty("pbc.prototypes_path"));
	public static final File EXECUTIONS = new File(PROPS.getProperty("pbc.executions_path"));

	public static final String BENCHMARKS = PROPS.getProperty("pbc.benchmarks_path");
	public static final String STRATEGIES = PROPS.getProperty("pbc.strategies_path");
	public static final String SOURCE = PROPS.getProperty("pbc.source_path");
	public static final String CONFIG = PROPS.getProperty("pbc.config_path");
	public static final String GROUNDING = PROPS.getProperty("pbc.grounding_path");
	public static final String PARAMS = PROPS.getProperty("pbc.params_path");
	public static final String LIBS = PROPS.getProperty("pbc.libs_path");
	public static final String INTERVIEW = PROPS.getProperty("pbc.interview_path");
	public static final String INTERVIEW_RESOURCES = PROPS.getProperty("pbc.interview_resources_path");
	public static final String SYSTEM_OUT_FILE = PROPS.getProperty("pbc.system_out_file");
	public static final String SYSTEM_ERR_FILE = PROPS.getProperty("pbc.system_err_file");
	public static final String PROTOTYPES_PATH = PROPS.getProperty("pbc.prototypes_path");
	public static final String EXECUTIONS_PATH = PROPS.getProperty("pbc.executions_path");
	public static final String INTERVIEW_PATH = PROPS.getProperty("pbc.interview_path");
	public static final String INTERVIEW_RESOUCES_PATH = PROPS.getProperty("pbc.interview_resources_path");
	
	
	public static final String INTERNAL_BENCHMARK_FOLDER = "benchmarks/";

	public static final String DATAFILE_NAME = "data.zip";
	public static final String STRATEGY_RUNNABLE = "run.bat";
	public static final String GROUNDING_ROUTINE = "groundingroutine.bat";
	public static final String INIT_CONFIGURATION_EXEC = "initconfiguration.bat";
	public static final String BENCHMARK_SERVICE = "benchmarkService.bat";
	public static final String EXEC_FINAL_TEST = "src/test.bat";

	public static final String OUTPUT_DIR = PROPS.getProperty("pbc.output_directory");

}
