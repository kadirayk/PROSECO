package de.upb.crc901.proseco.core.test;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.upb.crc901.proseco.commons.controller.DefaultProcessController;
import de.upb.crc901.proseco.commons.controller.ProcessController;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.util.FileUtil;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.commons.util.SerializationUtil;
import de.upb.crc901.proseco.core.composition.FileBasedConfigurationProcess;

public class StrategyInterviewDataTest {

	static PROSECOProcessEnvironment env;
	static File interviewFile;

	@BeforeClass
	public static void initialize() throws Exception {
		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
		processController.createNew(null);
		processController.fixDomain("test");
		env = processController.getProcessEnvironment();
		interviewFile = new File(env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
	}

	@Ignore
	@Test
	public void testPartialInterviewHasInputsThatAreNotInInterviewDefinition() throws Exception {
//		ProcessController processController = new DefaultProcessController(new File(""));
//		env = processController.getConstructionProcessEnvironment("test-default");
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//
//		InterviewFillout filloutState = SerializationUtil.readAsJSON(env.getInterviewStateFile());
//		filloutState.getAnswers().get("nonexisting-id");
//		System.out.println("");

	}

	private void setupStrategyFile() {
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

}
