package de.upb.crc901.proseco.prototype.genderpredictor.benchmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class BenchmarkRcv {
	private static final String TASK_DIRECTORY = "task/";
	private static final String TASK_FILE_PREFIX = "task_";
	private static final String TASK_FILE_EXT = ".task";
	private static final String TASK_FILE_TEMP_EXT = ".temptask";

	public static void main(final String[] args) {
		if (args.length != 1) {
			System.out.println("Correct Usage: java BenchmarkRcv [candidateDirectory]");
			System.exit(1);
		}

		final File candidateDirectory = new File(args[0]);

		if (!candidateDirectory.exists() || !candidateDirectory.isDirectory()) {
			System.out.println("Given candidate directory does not exist or is not a directory.");
		}

		final File taskTempFile = new File(TASK_DIRECTORY + TASK_FILE_PREFIX + System.currentTimeMillis() + TASK_FILE_TEMP_EXT);
		taskTempFile.getParentFile().mkdirs();

		try (final BufferedWriter bw = new BufferedWriter(new FileWriter(taskTempFile))) {
			bw.write(candidateDirectory.getAbsolutePath());
		} catch (final IOException e) {
			e.printStackTrace();
		}
		final File taskFile = new File(TASK_DIRECTORY + TASK_FILE_PREFIX + System.currentTimeMillis() + TASK_FILE_EXT);

		try {
			FileUtils.moveFile(taskTempFile, taskFile);
		} catch (final IOException e) {
			e.printStackTrace();
		}

	}

}
