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

public class MultipleStrategiesAllSuccessful {
	static String processId;
	static PROSECOProcessEnvironment env;
	static String output;

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
		answers.put("Please select prototype", "test1");
		processController.updateInterview(answers);

		PROSECOSolution solution = processController.startComposition(1000);
		processController.chooseAndDeploySolution(solution);

		output = FileUtil.readFile("processes/" + processId + "/test.out");
	}

	@Test
	public void testWinningStrategy() {
		int startGrounding = output.indexOf("Grounding");
		int startWinningStrategyDir = output.indexOf("param2:", startGrounding) + "param2:".length();
		int lineEnd = output.indexOf('\n', startWinningStrategyDir);
		String winningStrategyDir = output.substring(startWinningStrategyDir, lineEnd).trim();
		assertTrue(winningStrategyDir.endsWith("strategy3"));
	}

	@Test
	public void testProcessStatusDone() {
		String status = FileUtil.readFile("processes/" + processId + "/process.status");
		assertEquals("done", status);
	}
}
