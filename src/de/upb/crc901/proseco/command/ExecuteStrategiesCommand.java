package de.upb.crc901.proseco.command;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;

/**
 * ExecuteStrategiesCommand, searches for strategy subfolders and forking a new
 * process for each strategy. Output and Error streams of these processes are
 * directed to <code>systemlog/systemOut.log</code> and
 * <code>systemlog/systemErr.log</code> files respectively.
 * 
 * @author kadirayk
 *
 */
public class ExecuteStrategiesCommand implements Command {
	private ExecutionEnvironment executionEnvironment;
	private List<Process> strategyProcessList;

	public ExecuteStrategiesCommand(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
		strategyProcessList = new LinkedList<>();
	}

	@Override
	public void execute() throws Exception {
		final File[] strategySubFolders = executionEnvironment.getStrategyDirectory().listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isDirectory();
			}
		});

		List<String> interviewResources = getInterviewResourcesForStrategy();

		for (final File strategyFolder : strategySubFolders) {
			System.out.print("Starting process for strategy " + strategyFolder.getName() + "...");
			interviewResources.add(0, strategyFolder.getAbsolutePath() + File.separator + Config.STRATEGY_RUNNABLE);
			File systemOut = new File(strategyFolder.getAbsolutePath() + File.separator + Config.SYSTEM_OUT_FILE);
			File systemErr = new File(strategyFolder.getAbsolutePath() + File.separator + Config.SYSTEM_ERR_FILE);
			String[] commandArguments = interviewResources.stream().toArray(String[]::new);
			final ProcessBuilder pb = new ProcessBuilder(commandArguments).redirectOutput(Redirect.appendTo(systemOut))
					.redirectError(Redirect.appendTo(systemErr));
			try {
				final Process p = pb.start();
				this.strategyProcessList.add(p);
				System.out.println("DONE.");
			} catch (final IOException e) {
				System.out.println("Could not create process for strategy " + strategyFolder.getName());
				e.printStackTrace();
			}
		}
	}

	public List<Process> getStrategyProcessList() {
		return strategyProcessList;
	}

	private List<String> getInterviewResourcesForStrategy() {
		List<String> commandArgumentList = new ArrayList<>();

		final File[] interviewResources = executionEnvironment.getInterviewResourcesDirectory()
				.listFiles(new FileFilter() {
					@Override
					public boolean accept(final File file) {
						return file.isFile();
					}
				});

		for (File resource : interviewResources) {
			commandArgumentList.add(resource.getAbsolutePath());
		}
		return commandArgumentList;
	}

}
