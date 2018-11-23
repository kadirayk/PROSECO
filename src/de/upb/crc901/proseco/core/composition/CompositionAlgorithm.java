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

import de.upb.crc901.proseco.core.PROSECOConfig;

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
	public CompositionAlgorithm(final PROSECOProcessEnvironment environment, int timeoutInSeconds) {
		this.executionEnvironment = environment;
		this.timeoutInSeconds = timeoutInSeconds;
	}

	@Override
	public void run() {
		try {

			/* serialize execution environment into the process folder */
			File file = new File(executionEnvironment.getProcessDirectory() + File.separator + "proseco.conf");
			Map<String, String> redefinedValues = new HashMap<>();
			PROSECOConfig config = executionEnvironment.getProsecoConfig();
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
			FileUtils.writeStringToFile(new File(executionEnvironment.getProcessDirectory() + File.separator + "process.id"), executionEnvironment.getProcessId(), Charset.defaultCharset());

			/* create search folder and the sub-folder for the inputs, and copy the interview resources here */
			FileUtils.forceMkdir(executionEnvironment.getSearchDirectory());
			if(executionEnvironment.getInterviewResourcesDirectory().exists()) {
				FileUtils.copyDirectory(executionEnvironment.getInterviewResourcesDirectory(), executionEnvironment.getSearchInputDirectory());
			}

			/* execute hooks that should run prior to configuration */

			/* invoke strategies */
			StrategyExecutor executeStrategiesCommand = new StrategyExecutor(executionEnvironment);
			executeStrategiesCommand.execute(60 * 1000);
			System.out.println("Execution of strategies finished!");

			/* execute hooks that should run after configuration */

			/* determine strategy that delivered the best solution */
			Optional<File> winningStrategy = Optional.empty();
			double bestScoreSeen = Double.MAX_VALUE;
			for (final File strategy : executionEnvironment.getStrategyDirectory().listFiles()) {
				if (!strategy.isDirectory()) {
					continue;
				}
				final File fValueFile = new File(executionEnvironment.getSearchOutputDirectory() + File.separator + strategy.getName() + File.separator + "score");
				if (!fValueFile.exists()) {
					logger.info("score file was not found in file {} for strategy {}", fValueFile.getAbsolutePath(), strategy.getName());
					continue;
				}
				Double parsedValue = Double.parseDouble(FileUtils.readFileToString(fValueFile, Charset.defaultCharset()));
				if (parsedValue < bestScoreSeen) {
					winningStrategy = Optional.of(strategy);
					bestScoreSeen = parsedValue;
				}
			}
			if (!winningStrategy.isPresent()) {
				logger.info("None of the strategies has found a solution.");
				return;
			}
			logger.info("Identified {} as a winning strategy with score {}", winningStrategy.get(), bestScoreSeen);

			/* execute grounding routine */
			{
				File groundingLog = new File(executionEnvironment.getGroundingDirectory() + File.separator + executionEnvironment.getProsecoConfig().getNameOfServiceLogFile());
				String[] groundingCommand = new String[4];
				groundingCommand[0] = executionEnvironment.getGroundingFile().getAbsolutePath();
				groundingCommand[1] = executionEnvironment.getProcessId();
				groundingCommand[2] = executionEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + winningStrategy.get().getName();
				groundingCommand[3] = executionEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + "final";
				new File(groundingCommand[0]).setExecutable(true);
				final ProcessBuilder pb = new ProcessBuilder(groundingCommand).directory(executionEnvironment.getGroundingDirectory());
				// pb.redirectOutput(Redirect.appendTo(groundingLog)).redirectError(Redirect.appendTo(groundingLog));
				pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
				logger.info("Execute grounding command {}. Working directory is set to {}", Arrays.toString(groundingCommand), executionEnvironment.getGroundingDirectory());
				pb.start().waitFor();
				logger.info("Grounding completed.");
			}

			/* deploy service. First determine available port. Then execute the deployment. */
			String host = executionEnvironment.getPrototypeConfig().getDeploymentHost();
			int port = executionEnvironment.getPrototypeConfig().getDeploymentMinPort();
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
			deploymentCommand[0] = executionEnvironment.getDeploymentFile().getAbsolutePath();
			deploymentCommand[1] = executionEnvironment.getProcessId();
			deploymentCommand[2] = host;
			deploymentCommand[3] = "" + port;
			new File(deploymentCommand[0]).setExecutable(true);
			logger.info("Deploying service {} to {}:{}", deploymentCommand[1], deploymentCommand[2], deploymentCommand[3]);
			final ProcessBuilder pb = new ProcessBuilder(deploymentCommand);
			pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
			pb.start().waitFor();
			logger.info("Deployment completed.");

			/* create handle file */
			FileUtils.writeStringToFile(executionEnvironment.getServiceHandle(), "http://" + deploymentCommand[2] + ":" + port + "/" + executionEnvironment.getPrototypeConfig().getDeploymentEntryPoint(), Charset.defaultCharset());

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			/* clean up workspace */
			if (false && executionEnvironment.getProsecoConfig().isFinalCleanupEnabled()) {
				logger.info("Clean up execution directory...");
				try {
					FileUtils.deleteDirectory(executionEnvironment.getProcessDirectory());
				} catch (final IOException e) {
					e.printStackTrace();
				}
				logger.info("PROSECO has terminated.");
			}
		}

	}

	/**
	 * Extract prototypeName of prototypeName-Id pair
	 * 
	 * @param prototypeId
	 * @return
	 */
	private String getPrototypeNameFromProcessId(String prototypeId) {
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
