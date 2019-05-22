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
public interface PrototypeConfig extends Mutable {

	@Key(ConfigConstants.BENCHMARK_PATH)
	@DefaultValue("5")
	public String getBenchmarkPath();

	@Key(ConfigConstants.STRATEGIES)
	@DefaultValue("strategies")
	public String getNameOfStrategyFolder();

	@Key(ConfigConstants.DISABLED_STRATEGIES)
	@DefaultValue("")
	public String getDisabledStrategies();

	@Key(ConfigConstants.GROUNDING_FOLDER)
	@DefaultValue("")
	public String getNameOfGroundingFolder();

	@Key(ConfigConstants.GROUNDING_EXEC)
	@DefaultValue("grounding")
	public String getGroundingCommand();

	@Key(ConfigConstants.GROUNDING_RESERVEDSECONDS)
	@DefaultValue("5")
	public int getSecondsReservedForGrounding();

	@Key(ConfigConstants.DEPLOYMENT_EXEC)
	@DefaultValue("deployment")
	public String getDeploymentCommand();

	@Key(ConfigConstants.DEPLOYMENT_HOST)
	@DefaultValue("localhost")
	public String getDeploymentHost();

	@Key(ConfigConstants.DEPLOYMENT_PORT_MIN)
	@DefaultValue("8100")
	public int getDeploymentMinPort();

	@Key(ConfigConstants.DEPLOYMENT_PORT_MAX)
	@DefaultValue("8200")
	public int getDeploymentMaxPort();

	@Key(ConfigConstants.DEPLOYMENT_ENTRYPOINT)
	@DefaultValue("")
	public String getDeploymentEntryPoint();

	@Key(ConfigConstants.DEPLOYMENT_RESERVEDSECONDS)
	@DefaultValue("5")
	public int getSecondsReservedForDeployment();

	@Key(ConfigConstants.STRATEGY_RUNNABLE)
	@DefaultValue("run")
	public String getSearchRunnable();

	public static PrototypeConfig get(final PROSECOConfig prosecoConfig, final String prototypeName) {
		return get(new File(prosecoConfig.getDirectoryForDomains() + File.separator + prototypeName + File.separator
				+ "prototype.conf"));
	}

	public static PrototypeConfig get(final String file) {
		return get(new File(file));
	}

	public static PrototypeConfig get(final File file) {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.err.println("Could not find config file " + file + ". Assuming default configuration");
		} catch (IOException e) {
			System.err.println("Encountered problem with config file " + file
					+ ". Assuming default configuration. Problem:" + e.getMessage());
		}

		return ConfigFactory.create(PrototypeConfig.class, props);
	}

	@Key(ConfigConstants.PRE_GROUNDING_HOOK)
	@DefaultValue("analysis")
	public File getHookForPreGrounding();
}
