package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

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

public class MultipleStrategiesFailAtGrounding {
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
		answers.put(prototypeQuestion.getId(), "test4");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 1000);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");
	}

	/**
	 * Al strategies fail recursively at grounding step
	 */
	@Test
	public void testWinningAndBackupStrategy() {
		int startGrounding = output.indexOf("Grounding");
		int startWinningStrategyDir = output.indexOf("param2:", startGrounding) + "param2:".length();
		int lineEnd = output.indexOf("\n", startWinningStrategyDir);
		String winningStrategy = output.substring(startWinningStrategyDir, lineEnd).trim();
		
		
		startGrounding = output.indexOf("Grounding", lineEnd);
		startWinningStrategyDir = output.indexOf("param2:", startGrounding) + "param2:".length();
		lineEnd = output.indexOf("\n", startWinningStrategyDir);
		String backupStrategy1 = output.substring(startWinningStrategyDir, lineEnd).trim();
		
		startGrounding = output.indexOf("Grounding", lineEnd);
		startWinningStrategyDir = output.indexOf("param2:", startGrounding) + "param2:".length();
		lineEnd = output.indexOf("\n", startWinningStrategyDir);
		String backupStrategy2 = output.substring(startWinningStrategyDir, lineEnd).trim();
		
		assertTrue(winningStrategy.endsWith("strategy2")); // first attempt of grounding with strategy2
		assertTrue(backupStrategy1.endsWith("strategy1")); // grounding with backup strategy1
		assertTrue(backupStrategy2.endsWith("strategy3")); // grounding with backup strategy3
		
	}
	/**
	 * Process status should not be done
	 */
	@Test
	public void testProcessStatusNotDone() {
		String status = FileUtil.readFile("processes/" + processId + "/process.status");
		assertNotEquals("done", status);
	}
}
