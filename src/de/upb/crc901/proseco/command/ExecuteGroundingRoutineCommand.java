package de.upb.crc901.proseco.command;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;

/**
 * 
 * ExecuteGroundingRoutineCommand, finds the grounding routine
 * <code>(groundingroutine.bat)</code> executable file of the selected
 * prototype, and starts it as a process. Output and Error streams of this
 * process is redirected to <code>service.log</code> file.
 * 
 * @author kadirayk
 *
 */
public class ExecuteGroundingRoutineCommand implements Command {
	private ExecutionEnvironment executionEnvironment;

	public ExecuteGroundingRoutineCommand(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	@Override
	public void execute() throws Exception {
		File groundingFolder = executionEnvironment.getGroundingDirectory();
		File groundingLog = new File(groundingFolder.getAbsolutePath() + File.separator + Config.SERVICE_LOG_FILE);
		final ProcessBuilder pb = new ProcessBuilder(executionEnvironment.getGroundingFile().getAbsolutePath())
				.redirectOutput(Redirect.appendTo(groundingLog)).redirectError(Redirect.appendTo(groundingLog));
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
