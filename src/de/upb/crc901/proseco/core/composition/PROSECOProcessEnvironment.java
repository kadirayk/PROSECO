package de.upb.crc901.proseco.core.composition;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.upb.crc901.proseco.core.DomainConfig;
import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.ProcessConfig;
import de.upb.crc901.proseco.core.PrototypeConfig;
import de.upb.crc901.proseco.core.interview.InterviewFillout;
import de.upb.crc901.proseco.view.util.SerializationUtil;

/**
 * ExecutionEnvironment, is the directory where an instance of the selected prototype is created.
 * 
 * @author fmohr
 *
 */
public class PROSECOProcessEnvironment {

	private final PROSECOConfig prosecoConfig;

	/* process-specific */
	private final String processId;
	private final File processDirectory;

	/* domain-specific but process unspecific */
	private final File interviewDirectory; // original interview files

	/* domain-specific AND process-specific (specific to domain but not specific to prototype) */
	private final File domainDirectory;
	private final DomainConfig domainConfig;
	private final File interviewStateDirectory;
	private final File interviewStateFile;
	private final File interviewResourcesDirectory;

	/* prototype-specific */
	private final File prototypeDirectory;
	private final PrototypeConfig prototypeConfig;
	private final String prototypeName;
	private final File strategyDirectory;
	private final File benchmarksDirectory;
	private final File groundingDirectory;
	private final File groundingFile;
	private final File deploymentFile;

	/* configuration-process-specific (specific to prototype) */
	private final File searchDirectory;
	// private final File configDirectory;
	private final File analysisRoutine;
	private final InterviewFillout interviewFillout;

	/**
	 * @param processFolder The process folder MUST, by convention, contain a process.json that contains its id, the domain, the prototype, and the proseco configuration that is used to run it
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PROSECOProcessEnvironment(final File processFolder) throws FileNotFoundException, IOException {

		/* read the process.json */
		File processConfigFile = new File(processFolder + File.separator + "process.json");
		if (!processConfigFile.exists()) {
			throw new FileNotFoundException("Cannot create a PROSECOProcess environment for a folder without process.json");
		}
		ProcessConfig processConfig = new ObjectMapper().readValue(processConfigFile, ProcessConfig.class);
		if (processConfig.getProcessId() == null)
			throw new IllegalArgumentException("The process.json MUST define a process id");
		if (processConfig.getDomain() == null)
			throw new IllegalArgumentException("The process.json MUST define a domain");
		
		/* read PROSECO configuration and configure process */
		prosecoConfig = PROSECOConfig.get(processConfig.getProsecoConfigFile());
		processId = processConfig.getProcessId();
		processDirectory = new File(prosecoConfig.getDirectoryForProcesses() + File.separator + processId);
		domainDirectory = new File(prosecoConfig.getDirectoryForDomains() + File.separator + processConfig.getDomain());
		domainConfig = DomainConfig.get(domainDirectory + File.separator + processConfig.getDomain() + File.separator + "domain.conf");

		/* domain specific folders */
		interviewDirectory = new File(domainDirectory + File.separator + domainConfig.getNameOfInterviewFolder());
		interviewStateDirectory = new File(processDirectory + File.separator + domainConfig.getNameOfInterviewFolder());
		interviewStateFile = new File(interviewStateDirectory + File.separator + domainConfig.getNameOfInterviewStateFile());
		interviewResourcesDirectory = new File(interviewStateDirectory + File.separator + domainConfig.getNameOfInterviewResourceFolder());

		/* extract prototype from interview */
		interviewFillout = interviewStateFile.exists() ? SerializationUtil.readAsJSON(interviewStateFile) : null;

		/* prototype specific folders if prototype has been set in the interview */
		if (interviewFillout != null && interviewFillout.getAnswer("prototype") != null) {
			prototypeName = interviewFillout.getAnswer("prototype");
			prototypeDirectory = new File(domainDirectory + File.separator + domainConfig.getPrototypeFolder() + File.separator + prototypeName);
			prototypeConfig = PrototypeConfig.get(prototypeDirectory + File.separator + "prototype.conf");
			benchmarksDirectory = new File(prototypeDirectory + File.separator + prototypeConfig.getBenchmarkPath());
			groundingDirectory = new File(prototypeDirectory + File.separator + prototypeConfig.getNameOfGroundingFolder());
			groundingFile = new File(groundingDirectory + File.separator + prototypeConfig.getGroundingCommand());
			strategyDirectory = new File(prototypeDirectory + File.separator + prototypeConfig.getNameOfStrategyFolder());
			analysisRoutine = new File(prototypeDirectory + File.separator + prototypeConfig.getHookForPreGrounding());
			deploymentFile = new File(prototypeDirectory + File.separator + prototypeConfig.getDeploymentCommand());
		}
		else {
			prototypeName = null;
			prototypeDirectory = null;
			prototypeConfig = null;
			benchmarksDirectory = null;
			groundingDirectory = null;
			groundingFile = null;
			strategyDirectory = null;
			analysisRoutine = null;
			deploymentFile = null;
		}

		// configDirectory = new File(processDirectory + File.separator + prototype());;
		searchDirectory = new File(processDirectory + File.separator + "search");
	}

	public String getProcessId() {
		return processId;
	}

	public File getPrototypeDirectory() {
		return prototypeDirectory;
	}

	public File getBenchmarksDirectory() {
		return benchmarksDirectory;
	}

	public File getGroundingDirectory() {
		return groundingDirectory;
	}

	public File getStrategyDirectory() {
		return strategyDirectory;
	}

	// public File getConfigDirectory() {
	// return configDirectory;
	// }

	public File getInterviewDirectory() {
		return interviewDirectory;
	}

	public File getInterviewStateDirectory() {
		return interviewStateDirectory;
	}
	
	public File getInterviewStateFile() {
		return interviewStateFile;
	}

	public File getInterviewResourcesDirectory() {
		return interviewResourcesDirectory;
	}

	public File getGroundingFile() {
		return groundingFile;
	}

	public File getProcessDirectory() {
		return processDirectory;
	}

	public PROSECOConfig getProsecoConfig() {
		return prosecoConfig;
	}

	public PrototypeConfig getPrototypeConfig() {
		return prototypeConfig;
	}

	public String getPrototypeName() {
		return prototypeName;
	}

	public File getSearchDirectory() {
		return searchDirectory;
	}

	public File getSearchInputDirectory() {
		return new File(searchDirectory + File.separator + "inputs");
	}

	public File getSearchOutputDirectory() {
		return new File(searchDirectory + File.separator + "outputs");
	}

	public File getSearchStrategyOutputDirectory(String strategy) {
		return new File(getSearchOutputDirectory() + File.separator + strategy);
	}

	public File getAnalysisRoutine() {
		return analysisRoutine;
	}

	public File getDeploymentFile() {
		return deploymentFile;
	}

	public File getServiceHandle() {
		return new File(getProcessDirectory() + File.separator + "service.handle");
	}
}
