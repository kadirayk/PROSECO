package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimeoutTest {

	static String processId;
	static PROSECOProcessEnvironment env;
	static String output;

	@Test
	public void test1Timeout10sec() throws Exception {
		ProcessController processController = new DefaultProcessController(new File(""));
		env = processController.createConstructionProcessEnvironment("test");
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = new HashMap<>(fillout.getAnswers());

		Question prototypeQuestion = fillout.getCurrentState().getQuestions().get(0);
		answers.put(prototypeQuestion.getId(), "test");

		Question timeoutQuestion = fillout.getInterview().getQuestionByPath("timeout.timeout");
		answers.put(timeoutQuestion.getId(), "20");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 20);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");

		int timeout = getTimeOutFromOutput();
		assertTrue(timeout < 20);
	}

	@Test
	public void test2Timeout1min() throws Exception {
		ProcessController processController = new DefaultProcessController(new File(""));
		env = processController.createConstructionProcessEnvironment("test");
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = new HashMap<>(fillout.getAnswers());

		Question prototypeQuestion = fillout.getCurrentState().getQuestions().get(0);
		answers.put(prototypeQuestion.getId(), "test");

		Question timeoutQuestion = fillout.getInterview().getQuestionByPath("timeout.timeout");
		answers.put(timeoutQuestion.getId(), "60");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 60);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");

		int timeout = getTimeOutFromOutput();
		assertTrue(timeout < 60);
	}

	@Test
	public void test3Timeout10min() throws Exception {
		ProcessController processController = new DefaultProcessController(new File(""));
		env = processController.createConstructionProcessEnvironment("test");
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = new HashMap<>(fillout.getAnswers());

		Question prototypeQuestion = fillout.getCurrentState().getQuestions().get(0);
		answers.put(prototypeQuestion.getId(), "test");

		Question timeoutQuestion = fillout.getInterview().getQuestionByPath("timeout.timeout");
		answers.put(timeoutQuestion.getId(), "600");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 600);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");

		int timeout = getTimeOutFromOutput();
		assertTrue(timeout < 600);
	}

	@Test
	public void test4StrategyNotTerminatingInTime() throws Exception {
		ProcessController processController = new DefaultProcessController(new File(""));
		env = processController.createConstructionProcessEnvironment("test");
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = new HashMap<>(fillout.getAnswers());

		Question prototypeQuestion = fillout.getCurrentState().getQuestions().get(0);
		answers.put(prototypeQuestion.getId(), "test");

		Question timeoutQuestion = fillout.getInterview().getQuestionByPath("timeout.timeout");
		answers.put(timeoutQuestion.getId(), "20");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		changeTimeoutInStrategy("21");

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 20);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");

		// Strategy will be killed when it exceeds timeout
		// Grounding and Deployment steps will not happen
		assertTrue(output.contains("Strategy"));
		assertTrue(!output.contains("Grounding"));
		assertTrue(!output.contains("Deployment"));
		revertStrategyFile();

	}

	@Test
	public void test5StrategyTerminatingInTime() throws Exception {
		ProcessController processController = new DefaultProcessController(new File(""));
		env = processController.createConstructionProcessEnvironment("test");
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = new HashMap<>(fillout.getAnswers());

		Question prototypeQuestion = fillout.getCurrentState().getQuestions().get(0);
		answers.put(prototypeQuestion.getId(), "test");

		Question timeoutQuestion = fillout.getInterview().getQuestionByPath("timeout.timeout");
		answers.put(timeoutQuestion.getId(), "20");

		fillout = new InterviewFillout(fillout.getInterview(), answers);

		SerializationUtil.writeAsJSON(env.getInterviewStateFile(), fillout);
		ProcessStateProvider.setProcessStatus(env.getProcessId(), EProcessState.INTERVIEW);
		processId = env.getProcessId();
		env = processController.getConstructionProcessEnvironment(processId);

		CompositionAlgorithm algorithm = new CompositionAlgorithm(env, 20);
		algorithm.run();
		output = FileUtil.readFile("processes/" + processId + "/test.out");

		// All the steps should be executed when strategy comleted in time
		assertTrue(output.contains("Strategy"));
		assertTrue(output.contains("Grounding"));
		assertTrue(output.contains("Deployment"));

	}

	private void changeTimeoutInStrategy(String timeoutInSeconds) {
		String strategyExecutableBat = env.getStrategyDirectory().getAbsolutePath() + "/strategy1/run.bat";
		String strategyExecutablesh = env.getStrategyDirectory().getAbsolutePath() + "/strategy1/run.sh";
		changeTimeOutInStrategyBat(strategyExecutableBat, timeoutInSeconds);
		changeTimeOutInStrategySh(strategyExecutablesh, timeoutInSeconds);
	}
	
	private void revertStrategyFile() {
		String strategyExecutableBat = env.getStrategyDirectory().getAbsolutePath() + "/strategy1/run.bat";
		StringBuilder str = new StringBuilder();
		str.append("echo Strategy >> %1/test.out\n");
		str.append("echo file:%~dp0%~nx0 >> %1/test.out\n");
		str.append("echo param1:%1 >> %1/test.out\n");
		str.append("echo param2:%2 >> %1/test.out\n");
		str.append("echo param3:%3 >> %1/test.out\n");
		str.append("echo param4:%4 >> %1/test.out\n");
		str.append("echo 1.0 > %3/score");
		FileUtil.writeToFile(strategyExecutableBat, str.toString());
		
		String strategyExecutablesh = env.getStrategyDirectory().getAbsolutePath() + "/strategy1/run.sh";
		str = new StringBuilder();
		str.append("echo Strategy >> $1/test.out\n");
		str.append("echo file:$0 >> $1/test.out\n");
		str.append("echo param1:$1 >> $1/test.out\n");
		str.append("echo param2:$2 >> $1/test.out\n");
		str.append("echo param3:$3 >> $1/test.out\n");
		str.append("echo param4:$4 >> $1/test.out\n");
		str.append("echo 1.0 > $3/score");
		FileUtil.writeToFile(strategyExecutablesh, str.toString());
	}

	private int getTimeOutFromOutput() {
		int startStrategy = output.indexOf("Strategy");
		int startTimeout = output.indexOf("param4:", startStrategy) + "param4:".length();
		int lineEnd = output.indexOf("\n", startTimeout);
		int timeout = Integer.parseInt(output.substring(startTimeout, lineEnd).trim());
		return timeout;
	}

	private void changeTimeOutInStrategyBat(String strategyExecutable, String timeoutInSeconds) {
		StringBuilder str = new StringBuilder();
		str.append("echo Strategy >> %1/test.out\n");
		str.append("echo file:%~dp0%~nx0 >> %1/test.out\n");
		str.append("echo param1:%1 >> %1/test.out\n");
		str.append("echo param2:%2 >> %1/test.out\n");
		str.append("echo param3:%3 >> %1/test.out\n");
		str.append("echo param4:%4 >> %1/test.out\n");
		str.append("PING localhost -n " + timeoutInSeconds + " > NUL\n");
		str.append("echo 1.0 > %3/score");
		FileUtil.writeToFile(strategyExecutable, str.toString());
	}

	private void changeTimeOutInStrategySh(String strategyExecutable, String timeoutInSeconds) {
		StringBuilder str = new StringBuilder();
		str.append("echo Strategy >> $1/test.out\n");
		str.append("echo file:$0 >> $1/test.out\n");
		str.append("echo param1:$1 >> $1/test.out\n");
		str.append("echo param2:$2 >> $1/test.out\n");
		str.append("echo param3:$3 >> $1/test.out\n");
		str.append("echo param4:$4 >> $1/test.out\n");
		str.append("sleep " + timeoutInSeconds + "\n");
		str.append("echo 1.0 > $3/score");
		FileUtil.writeToFile(strategyExecutable, str.toString());
	}
}
