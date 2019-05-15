package de.upb.crc901.proseco.core.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.upb.crc901.proseco.commons.controller.CannotFixDomainInThisProcessException;
import de.upb.crc901.proseco.commons.controller.GroundingNotSuccessfulForAnyStrategyException;
import de.upb.crc901.proseco.commons.controller.NoStrategyFoundASolutionException;
import de.upb.crc901.proseco.commons.controller.PROSECOSolution;
import de.upb.crc901.proseco.commons.controller.ProcessController;
import de.upb.crc901.proseco.commons.controller.ProcessIdAlreadyExistsException;
import de.upb.crc901.proseco.commons.controller.PrototypeCouldNotBeExtractedException;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.processstatus.EProcessState;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.composition.FileBasedConfigurationProcess;
import de.upb.crc901.proseco.core.test.util.Parser;

/*-
 * 
 * Possible Transitions:
 * rows: possible state a process can be in 
 * cols: possible next states a process can move to
 * + : correct transition
 * - : incorrect transition but programmatically possible (should raise exception)
 * x : programmatically impossible transition
 * 
 * 
 * 			   INIT	 CREATED DOMAIN	INTERVIEW	COMPOSITION	PROTOTYPE CHOSEN	GROUNDING	DEPLOYMENT	DONE
 *	INIT		x		+		-		-			-			x		x			-			x		x
 *	CREATED		x		-		+		-			-			x		x			-			x		x
 *	DOMAIN		x		-		-		+			-			x		x			-			x		x
 *	INTERVIEW	x		-		-		+			+			x		x			-			x		x
 *	CHOSEN		x		-		-		-			-			x		x			+			x		x
 *	DONE		x		-		-		-			-			x		x			-			x		x
 *
 * 
 * states inaccessible from outside: COMPOSITION, PROTOTYPE, GROUNDING, DEPLOYMENT 
 * 
 * in total 30 different cases are available
 * 
 * @author kadirayk
 *
 */

public class StateTransitionTest {
	static String processId;
	static PROSECOProcessEnvironment env;
	static String output;

	/*
	 * Tests for state = INIT
	 */

