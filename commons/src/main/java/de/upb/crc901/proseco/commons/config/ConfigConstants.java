package de.upb.crc901.proseco.commons.config;

public class ConfigConstants {

	private ConfigConstants() {
	}

	/*
	 *  DomainConfig 
	 *  */
	public static final String INTERVIEW = "domain.interview.path";
	public static final String INTERVIEW_STATE_FILE = "domain.interview.statefile";
	public static final String INTERVIEW_RESOURCES = "domain.interview.resources.path";
	public static final String PROTOTYPE_FOLDER = "domain.prototypes";

	
	/* 
	 * GlobalConfig 
	 * */
	public static final String K_DEBUG_MODE = "proseco.debug.mode";
	public static final String K_REDIRECT_PROCESS_OUTPUTS = "proseco.debug.redirectoutputs";
	public static final String K_DISABLE_GROUNDING = "proseco.debug.disableGrounding";
	public static final String K_DISABLE_DEPLOYMENT = "proseco.debug.disableDeployment";
	public static final String K_PROSECO_CONFIG_FILE = "proseco.config_file";
	public static final String K_SCRIPT_EXTENSION_WIN = "proseco.script.extension.windows";
	public static final String K_SCRIPT_EXTENSION_NOWIN = "proseco.script.extension.nonwindows";
	public static final String K_PROCESS_CONFIG_FILENAME = "proseco.process.config_filename";

	/* 
	 * PROSECOConfig 
	 * */
	/* phase 1: identify prototype */
	public static final String DOMAINS_PATH = "proseco.domains";
	public static final String PROCESS_PATH = "proseco.processes";
	
	/* behavior where to put outputs */
	public static final String OUTPUT_DIR = "proseco.output_directory";
	
	/* where to store log information */
	public static final String SYSTEM_OUT_FILE = "proseco.system_out_file";
	public static final String SYSTEM_ERR_FILE = "proseco.system_err_file";
	public static final String SYSTEM_ALL_FILE = "proseco.system_all_file";
	public static final String SERVICE_LOG_FILE = "proseco.service_log_file";
	
	/* whether or not PROSECO composition should clean up the working directory at the end */
	public static final String FINAL_CLEAN_UP = "proseco.final_clean_up";
	
	
	
	/*
	 * PrototypeConfig
	 */
	/* search */
	public static final String STRATEGIES = "pbc.strategies_path";
	public static final String STRATEGY_RUNNABLE = "pbc.strategy.runnable";
	public static final String DISABLED_STRATEGIES = "proseco.disabled.strategies";

	/* pre-grounding filter */
	public static final String PRE_GROUNDING_HOOK = "pbc.hook.preground";

	/* grounding */
	public static final String GROUNDING_FOLDER = "proseco.grounding.folder";
	public static final String GROUNDING_EXEC = "proseco.grounding.executable";
	public static final String GROUNDING_RESERVEDSECONDS = "proseco.grounding.reservedseconds";

	/* deployment */
	public static final String DEPLOYMENT_EXEC = "proseco.deployment.executable";
	public static final String DEPLOYMENT_HOST = "proseco.deployment.host";
	public static final String DEPLOYMENT_PORT_MIN = "proseco.deployment.minport";
	public static final String DEPLOYMENT_PORT_MAX = "proseco.deployment.maxport";
	public static final String DEPLOYMENT_ENTRYPOINT = "proseco.deployment.entrypoint";
	public static final String DEPLOYMENT_RESERVEDSECONDS = "proseco.deployment.reservedseconds";

	/* benchmarking */
	public static final String BENCHMARK_SERVICE = "benchmarkService.bat";
	public static final String BENCHMARK_PATH = "pbc.benchmarks_path";
	public static final String INTERNAL_BENCHMARK_FOLDER = "benchmarks/";

	public static final String EXEC_FINAL_TEST = "src/test.bat";
	
}

