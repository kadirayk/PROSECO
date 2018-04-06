package de.upb.crc901.proseco.command;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;
import jaicore.basic.FileUtil;

/**
 * MovePlaceholderFilesToSourceCommand, moves all placeholder files created by
 * the winning strategy to the source folder as inputs for the grounding
 * routine.
 * 
 * @author kadirayk
 * 
 */
public class MovePlaceholderFilesToSourceCommand implements Command {
	private ExecutionEnvironment executionEnvironment;

	public MovePlaceholderFilesToSourceCommand(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	@Override
	public void execute() throws Exception {
		System.out.print("Copy placeholder files of winning strategies to src folder "
				+ executionEnvironment.getGroundingDirectory() + " ...");

		// Pick the output of the winning strategy
		String winningStrategyName = "";
		double fValue = 0.0;
		for (final File strategy : executionEnvironment.getStrategyDirectory().listFiles()) {
			if (!strategy.isDirectory()) {
				continue;
			}

			final File fValueFile = new File(
					strategy.getAbsolutePath() + File.separator + Config.OUTPUT_DIR + File.separator + "f.value");
			if (!fValueFile.exists()) {
				System.out.println(
						"f.value file was not found for strategy; ignoring it: " + fValueFile.getAbsolutePath());
				continue;
			}
			Double parsedValue = 0.0;
			try {
				parsedValue = Double.parseDouble(FileUtil.readFileAsString(fValueFile.getAbsolutePath()));
			} catch (final NumberFormatException e) {
				e.printStackTrace();
			} catch (final IOException e) {
				e.printStackTrace();
			}

			if (parsedValue >= fValue) {
				winningStrategyName = strategy.getName();
				fValue = parsedValue;
			}
		}

		if (winningStrategyName.isEmpty()) {
			throw new IllegalStateException("Name of the winning strategyg is not filled.");
		}

		final File winningStrategy = new File(executionEnvironment.getStrategyDirectory() + File.separator
				+ winningStrategyName + File.separator + Config.OUTPUT_DIR);
		for (final File strategyFile : winningStrategy.listFiles()) {
			if (strategyFile.isFile()) {
				final File groundingFolderFile = new File(
						executionEnvironment.getGroundingDirectory() + File.separator + strategyFile.getName());
				try {
					FileUtils.copyFile(strategyFile, groundingFolderFile);
				} catch (final IOException e) {
					System.out.println("\nCould not move placeholder file " + strategyFile.getName() + " from "
							+ strategyFile.getAbsolutePath() + " to " + groundingFolderFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		System.out.println("DONE.");
	}

}
