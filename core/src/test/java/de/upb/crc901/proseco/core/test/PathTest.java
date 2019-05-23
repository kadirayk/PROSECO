package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.crc901.proseco.commons.controller.CannotFixDomainInThisProcessException;
import de.upb.crc901.proseco.commons.controller.GroundingNotSuccessfulForAnyStrategyException;
import de.upb.crc901.proseco.commons.controller.NoStrategyFoundASolutionException;
import de.upb.crc901.proseco.commons.controller.PROSECOSolution;
import de.upb.crc901.proseco.commons.controller.ProcessController;
import de.upb.crc901.proseco.commons.controller.ProcessIdAlreadyExistsException;
import de.upb.crc901.proseco.commons.controller.PrototypeCouldNotBeExtractedException;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.util.FileUtil;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.commons.util.Parser;
import de.upb.crc901.proseco.core.composition.FileBasedConfigurationProcess;

public class PathTest {

	static String processId;
	static PROSECOProcessEnvironment env;
	static String output;
	static String strategy = "Strategy";
	static String file = "file:";

	@BeforeClass
	public static void initialize() throws ProcessIdAlreadyExistsException, InvalidStateTransitionException,
			CannotFixDomainInThisProcessException, IOException, NoStrategyFoundASolutionException,
			PrototypeCouldNotBeExtractedException, GroundingNotSuccessfulForAnyStrategyException {
		ProcessController processController = new FileBasedConfigurationProcess(new File(""));
		processController.createNew(null);
		processController.fixDomain("test");
		env = processController.getProcessEnvironment();
		processId = env.getProcessId();
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
		answers.put("Please select prototype", "test");
		processController.updateInterview(answers);

		PROSECOSolution solution = processController.startComposition(1000);
		processController.chooseAndDeploySolution(solution);
		env = processController.getProcessEnvironment();
		output = FileUtil.readFile("processes/" + processId + "/test.out");
	}

	@Test
	public void testStrategyDirectory() {
		int startStrategy = output.indexOf(strategy);
		int startStrategyFile = output.indexOf(file, startStrategy) + file.length();
		int lineEnd = output.indexOf('\n', startStrategyFile);
		String strategyFile = output.substring(startStrategyFile, lineEnd);
		int fileIndex = strategyFile.indexOf("strategy1") - 1;
		String strategyDir = strategyFile.substring(0, fileIndex);
		assertEquals(env.getStrategyDirectory().getAbsolutePath(), strategyDir);
	}

	@Test
	public void testProcessDirectory() {
		int startStrategy = output.indexOf(strategy);

		int startProcessDir = output.indexOf("param1:", startStrategy) + "param1:".length();
		int lineEnd = output.indexOf('\n', startProcessDir);
		String processDir = output.substring(startProcessDir, lineEnd).trim();
		assertEquals(env.getProcessDirectory().getAbsolutePath(), processDir);
	}

	@Test
	public void testGroundingFile() {
		int startGrounding = output.indexOf("Grounding");
		int startGroundingFile = output.indexOf(file, startGrounding) + file.length();
		int lineEnd = output.indexOf('\n', startGroundingFile);
		String groundingFile = output.substring(startGroundingFile, lineEnd).trim();
		assertEquals(env.groundingExecutable().getAbsolutePath(), groundingFile);
	}

	@Test
	public void testDeploymentFile() {
		int startDeployment = output.indexOf("Deployment");
		int startDeploymentFile = output.indexOf(file, startDeployment) + file.length();
		int lineEnd = output.indexOf('\n', startDeploymentFile);
		String deploymentFile = output.substring(startDeploymentFile, lineEnd).trim();
		assertEquals(env.deploymentExecutable().getAbsolutePath(), deploymentFile);
	}

	@Test
	public void testSearchDirectory() {

		int startStrategy = output.indexOf(strategy);
		int startSearchDir = output.indexOf("param2:", startStrategy) + "param2:".length();
		int lineEnd = output.indexOf('\n', startSearchDir);
		String searchDir = output.substring(startSearchDir, lineEnd - ("//inputs ").length());
		assertTrue(env.getSearchDirectory().getAbsolutePath().contains(searchDir));

	}

}
