package de.upb.crc901.proseco.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import de.upb.crc901.proseco.core.composition.PROSECOProcessEnvironment;

public abstract class SearchStrategy implements Runnable {

	private final String strategyName;
	private final File dirOfInputs;
	private final File dirOfOutputs;
	private final PROSECOProcessEnvironment environment;
	private final long deadline;

	public SearchStrategy(String[] args) throws FileNotFoundException, IOException {
		if (args.length != 4) {
			throw new IllegalArgumentException("A search strategy must be invoked with an array of exactly four arguments (folder with PROSECO conf and process id, input folder, output folder, and timeout in seconds).\nThe following arguments were given: " + Arrays.toString(args));
		}
		File environmentDir = new File(args[0]); // the process folder
		this.environment = new PROSECOProcessEnvironment(environmentDir);
		this.dirOfInputs = new File(args[1]);
		this.dirOfOutputs = new File(args[2]);
		this.strategyName = this.dirOfOutputs.getName();
		this.deadline= System.currentTimeMillis() + Integer.valueOf(args[3]) * 1000;
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
	
	protected boolean checkCandidate(File candidateOutputFolder) throws InterruptedException, IOException {
		File analysisProcessFile = environment.getAnalysisRoutine();
		System.out.println("Running analysis " + analysisProcessFile.getAbsolutePath() + " on " + candidateOutputFolder.getAbsolutePath());
		ProcessBuilder sb = new ProcessBuilder(analysisProcessFile.getAbsolutePath(), candidateOutputFolder.getAbsolutePath());
		Process p = sb.start();
		int exitCode = p.waitFor();
		System.out.println("Result code is " + exitCode);
		return exitCode == 0;
	}

	protected void writeOutputObject(String filename, Object o) throws IOException {
		File outFile = new File(dirOfOutputs + File.separator + filename);
		if (!outFile.getParentFile().exists())
			FileUtils.forceMkdir(outFile.getParentFile());
		BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(outFile));
		try (ObjectOutputStream os = new ObjectOutputStream(fw)) {
			os.writeObject(o);
		}
	}
	
	protected void writeStringOutput(String filename, String output) throws IOException {
		File outFile = new File(dirOfOutputs + File.separator + filename);
		if (!outFile.getParentFile().exists())
			FileUtils.forceMkdir(outFile.getParentFile());
		FileUtils.writeStringToFile(outFile, output, Charset.defaultCharset());
	}

	protected void writeScore(double score) throws IOException {
		writeStringOutput("score", "" + score);
	}
	
	protected int getRemainingSeconds() {
		return (int)((deadline - System.currentTimeMillis()) / 1000);
	}
}
