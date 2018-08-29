package de.upb.crc901.proseco.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * ShutDownInternalBenchmarkServiceCommand, after execution of the strategies
 * completed it waits for internal benchmark service
 * <code>(benchmarkService.bat)</code> to terminate.
 * 
 * @author kadirayk
 *
 */
public class ShutDownInternalBenchmarkServiceCommand implements Command {
	private Process internalBenchmarkService;

	public ShutDownInternalBenchmarkServiceCommand(Process internalBenchmarkService) {
		this.internalBenchmarkService = internalBenchmarkService;
	}

	@Override
	public void execute() throws Exception {
		System.out.print("Shutdown internal benchmark service...");
		try (BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(this.internalBenchmarkService.getOutputStream()))) {
			bw.write("q\n");
		} catch (final IOException e1) {
			//e1.printStackTrace();
		}

		try {
			this.internalBenchmarkService.waitFor();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("DONE.");

	}

}
