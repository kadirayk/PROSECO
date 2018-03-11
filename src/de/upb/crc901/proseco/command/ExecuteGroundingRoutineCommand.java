package de.upb.crc901.proseco.command;

import java.io.IOException;

import de.upb.crc901.proseco.prototype.ExecutionEnvironment;

public class ExecuteGroundingRoutineCommand implements Command {
	private ExecutionEnvironment executionEnvironment;

	public ExecuteGroundingRoutineCommand(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	@Override
	public void execute() throws Exception {
		final ProcessBuilder pb = new ProcessBuilder(executionEnvironment.getGroundingFile().getAbsolutePath());
		System.out.print("Execute grounding process...");
		Process p;
		try {
			p = pb.start();
			while (p.isAlive()) {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("DONE.");
	}

}
