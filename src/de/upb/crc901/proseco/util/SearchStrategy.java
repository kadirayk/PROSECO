package de.upb.crc901.proseco.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.core.interview.InterviewFillout;
import de.upb.crc901.proseco.view.util.FileUtil;
import de.upb.crc901.proseco.view.util.SerializationUtil;

public abstract class SearchStrategy implements Runnable {

	private final PROSECOProcessEnvironment environment;
	private final InterviewFillout interview;

	public SearchStrategy() throws FileNotFoundException, IOException {
		this.environment = new PROSECOProcessEnvironment(PROSECOConfig.get(new File("proseco.conf")), FileUtil.readFile("process.id"));
		System.out.println("Retrieving interview data from " + this.environment.getInterviewStateDirectory());
		this.interview = SerializationUtil.readAsJSON(this.environment.getInterviewStateDirectory());
	}

	public PROSECOProcessEnvironment getEnvironment() {
		return environment;
	}

	public InterviewFillout getInterview() {
		return interview;
	}
}
