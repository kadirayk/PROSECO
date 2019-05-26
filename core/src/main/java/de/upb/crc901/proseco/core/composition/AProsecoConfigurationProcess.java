package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.upb.crc901.proseco.commons.config.GlobalConfig;
import de.upb.crc901.proseco.commons.controller.CannotFixDomainInThisProcessException;
import de.upb.crc901.proseco.commons.controller.DefaultPrototypeExtractor;
import de.upb.crc901.proseco.commons.controller.GroundingNotSuccessfulForAnyStrategyException;
import de.upb.crc901.proseco.commons.controller.NoStrategyFoundASolutionException;
import de.upb.crc901.proseco.commons.controller.PROSECOSolution;
import de.upb.crc901.proseco.commons.controller.ProcessController;
import de.upb.crc901.proseco.commons.controller.PrototypeCouldNotBeExtractedException;
import de.upb.crc901.proseco.commons.processstatus.EProcessState;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.processstatus.ProcessStateProvider;
import de.upb.crc901.proseco.commons.processstatus.ProcessStateTransitionController;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;

public abstract class AProsecoConfigurationProcess implements ProcessController {

	private EProcessState processState;
	protected String processId;
	protected PROSECOProcessEnvironment processEnvironment;
	protected int timeoutInSeconds;
	protected static final Logger logger = LoggerFactory.getLogger(AProsecoConfigurationProcess.class);
	private static final GlobalConfig GLOBAL_CONFIG = ConfigFactory.create(GlobalConfig.class);
	private String domain;
	protected String prototype;
	protected Map<String, String> answers;
	protected PROSECOSolution solution;

	public AProsecoConfigurationProcess() {
		processState = EProcessState.INIT;
	}

	@Override
	public String getProcessId() {
		return processId;
	}

	protected void setProcessId(String processId) {
		this.processId = processId;
	}

	@Override
	public void fixDomain(String domain) throws CannotFixDomainInThisProcessException, InvalidStateTransitionException {
		this.domain = domain;
		updateProcessState(EProcessState.DOMAIN_DEFINITION);

	}

	protected void extractPrototype() throws PrototypeCouldNotBeExtractedException, InvalidStateTransitionException {
		DefaultPrototypeExtractor prototypeExtractor = new DefaultPrototypeExtractor();
		prototype = prototypeExtractor.getPrototype(domain, answers);
		updateProcessState(EProcessState.PROTOTYPE_EXTRACTED);
	}

	@Override
	public PROSECOSolution startComposition(int timeoutInSeconds) throws NoStrategyFoundASolutionException, InvalidStateTransitionException, PrototypeCouldNotBeExtractedException {
		updateProcessState(EProcessState.COMPOSITION);

		extractPrototype();

		int secondsReservedForGrounding = this.processEnvironment.getPrototypeConfig().getSecondsReservedForGrounding();
		int secondsReservedForDeployment = this.processEnvironment.getPrototypeConfig().getSecondsReservedForDeployment();
		int timeout = Math.max(1, this.timeoutInSeconds - (secondsReservedForGrounding + secondsReservedForDeployment));
		logger.debug("Create command for executing strategies and execute them with a timeout of {} = max(1, specifiedTimeout - (secondsForGrounding + secondsForDeployment)) = max(1, {} - ({} + {})) ...", timeout, this.timeoutInSeconds,
				secondsReservedForGrounding, secondsReservedForDeployment);
		StrategyExecutor executeStrategiesCommand = new StrategyExecutor(this.processEnvironment);
		try {
			updateProcessState(EProcessState.STRATEGY_CHOSEN);
			executeStrategiesCommand.execute(timeout * 1000);
		} catch (IOException | InterruptedException e1) {
			logger.error(e1.getMessage());
			Thread.currentThread().interrupt();
		}
		logger.info("Execution of strategies finished!");

		/* execute hooks that should run after configuration */

		/* determine strategy that delivered the best solution */
		Optional<File> winningStrategy = Optional.empty();
		double bestScoreSeen = Double.MAX_VALUE;
		for (final File strategy : this.processEnvironment.getStrategyDirectory().listFiles()) {
			if (!strategy.isDirectory()) {
				continue;
			}
			final File fValueFile = new File(this.processEnvironment.getSearchOutputDirectory() + File.separator + strategy.getName() + File.separator + "score");
			if (!fValueFile.exists()) {
				logger.info("score file was not found in file {} for strategy {}", fValueFile.getAbsolutePath(), strategy.getName());
				continue;
			}
			Double parsedValue = Double.MAX_VALUE;
			try {
				parsedValue = Double.parseDouble(FileUtils.readFileToString(fValueFile, Charset.defaultCharset()));
			} catch (NumberFormatException | IOException e) {
				logger.error(e.getMessage());
			}
			if (parsedValue < bestScoreSeen) {
				winningStrategy = Optional.of(strategy);
				bestScoreSeen = parsedValue;
			}
			logger.info("Strategy {} reports a solution performance of {}", strategy.getName(), parsedValue);
		}
		if (!winningStrategy.isPresent()) {
			logger.info("None of the strategies has found a solution.");
			throw new NoStrategyFoundASolutionException();
		}
		logger.info("Identified {} as a winning strategy with score {}", winningStrategy.get(), bestScoreSeen);
		PROSECOSolution prosecoSolution = new PROSECOSolution();
		prosecoSolution.setProcessId(processId);
		prosecoSolution.setWinningScore(bestScoreSeen);
		prosecoSolution.setWinningStrategyFolder(winningStrategy.get());
		this.solution = prosecoSolution;
		return prosecoSolution;
	}

