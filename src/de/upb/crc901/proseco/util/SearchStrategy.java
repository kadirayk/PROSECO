package de.upb.crc901.proseco.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;

import de.upb.crc901.proseco.core.PROSECOConfig;
import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;
import de.upb.crc901.proseco.view.util.FileUtil;

public abstract class SearchStrategy implements Runnable {

	private final String strategyName;
	private final File dirOfInputs;
	private final File dirOfOutputs;
	private final PROSECOProcessEnvironment environment;

	public SearchStrategy(String[] args) throws FileNotFoundException, IOException {
		if (args.length != 3) {
			throw new IllegalArgumentException("A search strategy must be invoked with an array of exactly three arguments (folder with PROSECO conf and process id, input and output folder)");
		}
		File environmentDir = new File(args[0]);
		this.environment = new PROSECOProcessEnvironment(PROSECOConfig.get(new File(environmentDir + File.separator + "proseco.conf")),
				FileUtil.readFile(environmentDir + File.separator + "process.id"));
		this.dirOfInputs = new File(args[1]);
		this.dirOfOutputs = new File(args[2]);
		this.strategyName = this.dirOfOutputs.getName();
		System.out.println("Retrieving interview data from " + this.environment.getInterviewStateDirectory());
	}

	public PROSECOProcessEnvironment getEnvironment() {
		return environment;
	}

	public File getDirOfInputs() {
		return dirOfInputs;
	}

	public File getDirOfOutputs() {
		return dirOfOutputs;
	}

	public String getStrategyName() {
		return strategyName;
	}

	protected void writeOutputObject(String filename, Object o) throws IOException {
		BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(new File(dirOfOutputs + File.separator + filename)));
		try (ObjectOutputStream os = new ObjectOutputStream(fw)) {
			os.writeObject(o);
		}

	}

	protected void writeScore(double score) throws IOException {
		FileUtils.writeStringToFile(new File(dirOfOutputs + File.separator + "score"), "" + score, Charset.defaultCharset());
	}
}
