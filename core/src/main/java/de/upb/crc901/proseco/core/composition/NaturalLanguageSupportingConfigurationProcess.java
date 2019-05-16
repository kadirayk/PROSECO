package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.commons.config.PROSECOConfig;
import de.upb.crc901.proseco.commons.config.ProcessConfig;
import de.upb.crc901.proseco.commons.controller.CannotFixDomainInThisProcessException;
import de.upb.crc901.proseco.commons.controller.DefaultDomainScoreComputer;
import de.upb.crc901.proseco.commons.controller.DomainCouldNotBeDetectedException;
import de.upb.crc901.proseco.commons.controller.ProcessIdAlreadyExistsException;
import de.upb.crc901.proseco.commons.controller.ProcessIdDoesNotExistException;
import de.upb.crc901.proseco.commons.controller.PrototypeCouldNotBeExtractedException;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.processstatus.EProcessState;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.commons.util.Parser;
import de.upb.crc901.proseco.commons.util.SerializationUtil;

public class NaturalLanguageSupportingConfigurationProcess extends AProsecoConfigurationProcess {

	private final File prosecoConfigFile;
	private final PROSECOConfig config;

	public NaturalLanguageSupportingConfigurationProcess(File prosecoConfigFile) {
		try {
			super.updateProcessState(EProcessState.INIT);
		} catch (InvalidStateTransitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.prosecoConfigFile = prosecoConfigFile;
		config = PROSECOConfig.get(prosecoConfigFile);
	}

	public <T> void receiveGeneralTaskDescription(T description) throws DomainCouldNotBeDetectedException, InvalidStateTransitionException {
		if (description instanceof String) {
			String descriptionString = (String) description;
			DefaultDomainScoreComputer domainScoreComputer = new DefaultDomainScoreComputer();
			List<String> avilableDomains = domainScoreComputer.getAvailableDomains(config);

			Double bestScore = 0.0;
			String detectedDomain = null;
			for (String domain : avilableDomains) {
				Double score = domainScoreComputer.getDomainScore(descriptionString, domain);
				if (score > bestScore) {
					bestScore = score;
					detectedDomain = domain;
				}
			}
			try {
				super.fixDomain(detectedDomain);
				createEnvironment(detectedDomain);
			} catch (CannotFixDomainInThisProcessException e) {
				e.printStackTrace();
			}
			answerInterview();
		}
	}

	private void answerInterview() throws InvalidStateTransitionException {
		updateProcessState(EProcessState.INTERVIEW);
		File interviewFile = new File(
				processEnvironment.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = null;
		try {
			fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String, String> answers = fillout.retrieveQuestionAnswerMap();
		for(String question: answers.keySet()) {
			String answer = askOracle(question);
			answers.put(question, answer);
		}
		updateInterview(answers);
	}
	
	private String askOracle(String question) {
		String answer = null;
		if(question.equals("Please select prototype")) {
			answer = "test1";
		}
		return answer;
	}
	
	@Override
	public void updateInterview(Map<String, String> answers) throws InvalidStateTransitionException {
		if(this.answers==null) {
			this.answers = new HashMap<>();
		}
		this.answers.putAll(answers);
		
		File interviewFile = new File(
				this.processEnvironment.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		Parser parser = new Parser();
		InterviewFillout fillout = null;
		try {
			fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
			fillout.updateAnswers(this.answers);
			fillout = new InterviewFillout(fillout.getInterview(), fillout.getAnswers());
		} catch (IOException e) {
			e.printStackTrace();
		}

		SerializationUtil.writeAsJSON(this.processEnvironment.getInterviewStateFile(), fillout);
		
		super.updateProcessState(EProcessState.INTERVIEW);

	}

	@Override
	public void fixDomain(String domain) throws CannotFixDomainInThisProcessException {
		throw new CannotFixDomainInThisProcessException();
	}

	@Override
	public void createNew(String processId) throws ProcessIdAlreadyExistsException, InvalidStateTransitionException {
		if (processId != null) {
			File processFolder = new File(config.getDirectoryForProcesses() + File.separator + processId);
			if (processFolder.exists()) {
				throw new ProcessIdAlreadyExistsException();
			}
		}
		
		this.processId = processId;
		
		super.updateProcessState(EProcessState.CREATED);
	}

	@Override
	public void attach(String processId) throws ProcessIdDoesNotExistException, InvalidStateTransitionException {
		File processFolder = new File(config.getDirectoryForProcesses() + File.separator + processId);
		if (!processFolder.exists()) {
			throw new ProcessIdDoesNotExistException();
		}
		this.processId = processId;
		super.updateProcessState(EProcessState.CREATED);
		
		try {
			processEnvironment = new PROSECOProcessEnvironment(processFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// domain is already known after attaching
		super.updateProcessState(EProcessState.DOMAIN_DEFINITION);

	}
	
	private void createEnvironment(String domain) {
		if(this.processId==null) {
			String id = domain + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toLowerCase();
			this.processId = id;
		}
		File processFolder = new File(config.getDirectoryForProcesses() + File.separator + this.processId);
		
		try {
			FileUtils.forceMkdir(processFolder);
		} catch (IOException e) {
			// File IO exception is only relevant for FileBasedConfigurationProcess
			e.printStackTrace();
		}
		
		ProcessConfig pc = new ProcessConfig(processId, domain, prosecoConfigFile);
		try {
			new ObjectMapper().writeValue(new File(processFolder + File.separator + "process.json"), pc);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			processEnvironment = new PROSECOProcessEnvironment(processFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	@Override
	protected void extractPrototype() throws PrototypeCouldNotBeExtractedException, InvalidStateTransitionException {
		super.extractPrototype();
		File processFolder = new File(config.getDirectoryForProcesses() + File.separator + processId);
		
		// update environment with prototype info
		try {
			this.processEnvironment = new PROSECOProcessEnvironment(processFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public PROSECOProcessEnvironment getProcessEnvironment() {
		return processEnvironment;
	}

}
