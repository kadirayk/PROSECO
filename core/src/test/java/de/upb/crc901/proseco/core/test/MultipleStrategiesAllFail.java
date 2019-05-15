package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import de.upb.crc901.proseco.commons.controller.NoStrategyFoundASolutionException;
import de.upb.crc901.proseco.commons.controller.ProcessController;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.composition.FileBasedConfigurationProcess;
import de.upb.crc901.proseco.core.test.util.Parser;

public class MultipleStrategiesAllFail {
	static String processId;
	static PROSECOProcessEnvironment env;
	static String output;
	static ProcessController processController;

	@BeforeClass
	public static void initialize() throws Exception {
		processController = new FileBasedConfigurationProcess(new File(""), 1000);
		processController.createNew(null);
		processController.fixDomain("test");
		env = processController.getProcessEnvironment();
		processId = env.getProcessId();
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
		answers.put("Please select prototype", "test2");
		processController.updateInterview(answers);
	}

	/**
	 * No winning strategy, process should not move to grounding step
	 */
	@Test(expected = NoStrategyFoundASolutionException.class)
	public void testGroundingNotExists() throws Exception {
		processController.startComposition();
	}

	/**
	 * Process status should not be done
	 */
	@Test
	public void testProcessStatusNotDone() {
		assertNotEquals("done", processController.getProcessState());
	}
}