	public PROSECOSolution getSolution() {
		return solution;
	}

	@Override
	public void chooseAndDeploySolution(PROSECOSolution solution) throws InvalidStateTransitionException, GroundingNotSuccessfulForAnyStrategyException {
		updateProcessState(EProcessState.GROUNDING);
		if (solution == null) {
			if (this.solution != null) {
				solution = this.solution;
			} else {
				return;
			}
		}
		try {
			ProcessStateProvider.setProcessStatus(this.processEnvironment.getProcessId(), EProcessState.GROUNDING);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		handleGrounding(solution);

		/*
		 * deploy service. First determine available port. Then execute the deployment.
		 */
		handleDeployment();
		updateProcessState(EProcessState.DONE);
		ProcessStateProvider.setProcessStatus(this.processEnvironment.getProcessId(), EProcessState.DONE);

	}

	private void handleDeployment() throws InvalidStateTransitionException {
		updateProcessState(EProcessState.DEPLOYMENT);
		ProcessStateProvider.setProcessStatus(this.processEnvironment.getProcessId(), EProcessState.DEPLOYMENT);
		String host = this.processEnvironment.getPrototypeConfig().getDeploymentHost();
		int port = this.processEnvironment.getPrototypeConfig().getDeploymentMinPort();
		boolean portIsAvailable = true;
		do {
			portIsAvailable = true;
			try (Socket s = new Socket(host, port)) {
				portIsAvailable = false;
				port++;
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			}
		} while (!portIsAvailable);
		String[] deploymentCommand = new String[4];
		deploymentCommand[0] = this.processEnvironment.deploymentExecutable().getAbsolutePath();
		deploymentCommand[1] = this.processEnvironment.getProcessId();
		deploymentCommand[2] = host;
		deploymentCommand[3] = "" + port;
		if (!new File(deploymentCommand[0]).setExecutable(true)) {
			logger.error("cannot set deploymentCommand as executable");
		}
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
			try {
				pb.start().waitFor();
			} catch (InterruptedException | IOException e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
		}
		logger.info("Deployment completed.");

		/* create handle file */
		try {
			FileUtils.writeStringToFile(this.processEnvironment.getServiceHandle(), "http://" + deploymentCommand[2] + ":" + port + "/" + this.processEnvironment.getPrototypeConfig().getDeploymentEntryPoint(), Charset.defaultCharset());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	private void handleGrounding(PROSECOSolution solution) throws GroundingNotSuccessfulForAnyStrategyException {
		int groundingStatus = 0;
		String[] groundingCommand = new String[4];
		groundingCommand[0] = this.processEnvironment.groundingExecutable().getAbsolutePath();
		groundingCommand[1] = this.processEnvironment.getProcessId();
		groundingCommand[2] = this.processEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + solution.getWinningStrategyFolder().getName();
		groundingCommand[3] = this.processEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + "final";
		if (!new File(groundingCommand[0]).setExecutable(true)) {
			logger.error("cannot set groundingCommand as executable");
		}
		final ProcessBuilder pb = new ProcessBuilder(groundingCommand).directory(this.processEnvironment.getGroundingDirectory());
		pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
		if (logger.isInfoEnabled()) {
			logger.info("Execute grounding command {}. Working directory is set to {}", Arrays.toString(groundingCommand), this.processEnvironment.getGroundingDirectory());
		}
		if (GLOBAL_CONFIG.debugMode() && GLOBAL_CONFIG.debugDisableGrounding()) {
			logger.warn("Grounding has been disabled for debugging! You can enable it in the GlobalConfig properties.");
		} else {
			try {
				Process p = pb.start();
				p.waitFor();
				groundingStatus = p.exitValue();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		if (groundingStatus != 0) {
			try {
				executeGroundingForBackupStrategy(solution.getWinningScore());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		logger.info("Grounding completed.");
	}

	private void executeGroundingForBackupStrategy(double bestScoreSeen) throws GroundingNotSuccessfulForAnyStrategyException, IOException {
		Entry<Double, File> secondBestStrategy = findSecondBestStrategy(bestScoreSeen);
		if (secondBestStrategy == null) {
			logger.error("Grounding did not succeed for any of the strategies");
			throw new GroundingNotSuccessfulForAnyStrategyException();
		}
		bestScoreSeen = secondBestStrategy.getKey();
		handleGroundingForBackup(bestScoreSeen, secondBestStrategy);
	}

	private void handleGroundingForBackup(double bestScoreSeen, Entry<Double, File> secondBestStrategy) throws IOException, GroundingNotSuccessfulForAnyStrategyException {
		int groundingStatus = 0;
		String[] groundingCommand = new String[4];
		groundingCommand[0] = this.processEnvironment.groundingExecutable().getAbsolutePath();
		groundingCommand[1] = this.processEnvironment.getProcessId();
		groundingCommand[2] = this.processEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + secondBestStrategy.getValue().getName();
		groundingCommand[3] = this.processEnvironment.getSearchOutputDirectory().getAbsolutePath() + File.separator + "final";
		if (!new File(groundingCommand[0]).setExecutable(true)) {
			logger.error("cannot set groundingCommand as executable");
		}
		final ProcessBuilder pb = new ProcessBuilder(groundingCommand).directory(this.processEnvironment.getGroundingDirectory());
		pb.redirectOutput(Redirect.INHERIT).redirectError(Redirect.INHERIT);
		if (logger.isInfoEnabled()) {
			logger.info("Execute grounding command {}. Working directory is set to {}", Arrays.toString(groundingCommand), this.processEnvironment.getGroundingDirectory());
		}
		if (GLOBAL_CONFIG.debugMode() && GLOBAL_CONFIG.debugDisableGrounding()) {
			logger.warn("Grounding has been disabled for debugging! You can enable it in the GlobalConfig properties.");
		} else {
			Process p = pb.start();
			try {
				p.waitFor();
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
				Thread.currentThread().interrupt();
			}
			groundingStatus = p.exitValue();
		}
		if (groundingStatus != 0) {
			executeGroundingForBackupStrategy(bestScoreSeen);

		}
		logger.info("Grounding completed.");
	}

	private Entry<Double, File> findSecondBestStrategy(Double bestScore) {
		TreeMap<Double, File> strategiesByScore = new TreeMap<>(Collections.reverseOrder());
		Entry<Double, File> secondBestStrategy = null;
		for (final File strategy : this.processEnvironment.getStrategyDirectory().listFiles()) {
			if (!strategy.isDirectory()) {
				continue;
			}
			final File fValueFile = new File(this.processEnvironment.getSearchOutputDirectory() + File.separator + strategy.getName() + File.separator + "score");
			if (!fValueFile.exists()) {
				logger.info("score file was not found in file {} for strategy {}", fValueFile.getAbsolutePath(), strategy.getName());
				continue;
			}
			Double parsedValue = Double.MAX_VALUE;
			try {
				parsedValue = Double.parseDouble(FileUtils.readFileToString(fValueFile, Charset.defaultCharset()));
			} catch (NumberFormatException | IOException e) {
				logger.error(e.getMessage());
			}
			strategiesByScore.put(parsedValue, strategy);
		}
		Entry<Double, File> secondBest = strategiesByScore.lowerEntry(bestScore);
		if (secondBest == null) {
			return null;
		} else {
			secondBestStrategy = strategiesByScore.lowerEntry(bestScore);
		}

		return secondBestStrategy;
	}

	protected void updateProcessState(EProcessState newState) throws InvalidStateTransitionException {
		processState = ProcessStateTransitionController.moveToNextState(processState, newState);
	}

	@Override
	public EProcessState getProcessState() {
		return this.processState;
	}

}
