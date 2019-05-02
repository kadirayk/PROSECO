package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

public class MultipleStrategiesAllSuccessful {
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
		answers.put(prototypeQuestion.getId(), "test1");

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
	public void testWinningStrategy() {
		int startGrounding = output.indexOf("Grounding");
		int startWinningStrategyDir = output.indexOf("param2:", startGrounding) + "param2:".length();
		int lineEnd = output.indexOf("\n", startWinningStrategyDir);
		String winningStrategyDir = output.substring(startWinningStrategyDir, lineEnd).trim();
		assertTrue(winningStrategyDir.endsWith("strategy3"));
	}

	@Test
	public void testProcessStatusDone() {
		String status = FileUtil.readFile("processes/" + processId + "/process.status");
		assertEquals("done", status);
	}
}
