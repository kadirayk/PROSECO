package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.GlobalConfig;
import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.view.app.model.processstatus.EProcessState;
import de.upb.crc901.proseco.view.app.model.processstatus.ProcessStateProvider;

/**
 *
 * PrototypeBasedComposer realizes the service construction of the selected prototype with the filled out interview data.
 *
 *
 * <br>
 * <br>
 * <img src="doc-files/PrototypeBasedComposer.png">
 *
 */
public class CompositionAlgorithm implements Runnable {

	private static final GlobalConfig GLOBAL_CONFIG = ConfigFactory.create(GlobalConfig.class);

	/* logging */
	private static final Logger logger = LoggerFactory.getLogger(CompositionAlgorithm.class);

	/** Base folder for matching the availability of prototypes */
	private final PROSECOProcessEnvironment executionEnvironment;

	private final int timeoutInSeconds;

	/**
	 * Instantiate a new PrototypeBasedComposer for executing the prototype composition pipeline.
	 *
	 * @param processId
	 *            consists of prototypeName-Id ie. imageClassification-E34A778BF1
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws Exception
	 */
	public CompositionAlgorithm(final PROSECOProcessEnvironment environment, final int timeoutInSeconds) {
		this.executionEnvironment = environment;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public void run() {
		try {
			/* serialize execution environment into the process folder */
			File file = new File(this.executionEnvironment.getProcessDirectory() + File.separator + "proseco.conf");
			Map<String, String> redefinedValues = new HashMap<>();
			PROSECOConfig config = this.executionEnvironment.getProsecoConfig();
			for (String key : config.propertyNames()) {
				String val = "";
				switch (key) {
				case PROSECOConfig.DOMAINS_PATH:
					val = config.getDirectoryForDomains().getAbsolutePath();
					break;
				case PROSECOConfig.PROCESS_PATH:
					val = config.getDirectoryForProcesses().getAbsolutePath();
					break;
				default:
					val = config.getProperty(key);
					break;
				}
				redefinedValues.put(key, val);
			}
			PROSECOConfig rewrittenConfig = ConfigFactory.create(PROSECOConfig.class, redefinedValues);
			rewrittenConfig.store(new FileOutputStream(file), "copy of original proseco config for this execution");

			FileUtils.writeStringToFile(new File(this.executionEnvironment.getProcessDirectory() + File.separator + "process.id"), this.executionEnvironment.getProcessId(), Charset.defaultCharset());

			/* create search folder and the sub-folder for the inputs, and copy the interview resources here */
			FileUtils.forceMkdir(this.executionEnvironment.getSearchDirectory());
			if (this.executionEnvironment.getInterviewResourcesDirectory().exists()) {
				FileUtils.copyDirectory(this.executionEnvironment.getInterviewResourcesDirectory(), this.executionEnvironment.getSearchInputDirectory());
			}

			/* execute hooks that should run prior to configuration */
			this.beforeConfiguration();

			/* invoke strategies */
			int secondsReservedForGrounding = this.executionEnvironment.getPrototypeConfig().getSecondsReservedForGrounding();
			int secondsReservedForDeployment = this.executionEnvironment.getPrototypeConfig().getSecondsReservedForDeployment();
			int timeout = Math.max(1, this.timeoutInSeconds - (secondsReservedForGrounding + secondsReservedForDeployment));
			logger.debug("Create command for executing strategies and execute them with a timeout of {} = max(1, specifiedTimeout - (secondsForGrounding + secondsForDeployment)) = max(1, {} - ({} + {})) ...", timeout, this.timeoutInSeconds,
					secondsReservedForGrounding, secondsReservedForDeployment);
			StrategyExecutor executeStrategiesCommand = new StrategyExecutor(this.executionEnvironment);
			executeStrategiesCommand.execute(timeout * 1000);
			logger.info("Execution of strategies finished!");

			/* execute hooks that should run after configuration */

			/* determine strategy that delivered the best solution */
			Optional<File> winningStrategy = Optional.empty();
			double bestScoreSeen = Double.MAX_VALUE;
			for (final File strategy : this.executionEnvironment.getStrategyDirectory().listFiles()) {
				if (!strategy.isDirectory()) {
					continue;
				}
				final File fValueFile = new File(this.executionEnvironment.getSearchOutputDirectory() + File.separator + strategy.getName() + File.separator + "score");
				if (!fValueFile.exists()) {
					logger.info("score file was not found in file {} for strategy {}", fValueFile.getAbsolutePath(), strategy.getName());
					continue;
				}
				Double parsedValue = Double.parseDouble(FileUtils.readFileToString(fValueFile, Charset.defaultCharset()));
				if (parsedValue < bestScoreSeen) {
					winningStrategy = Optional.of(strategy);
					bestScoreSeen = parsedValue;
				}
				logger.info("Strategy {} reports a solution performance of {}", strategy.getName(), parsedValue);
			}
			if (!winningStrategy.isPresent()) {
				logger.info("None of the strategies has found a solution.");
				return;
			}
			logger.info("Identified {} as a winning strategy with score {}", winningStrategy.get(), bestScoreSeen);

			/* execute grounding routine */
			ProcessStateProvider.setProcessStatus(this.executionEnvironment.getProcessId(), EProcessState.GROUNDING);
			{
				File groundingLog = new File(this.executionEnvironment.getGroundingDirectory() + File.separator + this.executionEnvironment.getProsecoConfig().getNameOfServiceLogFile());
				String[] groundingCommand = new String[4];
				groundingCommand[0] = this.executionEnvironment.groundingExecutable().getAbsolutePath();
				groundingCommand[1] = this.executionEnvironment.getProcessId();
				groundingCommand[2] = this.executionEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + winningStrategy.get().getName();
				groundingCommand[3] = this.executionEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + "final";
				new File(groundingCommand[0]).setExecutable(true);
				final ProcessBuilder pb = new ProcessBuilder(groundingCommand).directory(this.executionEnvironment.getGroundingDirectory());
				// pb.redirectOutput(Redirect.appendTo(groundingLog)).redirectError(Redirect.appendTo(groundingLog));
				pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
				logger.info("Execute grounding command {}. Working directory is set to {}", Arrays.toString(groundingCommand), this.executionEnvironment.getGroundingDirectory());
				if (GLOBAL_CONFIG.debugMode() && GLOBAL_CONFIG.debugDisableGrounding()) {
					logger.warn("Grounding has been disabled for debugging! You can enable it in the GlobalConfig properties.");
				} else {
					pb.start().waitFor();
				}
				logger.info("Grounding completed.");
			}

			/* deploy service. First determine available port. Then execute the deployment. */
			ProcessStateProvider.setProcessStatus(this.executionEnvironment.getProcessId(), EProcessState.DEPLOYMENT);
			String host = this.executionEnvironment.getPrototypeConfig().getDeploymentHost();
			int port = this.executionEnvironment.getPrototypeConfig().getDeploymentMinPort();
			boolean portIsAvailable = true;
			do {
				portIsAvailable = true;
				try (Socket s = new Socket(host, port)) {
					portIsAvailable = false;
					port++;
				} catch (IOException ex) {
				}
			} while (!portIsAvailable);
			String[] deploymentCommand = new String[4];
			deploymentCommand[0] = this.executionEnvironment.deploymentExecutable().getAbsolutePath();
			deploymentCommand[1] = this.executionEnvironment.getProcessId();
			deploymentCommand[2] = host;
			deploymentCommand[3] = "" + port;
			new File(deploymentCommand[0]).setExecutable(true);
			logger.info("Deploying service {} to {}:{}", deploymentCommand[1], deploymentCommand[2], deploymentCommand[3]);
			final ProcessBuilder pb = new ProcessBuilder(deploymentCommand);
			pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
			if (GLOBAL_CONFIG.debugMode() && (GLOBAL_CONFIG.debugDisableDeployment() || GLOBAL_CONFIG.debugDisableGrounding())) {
				if (GLOBAL_CONFIG.debugDisableGrounding()) {
					logger.warn("Deployment has been disabled as grounding routine was disabled for debugging. To enable both check");
				} else {
					logger.warn("Deployment has been disabled for debugging! You can enable it in the GlobalConfig properties.");
				}
			} else {
				pb.start().waitFor();
			}
			logger.info("Deployment completed.");

			/* create handle file */
			FileUtils.writeStringToFile(this.executionEnvironment.getServiceHandle(), "http://" + deploymentCommand[2] + ":" + port + "/" + this.executionEnvironment.getPrototypeConfig().getDeploymentEntryPoint(), Charset.defaultCharset());
			ProcessStateProvider.setProcessStatus(this.executionEnvironment.getProcessId(), EProcessState.DONE);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			/* clean up workspace */
			if (false && this.executionEnvironment.getProsecoConfig().isFinalCleanupEnabled()) {
				logger.info("Clean up execution directory...");
				try {
					FileUtils.deleteDirectory(this.executionEnvironment.getProcessDirectory());
				} catch (final IOException e) {
					e.printStackTrace();
				}
				logger.info("PROSECO has terminated.");
			}
		}

	}

	protected void beforeConfiguration() {
	}

	/**
	 * Extract prototypeName of prototypeName-Id pair
	 *
	 * @param prototypeId
	 * @return
	 */
	private String getPrototypeNameFromProcessId(final String prototypeId) {
		String prototypeName = null;
		if (prototypeId != null) {
			String[] prototypeNameIdPair = prototypeId.split("-");
			if (prototypeNameIdPair.length > 0) {
				prototypeName = prototypeNameIdPair[0];
			}
		}
		return prototypeName;
	}

}
