package de.upb.crc901.proseco.commons.config;

import java.io.File;

import org.aeonbits.owner.Config;

/**
 * This config interface provides basic constants for the PROSECO environment.
 *
 * @author wever
 */
public interface GlobalConfig extends Config {

	/**
	 * @return A flag stating whether PROSECO is executed in DEBUG mode.
	 */
	@Key(ConfigConstants.K_DEBUG_MODE)
	@DefaultValue("false")
	public boolean debugMode();

	@Key(ConfigConstants.K_REDIRECT_PROCESS_OUTPUTS)
	@DefaultValue("false")
	public boolean redirectProcessOutputs();

	@Key(ConfigConstants.K_DISABLE_DEPLOYMENT)
	@DefaultValue("true")
	public boolean debugDisableDeployment();

	@Key(ConfigConstants.K_DISABLE_GROUNDING)
	@DefaultValue("true")
	public boolean debugDisableGrounding();

	/**
	 * @return The file of the proseco config.
	 */
	@Key(ConfigConstants.K_PROSECO_CONFIG_FILE)
	@DefaultValue("res/proseco.conf")
	public File prosecoConfigFile();

	/**
	 * @return File extension for script files on Windows systems.
	 */
	@Key(ConfigConstants.K_SCRIPT_EXTENSION_WIN)
	@DefaultValue(".bat")
	public String scriptExtensionWindows();

	/**
	 * @return File extension for script files on non-Windows systems.
	 */
	@Key(ConfigConstants.K_SCRIPT_EXTENSION_NOWIN)
	@DefaultValue(".sh")
	public String scriptExtensionNonWindows();

	@Key(ConfigConstants.K_PROCESS_CONFIG_FILENAME)
	@DefaultValue("process.json")
	public String processConfigFilename();

}
