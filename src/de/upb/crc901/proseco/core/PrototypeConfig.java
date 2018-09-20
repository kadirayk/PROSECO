package de.upb.crc901.proseco.core;

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
public interface PrototypeConfig extends Mutable {

	/* phase 2: conduct interview */
	public static final String INTERVIEW = "pbc.interview_path";
	public static final String INTERVIEW_RESOURCES = "pbc.interview_resources_path";

	/* phase 3: PROSECO composition phase */

	/* 3a: execution environment for the selected prototype */
	public static final String BENCHMARK_PATH = "pbc.benchmarks_path";
	public static final String STRATEGIES = "pbc.strategies_path";
	public static final String SOURCE = "pbc.source_path";
	public static final String CONFIG = "pbc.config_path";
	public static final String GROUNDING_FOLDER = "pbc.grounding_path";
	public static final String PARAMS = "pbc.params_path";
	public static final String LIBS = "pbc.libs_path";
	public static final String INTERNAL_BENCHMARK_FOLDER = "benchmarks/";

	public static final String STRATEGY_RUNNABLE = "pbc.strategy.runnable";
	public static final String GROUNDING_ROUTINE = "groundingroutine.bat";
	public static final String INIT_CONFIGURATION_EXEC = "initconfiguration.bat";
	public static final String BENCHMARK_SERVICE = "benchmarkService.bat";
	public static final String EXEC_FINAL_TEST = "src/test.bat";

	@Key(BENCHMARK_PATH)
	@DefaultValue("5")
	public String getBenchmarkPath();

	@Key(STRATEGIES)
	@DefaultValue("strategies")
	public String getNameOfStrategyFolder();

	@Key(SOURCE)
	public File getPathToSource();

	@Key(GROUNDING_FOLDER)
	public String getNameOfGroundingFolder();

	@Key(GROUNDING_ROUTINE)
	public String getNameOfGroundingRoutine();

	@Key(PARAMS)
	public File getPathToParams();

	@Key(LIBS)
	@DefaultValue("libs")
	public File getPathToLibs();

	@Key(INTERVIEW)
	@DefaultValue("interview")
	public String getNameOfInterviewFolder();

	@Key(INTERVIEW_RESOURCES)
	@DefaultValue("res")
	public String getNameOfInterviewResourceFolder();

	@Key(STRATEGY_RUNNABLE)
	@DefaultValue("run.sh")
	public String getSearchRunnable();

	public static PrototypeConfig get(PROSECOConfig prosecoConfig, String prototypeName) {
		return get(new File(prosecoConfig.getPathToPrototypes() + File.separator + prototypeName + File.separator + "prototype.conf"));
	}
	
	public static PrototypeConfig get(String file) {
		return get(new File(file));
	}

	public static PrototypeConfig get(File file) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("Could not find config file " + file + ". Assuming default configuration");
		} catch (IOException e) {
			System.err.println("Encountered problem with config file " + file + ". Assuming default configuration. Problem:" + e.getMessage());
		}

		return ConfigFactory.create(PrototypeConfig.class, props);
	}
}