	/**
	 * INIT -> CREATED [CORRECT]
	 */
	@Test
	public void testInitToCreated() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INIT);
		process.createNew(null);
		assertEquals(EProcessState.CREATED, process.getProcessState());
	}

	/**
	 * INIT -> DOMAIN_DEFINITON [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInitToDomain() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INIT);
		process.fixDomain("test");
	}

	/**
	 * INIT -> INTERVIEW [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInitToInterview() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INIT);
		process.updateInterview(new HashMap<>());
	}

	/**
	 * INIT -> COMPOSITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInitToComposition() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INIT);
		process.startComposition();
	}

	/**
	 * INIT -> GROUNDING [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInitToGrounding() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INIT);
		process.chooseAndDeploySolution(null);
	}

	/*
	 * Tests for state = CREATED
	 */

	/**
	 * CREATED -> CREATED [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testCreatedToCreated() throws Exception {
		ProcessController process = getProcessForState(EProcessState.CREATED);
		process.createNew(null);
	}

	/**
	 * CREATED -> DOMAIN_DEFINITON [CORRECT]
	 */
	@Test
	public void testCreatedToDomain() throws Exception {
		ProcessController process = getProcessForState(EProcessState.CREATED);
		process.fixDomain("test");
		assertEquals(EProcessState.DOMAIN_DEFINITION, process.getProcessState());
	}

	/**
	 * CREATED -> INTERVIEW [CORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testCreatedToInterview() throws Exception {
		ProcessController process = getProcessForState(EProcessState.CREATED);
		process.updateInterview(new HashMap<>());
	}
	
	/**
	 * CREATED -> COMPOSITON [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testCreatedToComposition() throws Exception {
		ProcessController process = getProcessForState(EProcessState.CREATED);
		process.startComposition();
	}
	
	/**
	 * CREATED -> GROUNDING [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testCreatedToGrounding() throws Exception {
		ProcessController process = getProcessForState(EProcessState.CREATED);
		process.chooseAndDeploySolution(null);
	}
	
	/*
	 * Tests for state = DOMAIN_DEFINITON
	 */
	
	/**
	 * DOMAIN_DEFINITON -> CREATED [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDomainToCreated() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DOMAIN_DEFINITION);
		process.createNew(null);
	}
	
	/**
	 * DOMAIN_DEFINITON -> DOMAIN_DEFINITON [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDomainToDomain() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DOMAIN_DEFINITION);
		process.fixDomain("");
	}
	
	/**
	 * DOMAIN_DEFINITON -> INTERVIEW [CORRECT]
	 */
	@Test
	public void testDomainToInterview() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DOMAIN_DEFINITION);
		process.updateInterview(new HashMap<>());
	}
	
	/**
	 * DOMAIN_DEFINITON -> COMPOSITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDomainToComposition() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DOMAIN_DEFINITION);
		process.startComposition();
	}
	
	/**
	 * DOMAIN_DEFINITON -> GROUNDING [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDomainToGrounding() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DOMAIN_DEFINITION);
		process.chooseAndDeploySolution(null);
	}
	

	/*
	 * Tests for state = INTERVIEW
	 */
	
	/**
	 * INTERVIEW -> CREATED [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInterviewToCreated() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INTERVIEW);
		process.createNew(null);
	}
	
	/**
	 * INTERVIEW -> DOMAIN_DEFINITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInterviewToDomain() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INTERVIEW);
		process.fixDomain("test");
	}
	
	/**
	 * INTERVIEW -> INTERVIEW [CORRECT]
	 */
	@Test
	public void testInterviewToInterview() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INTERVIEW);
		process.updateInterview(new HashMap<>());
		assertEquals(EProcessState.INTERVIEW, process.getProcessState());
	}
	
	/**
	 * INTERVIEW -> COMPOSITION [CORRECT]
	 */
	@Test
	public void testInterviewToComposition() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INTERVIEW);
		process.startComposition();
		assertEquals(EProcessState.STRATEGY_CHOSEN, process.getProcessState());
	}
	
	/**
	 * INTERVIEW -> GROUNDING [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testInterviewToGrounding() throws Exception {
		ProcessController process = getProcessForState(EProcessState.INTERVIEW);
		process.chooseAndDeploySolution(null);
	}
	
	/*
	 * Tests for state = STRATEGY_CHOSEN
	 */
	
	/**
	 * STRATEGY_CHOSEN -> CREATED [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testChosenToCreated() throws Exception {
		ProcessController process = getProcessForState(EProcessState.STRATEGY_CHOSEN);
		process.createNew(null);
	}

	/**
	 * STRATEGY_CHOSEN -> DOMAIN_DEFINITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testChosenToDomain() throws Exception {
		ProcessController process = getProcessForState(EProcessState.STRATEGY_CHOSEN);
		process.fixDomain("test");
	}
	
	/**
	 * STRATEGY_CHOSEN -> INTERVIEW [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testChosenToInterview() throws Exception {
		ProcessController process = getProcessForState(EProcessState.STRATEGY_CHOSEN);
		process.updateInterview(new HashMap<>());
	}
	
	/**
	 * STRATEGY_CHOSEN -> COMPOSITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testChosenToComposition() throws Exception {
		ProcessController process = getProcessForState(EProcessState.STRATEGY_CHOSEN);
		process.startComposition();
	}
	
	/**
	 * STRATEGY_CHOSEN -> GROUNDING [CORRECT]
	 */
	@Test
	public void testChosenToGrounding() throws Exception {
		ProcessController process = getProcessForState(EProcessState.STRATEGY_CHOSEN);
		process.chooseAndDeploySolution(null);
		assertEquals(EProcessState.DONE, process.getProcessState());
	}
	
	/*
	 * Tests for state = DONE
	 */
	
	/**
	 * DONE -> CREATED [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDoneToCreated() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DONE);
		process.createNew(null);
	}
	
	/**
	 * DONE -> DOMAIN_DEFINITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDoneToDomain() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DONE);
		process.fixDomain("test");
	}
	
	/**
	 * DONE -> INTERVIEW [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDoneToInterview() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DONE);
		process.updateInterview(new HashMap<>());
	}
	
	/**
	 * DONE -> COMPOSITION [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDoneToComposition() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DONE);
		process.startComposition();
	}
	
	/**
	 * DONE -> GROUNDING [INCORRECT]
	 */
	@Test(expected = InvalidStateTransitionException.class)
	public void testDoneToGrounding() throws Exception {
		ProcessController process = getProcessForState(EProcessState.DONE);
		process.chooseAndDeploySolution(null);
	}
	
	@Test
	public void testWholeTransition() throws Exception {
		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
		assertEquals(EProcessState.INIT, processController.getProcessState());

		processController.createNew(null);
		assertEquals(EProcessState.CREATED, processController.getProcessState());

		processController.fixDomain("test");
		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());

		env = processController.getProcessEnvironment();
		processId = env.getProcessId();
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
		answers.put("Please select prototype", "test1");
		processController.updateInterview(answers);
		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());

		PROSECOSolution solution = processController.startComposition();
		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());

		processController.chooseAndDeploySolution(solution);
		assertEquals(EProcessState.DONE, processController.getProcessState());
	}

	@Test
	public void testWholeTransitionWithAttached() throws Exception {
		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
		processController.createNew(null);
		processController.fixDomain("test");
		env = processController.getProcessEnvironment();
		processId = env.getProcessId();
		
		processController = new FileBasedConfigurationProcess(new File(""), 1000);
		processController.attach(processId);
		env = processController.getProcessEnvironment();
		
		File interviewFile = new File(
				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
		answers.put("Please select prototype", "test1");
		processController.updateInterview(answers);
		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());

		PROSECOSolution solution = processController.startComposition();
		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());

		processController.chooseAndDeploySolution(solution);
		assertEquals(EProcessState.DONE, processController.getProcessState());
	}
	
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testFixDomainBeforeCreateNew() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.fixDomain("test");
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testInterviewBeforeCreateNew() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testStartCompositionBeforeCreateNew() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.startComposition();
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testDeployBeforeCreateNew() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		PROSECOSolution solution = null;
//		processController.chooseAndDeploySolution(solution);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testInterviewBeforeFixDomain() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCompositionBeforeFixDomain() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.startComposition();
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testDeployBeforeFixDomain() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCompositionBeforeInterview() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		processController.startComposition();
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testDeployBeforeInterview() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testDeployBeforeComposition() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCreateNewAfterFixDomain() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		processController.createNew(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCreateNewAfterInterview() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		processController.createNew(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCreateNewAfterComposition() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//		processController.createNew(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCreateNewAfterDeploy() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(solution);
//		assertEquals(EProcessState.DONE, processController.getProcessState());
//		processController.createNew(null);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testFixDomainAfterInterview() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		processController.fixDomain("test");
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testFixDomainAfterComposition() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//		processController.fixDomain("test");
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testFixDomainAfterDeploy() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(solution);
//		assertEquals(EProcessState.DONE, processController.getProcessState());
//		processController.fixDomain("test");
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testInterviewAfterComposition() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//
//		processController.updateInterview(answers);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testInterviewAfterDeploy() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(solution);
//		assertEquals(EProcessState.DONE, processController.getProcessState());
//		processController.updateInterview(answers);
//	}
//
//	@Test(expected = InvalidStateTransitionException.class)
//	public void testCompositionAfterDeploy() throws Exception {
//		ProcessController processController = new FileBasedConfigurationProcess(new File(""), 1000);
//		assertEquals(EProcessState.INIT, processController.getProcessState());
//
//		processController.createNew(null);
//		assertEquals(EProcessState.CREATED, processController.getProcessState());
//
//		processController.fixDomain("test");
//		assertEquals(EProcessState.DOMAIN_DEFINITION, processController.getProcessState());
//
//		env = processController.getProcessEnvironment();
//		processId = env.getProcessId();
//		File interviewFile = new File(
//				env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
//		Parser parser = new Parser();
//		InterviewFillout fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
//		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
//		answers.put("Please select prototype", "test1");
//		processController.updateInterview(answers);
//		assertEquals(EProcessState.INTERVIEW, processController.getProcessState());
//
//		PROSECOSolution solution = processController.startComposition();
//		assertEquals(EProcessState.STRATEGY_CHOSEN, processController.getProcessState());
//
//		processController.chooseAndDeploySolution(solution);
//		assertEquals(EProcessState.DONE, processController.getProcessState());
//
//		processController.startComposition();
//	}

	private ProcessController getProcessForState(EProcessState requestedState) throws ProcessIdAlreadyExistsException,
			InvalidStateTransitionException, CannotFixDomainInThisProcessException, NoStrategyFoundASolutionException,
			PrototypeCouldNotBeExtractedException, GroundingNotSuccessfulForAnyStrategyException {
		ProcessController process = null;
		switch (requestedState) {
		case INIT:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			break;
		case CREATED:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			process.createNew(null);
			break;
		case DOMAIN_DEFINITION:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			process.createNew(null);
			process.fixDomain("test");
			break;
		case INTERVIEW:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			process.createNew(null);
			process.fixDomain("test");
			env = process.getProcessEnvironment();
			processId = env.getProcessId();
			File interviewFile = new File(
					env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
			Parser parser = new Parser();
			InterviewFillout fillout = null;
			try {
				fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
			answers.put("Please select prototype", "test1");
			process.updateInterview(answers);
			break;
		case COMPOSITION:
			System.err.println("This State is inaccessible from outside");
			break;
		case STRATEGY_CHOSEN:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			process.createNew(null);
			process.fixDomain("test");
			env = process.getProcessEnvironment();
			processId = env.getProcessId();
			interviewFile = new File(env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
			parser = new Parser();
			fillout = null;
			try {
				fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			answers = fillout.retrieveQuestionAnswerMap();
			answers.put("Please select prototype", "test1");
			process.updateInterview(answers);
			PROSECOSolution solution = process.startComposition();
			break;
		case DEPLOYMENT:
			System.err.println("This State is inaccessible from outside");
			break;
		case DONE:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			process.createNew(null);
			process.fixDomain("test");
			env = process.getProcessEnvironment();
			processId = env.getProcessId();
			interviewFile = new File(env.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
			parser = new Parser();
			fillout = null;
			try {
				fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			answers = fillout.retrieveQuestionAnswerMap();
			answers.put("Please select prototype", "test1");
			process.updateInterview(answers);
			solution = process.startComposition();
			process.chooseAndDeploySolution(solution);
			break;
		case GROUNDING:
			System.err.println("This State is inaccessible from outside");
			break;
		case PROTOTYPE_EXTRACTED:
			System.err.println("This State is inaccessible from outside");
			break;
		default:
			process = new FileBasedConfigurationProcess(new File(""), 1000);
			break;

		}
		return process;
	}
}
