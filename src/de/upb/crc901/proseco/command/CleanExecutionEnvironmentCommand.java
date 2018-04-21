package de.upb.crc901.proseco.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import de.upb.crc901.proseco.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;

/**
 * CleanExecutionEnvironmentCommand cleans up the execution directory by
 * deleting all the created files in the <code>ExecutionEnvironment</code>, if
 * the <code>FINAL_CLEAN_UP</code> flag is active.
 * 
 * @author kadirayk
 *
 */
public class CleanExecutionEnvironmentCommand implements Command {

	private ExecutionEnvironment executionEnvironment;

	@Override
	public void execute() throws Exception {
		if (Config.FINAL_CLEAN_UP) {
			System.out.print("Clean up execution directory...");

			try {
				// delete working directories
				FileUtils.deleteDirectory(executionEnvironment.getBenchmarksDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getConfigDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getGroundingDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getParamsDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getStrategyDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getLibsDirectory());
				FileUtils.deleteDirectory(executionEnvironment.getInterviewDirectory());

				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + File.separator
						+ "contTrainingInstances.serialized").delete();
				new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + File.separator
						+ "testInstances.serialized").delete();

				final String[] filesInMainDir = { "GroundingRoutine.jar", "InitConfiguration.jar",
						"initconfiguration.bat", "groundingroutine.bat", "src/contTrainingInstances.serialized",
						"src/testInstances.serialized", "src/compile.bat", "src/train.bat" };
				for (final String filename : filesInMainDir) {
					Files.delete(new File(
							executionEnvironment.getExecutionDirectory().getAbsolutePath() + File.separator + filename)
									.toPath());
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			for (final File placeholderFile : executionEnvironment.getSourceDirectory().listFiles()) {
				if (placeholderFile.isFile()
						&& FilenameUtils.getExtension(placeholderFile.getAbsolutePath()).equals("ph")) {
					try {
						Files.delete(placeholderFile.toPath());
					} catch (final IOException e) {
						System.out.println("Could not delete placeholder file : " + placeholderFile.getAbsolutePath());
						e.printStackTrace();
					}
				}
			}
			System.out.println("DONE.");
		}
	}

}
