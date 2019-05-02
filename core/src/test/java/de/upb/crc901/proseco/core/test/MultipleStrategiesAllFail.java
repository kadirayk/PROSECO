package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

public class MultipleStrategiesAllFail {
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
		answers.put(prototypeQuestion.getId(), "test2");

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
	 * No winning strategy, process should not move to grounding step
	 */
	@Test
	public void testGroundingNotExists() {
		assertFalse(output.contains("Grounding"));
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
