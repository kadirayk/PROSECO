package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.commons.config.PROSECOConfig;
import de.upb.crc901.proseco.commons.config.ProcessConfig;
import de.upb.crc901.proseco.commons.controller.ProcessIdAlreadyExistsException;
import de.upb.crc901.proseco.commons.controller.ProcessIdDoesNotExistException;
import de.upb.crc901.proseco.commons.controller.PrototypeCouldNotBeExtractedException;
import de.upb.crc901.proseco.commons.interview.InterviewFillout;
import de.upb.crc901.proseco.commons.processstatus.EProcessState;
import de.upb.crc901.proseco.commons.processstatus.InvalidStateTransitionException;
import de.upb.crc901.proseco.commons.util.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.commons.util.Parser;
import de.upb.crc901.proseco.commons.util.SerializationUtil;

/**
 * Implementation of {@link AProsecoConfigurationProcess} for file based configuration
 *
 * @author kadirayk
 *
 */
public class FileBasedConfigurationProcess extends AProsecoConfigurationProcess {

	private final File prosecoConfigFile;
	private final PROSECOConfig config;

	/**
	 * Default constructor that initiates a FileBasedConfigurationProcess with the given prosecoConfig file
	 *
	 * @param prosecoConfigFile file that points to the prosecoConfigFile that configures the values described in {@link PROSECOConfig}
	 */
	public FileBasedConfigurationProcess(final File prosecoConfigFile) {
		try {
			super.updateProcessState(EProcessState.INIT);
		} catch (final InvalidStateTransitionException e) {
			logger.error(e.getMessage());
		}
		this.prosecoConfigFile = prosecoConfigFile;
		this.config = PROSECOConfig.get(prosecoConfigFile);
	}

	@Override
	public void createNew() throws InvalidStateTransitionException {
		super.updateProcessState(EProcessState.CREATED);
	}

	@Override
	public void createNew(final String processId) throws ProcessIdAlreadyExistsException, InvalidStateTransitionException {
		if (processId != null) {
			final File processFolder = new File(this.config.getDirectoryForProcesses() + File.separator + processId);
			if (processFolder.exists()) {
				throw new ProcessIdAlreadyExistsException();
			}
		}

		this.processId = processId;

		super.updateProcessState(EProcessState.CREATED);
	}

	private void createEnvironment(final String domain) {
		if (this.processId == null) {
			final String id = domain + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toLowerCase();
			this.processId = id;
		}
		final File processFolder = new File(this.config.getDirectoryForProcesses() + File.separator + this.processId);

		try {
			FileUtils.forceMkdir(processFolder);
		} catch (final IOException e) {
			// File IO exception is only relevant for FileBasedConfigurationProcess
			logger.error(e.getMessage());
		}

		final ProcessConfig pc = new ProcessConfig(this.processId, domain, this.prosecoConfigFile);
		try {
			new ObjectMapper().writeValue(new File(processFolder + File.separator + "process.json"), pc);
		} catch (final IOException e1) {
			logger.error(e1.getMessage());
		}
		try {
			this.processEnvironment = new PROSECOProcessEnvironment(processFolder);
		} catch (final IOException e) {
			logger.error(e.getMessage());
		}

	}

	@Override
	public void fixDomain(final String domain) throws InvalidStateTransitionException {
		super.fixDomain(domain);
		this.createEnvironment(domain);

	}

	@Override
	public PROSECOProcessEnvironment getProcessEnvironment() throws InvalidStateTransitionException {
		if (super.getProcessState() == EProcessState.INIT || super.getProcessState() == EProcessState.CREATED) {
			throw new InvalidStateTransitionException();
		}
		return this.processEnvironment;
	}

	@Override
	public void attach(final String processId) throws ProcessIdDoesNotExistException, InvalidStateTransitionException {
		final File processFolder = new File(this.config.getDirectoryForProcesses() + File.separator + processId);
		if (!processFolder.exists()) {
			throw new ProcessIdDoesNotExistException();
		}
		this.processId = processId;
		if (this.getProcessState() == EProcessState.INIT) {
			super.updateProcessState(EProcessState.CREATED);
		}
		this.fixDomain(processId.split("-")[0]);
		try {
			this.processEnvironment = new PROSECOProcessEnvironment(processFolder);
		} catch (final IOException e) {
			logger.error(e.getMessage());
		}

		if (this.getProcessState() != EProcessState.DOMAIN_DEFINITION) { // no need to update state
			super.updateProcessState(EProcessState.CREATED);
			// domain is already known after attaching
			super.updateProcessState(EProcessState.DOMAIN_DEFINITION);
		}

	}

	@Override
	public void updateInterview(final Map<String, String> answers) throws InvalidStateTransitionException {
		super.updateProcessState(EProcessState.INTERVIEW);
		if (this.answers == null) {
			this.answers = new HashMap<>();
		}
		this.answers.putAll(answers);

		final File interviewFile = new File(this.processEnvironment.getInterviewDirectory().getAbsolutePath() + File.separator + "interview.yaml");
		final Parser parser = new Parser();
		InterviewFillout fillout = null;
		try {
			fillout = new InterviewFillout(parser.initializeInterviewFromConfig(interviewFile));
			fillout.updateAnswers(this.answers);
			fillout = new InterviewFillout(fillout.getInterview(), fillout.getAnswers());
		} catch (final IOException e) {
			logger.error(e.getMessage());
		}

		SerializationUtil.writeAsJSON(this.processEnvironment.getInterviewStateFile(), fillout);

	}

	@Override
	protected void extractPrototype() throws PrototypeCouldNotBeExtractedException, InvalidStateTransitionException {
		super.extractPrototype();
		final File processFolder = new File(this.config.getDirectoryForProcesses() + File.separator + this.processId);

		// update environment with prototype info
		try {
			this.processEnvironment = new PROSECOProcessEnvironment(processFolder);
		} catch (final IOException e) {
			logger.error(e.getMessage());
		}
	}

}
