package de.upb.crc901.proseco.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import de.upb.crc901.proseco.prototype.ExecutionEnvironment;
import de.upb.crc901.proseco.util.Config;

/**
 * 
 * BootUpInternalBenchmarkServiceCommand, finds the internal benchmark service
 * <code>(benchmarkService.bat)</code> executable file of the selected
 * prototype, and starts it as a process. Output and Error streams of this
 * process is redirected to console.
 * 
 * @author kadirayk
 *
 */
public class BootUpInternalBenchmarkServiceCommand implements Command {
	private ExecutionEnvironment executionEnvironment;
	private Process internalBenchmarkService;

	public BootUpInternalBenchmarkServiceCommand(ExecutionEnvironment executionEnvironment) {
		this.executionEnvironment = executionEnvironment;
	}

	@Override
	public void execute() throws Exception {
		System.out.print("Boot up internal benchmark service...");

		final File benchmarkExec = new File(executionEnvironment.getExecutionDirectory().getAbsolutePath() + "/"
				+ Config.INTERNAL_BENCHMARK_FOLDER + Config.BENCHMARK_SERVICE);
		final ProcessBuilder pb = new ProcessBuilder(benchmarkExec.getAbsolutePath()).redirectOutput(Redirect.INHERIT)
				.redirectError(Redirect.INHERIT);

		try {
			this.internalBenchmarkService = pb.start();

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(this.internalBenchmarkService.getErrorStream()))) {
				String line;
				boolean serviceUpAndRunning = false;
				while (!serviceUpAndRunning && ((line = br.readLine()) != null)) {
					if (line.contains("Service up and running")) {
						serviceUpAndRunning = true;
					}
				}
			}
			System.out.println("DONE.");

		} catch (final IOException e) {
			System.err.println("ERROR: Could not boot benchmark service.");
			System.exit(1);
		}

	}

	public Process getInternalBenchmarkService() {
		return internalBenchmarkService;
	}

}
