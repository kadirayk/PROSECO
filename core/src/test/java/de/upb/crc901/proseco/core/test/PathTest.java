package de.upb.crc901.proseco.core.test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.crc901.proseco.commons.controller.DefaultProcessController;
import de.upb.crc901.proseco.commons.controller.ProcessController;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.interview.Question;
import de.upb.crc901.proseco.commons.processstatus.EProcessState;
import de.upb.crc901.proseco.commons.processstatus.ProcessStateProvider;
import de.upb.crc901.proseco.commons.util.FileUtil;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.commons.util.SerializationUtil;
import de.upb.crc901.proseco.core.composition.CompositionAlgorithm;
import de.upb.crc901.proseco.core.test.util.Parser;
import static org.junit.Assert.*;

public class PathTest {

	static String processId;
	static PROSECOProcessEnvironment env;
	static String output;

	@BeforeClass
	public static void initialize() throws Exception {
		ProcessController processController = new DefaultProcessController(new File(""));
		env = processController.createConstructionProcessEnvironment("test");
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = new HashMap<>(fillout.getAnswers());

		Question prototypeQuestion = fillout.getCurrentState().getQuestions().get(0);
		answers.put(prototypeQuestion.getId(), "test");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 1000);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");
	}

	
	@Test
	public void testStrategyDirectory() {
		int startStrategy = output.indexOf("Strategy");
		int startStrategyFile = output.indexOf("file:", startStrategy) + "file:".length();
		int lineEnd = output.indexOf("\n", startStrategyFile);
		String strategyFile = output.substring(startStrategyFile, lineEnd);
		int fileIndex = strategyFile.indexOf("\\strategy1");
		String strategyDir = strategyFile.substring(0, fileIndex);
		assertEquals(env.getStrategyDirectory().getAbsolutePath(), strategyDir);
	}
	
	@Test
	public void testProcessDirectory() {
		int startStrategy = output.indexOf("Strategy");

		int startProcessDir = output.indexOf("param1:", startStrategy) + "param1:".length();
		int lineEnd = output.indexOf("\n", startProcessDir);
		String processDir = output.substring(startProcessDir, lineEnd).trim();
		assertEquals(env.getProcessDirectory().getAbsolutePath(), processDir);
	}
	
	@Test
	public void testGroundingFile() {
		int startGrounding = output.indexOf("Grounding");
		int startGroundingFile = output.indexOf("file:", startGrounding) + "file:".length();
		int lineEnd = output.indexOf("\n", startGroundingFile);
		String groundingFile = output.substring(startGroundingFile, lineEnd).trim();
		assertEquals(env.groundingExecutable().getAbsolutePath(), groundingFile);
	}
	
	@Test
	public void testDeploymentFile() {
		int startDeployment = output.indexOf("Deployment");
		int startDeploymentFile = output.indexOf("file:", startDeployment) + "file:".length();
		int lineEnd = output.indexOf("\n", startDeploymentFile);
		String deploymentFile = output.substring(startDeploymentFile, lineEnd).trim();
		assertEquals(env.deploymentExecutable().getAbsolutePath(), deploymentFile);
	}
	
	@Test
	public void testSearchDirectory() {

		int startStrategy = output.indexOf("Strategy");
		int startSearchDir = output.indexOf("param2:", startStrategy) + "param2:".length();
		int lineEnd = output.indexOf("\n", startSearchDir);
		String searchDir = output.substring(startSearchDir, lineEnd-("//inputs ").length());
		assertEquals(env.getSearchDirectory().getAbsolutePath(), searchDir);
	

	}

}
