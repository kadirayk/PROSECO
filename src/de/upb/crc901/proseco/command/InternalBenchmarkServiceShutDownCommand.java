package de.upb.crc901.proseco.command;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class InternalBenchmarkServiceShutDownCommand implements Command {
	private Process internalBenchmarkService;

	public InternalBenchmarkServiceShutDownCommand(Process internalBenchmarkService) {
		this.internalBenchmarkService = internalBenchmarkService;
	}

	@Override
	public void execute() throws Exception {
		System.out.print("Shutdown internal benchmark service...");
		try (BufferedWriter bw = new BufferedWriter(
				new OutputStreamWriter(this.internalBenchmarkService.getOutputStream()))) {
			bw.write("q\n");
		} catch (final IOException e1) {
			e1.printStackTrace();
		}

		try {
			this.internalBenchmarkService.waitFor();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("DONE.");

	}

}
