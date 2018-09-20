package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aeonbits.owner.ConfigCache;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
	 * @param processId consists of prototypeName-Id ie. imageClassification-E34A778BF1
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
			Map<String,String> redefinedValues = new HashMap<>();
			PROSECOConfig config = executionEnvironment.getProsecoConfig();
			for (String key : config.propertyNames()) {
				String val = "";
				switch (key) {
				case PROSECOConfig.PROTOTYPES_PATH:
					val = config.getPathToPrototypes().getAbsolutePath();
					break;
				case PROSECOConfig.EXECUTIONS_PATH:
					val = config.getExecutionFolder().getAbsolutePath();
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
			
			/* execute hooks that should run prior to configuration */

			/* invoke strategies */
			StrategyExecutor executeStrategiesCommand = new StrategyExecutor(executionEnvironment);
			executeStrategiesCommand.execute();
			List<Process> strategyProcessList = executeStrategiesCommand.getStrategyProcessList();
			
			/* sleep for the predefined timeout the strategies have time to come up with a solution. Then kill all the search processes if not already terminated. */
			Thread.sleep(timeoutInSeconds * 1000);
			for (final Process p : strategyProcessList) {
				if (p.isAlive()) {
					logger.info("Process {} has not finished within the given timeout; enforcing its termination", p);
					p.destroyForcibly();
				}
			}

			/* execute hooks that should run after configuration */

			/* determine strategy that delivered the best solution */
			Optional<File> winningStrategy = Optional.empty();
			double bestScoreSeen = Double.MAX_VALUE;
			for (final File strategy : executionEnvironment.getStrategyDirectory().listFiles()) {
				if (!strategy.isDirectory()) {
					continue;
				}
				final File fValueFile = new File(strategy.getAbsolutePath() + File.separator + executionEnvironment.getProsecoConfig().getNameOfOutputFolder() + File.separator + "score");
				if (!fValueFile.exists()) {
					logger.info("score file was not found for strategy; ignoring it: {}", fValueFile.getAbsolutePath());
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

			/* move all placeholder files from the selected solution to the grounding folder */
			for (final File strategyFile : new File(winningStrategy.get() + File.separator + executionEnvironment.getProsecoConfig().getNameOfOutputFolder()).listFiles()) {
				if (strategyFile.isFile()) {
					final File groundingFolderFile = new File(executionEnvironment.getGroundingDirectory() + File.separator + strategyFile.getName());
					FileUtils.copyFile(strategyFile, groundingFolderFile);
				}
			}

			/* execute grounding routine */
			File groundingLog = new File(executionEnvironment.getGroundingDirectory() + File.separator + executionEnvironment.getProsecoConfig().getNameOfServiceLogFile());
			final ProcessBuilder pb = new ProcessBuilder(executionEnvironment.getGroundingRoutine().getAbsolutePath()).redirectOutput(Redirect.appendTo(groundingLog))
					.redirectError(Redirect.appendTo(groundingLog));
			logger.info("Execute grounding process...");
			pb.start().waitFor();
			logger.info("Grounding completed.");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			/* clean up workspace */
			if (executionEnvironment.getProsecoConfig().isFinalCleanupEnabled()) {
				logger.info("Clean up execution directory...");
				try {
					FileUtils.deleteDirectory(executionEnvironment.getBenchmarksDirectory());
					FileUtils.deleteDirectory(executionEnvironment.getGroundingDirectory());
					FileUtils.deleteDirectory(executionEnvironment.getParamsDirectory());
					FileUtils.deleteDirectory(executionEnvironment.getStrategyDirectory());
					FileUtils.deleteDirectory(executionEnvironment.getLibsDirectory());
					FileUtils.deleteDirectory(executionEnvironment.getInterviewStateDirectory());
					final String[] filesInMainDir = { "GroundingRoutine.jar", "InitConfiguration.jar", "initconfiguration.bat", "groundingroutine.bat",
							"src/contTrainingInstances.serialized", "src/testInstances.serialized", "src/compile.bat", "src/train.bat" };
					for (final String filename : filesInMainDir) {
						Files.delete(new File(executionEnvironment.getExecutionDirectory() + File.separator + filename).toPath());
					}
				} catch (final IOException e) {
					e.printStackTrace();
				}
				for (final File placeholderFile : executionEnvironment.getSourceDirectory().listFiles()) {
					if (placeholderFile.isFile() && FilenameUtils.getExtension(placeholderFile.getAbsolutePath()).equals("ph")) {
						try {
							Files.delete(placeholderFile.toPath());
						} catch (final IOException e) {
							System.out.println("Could not delete placeholder file : " + placeholderFile.getAbsolutePath());
							e.printStackTrace();
						}
					}
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
