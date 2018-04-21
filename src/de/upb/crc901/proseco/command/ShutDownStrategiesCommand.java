package de.upb.crc901.proseco.command;

import java.util.List;

public class ShutDownStrategiesCommand implements Command {

	private List<Process> strategyProcessList;

	public ShutDownStrategiesCommand(List<Process> strategyProcessList) {
		this.strategyProcessList = strategyProcessList;
	}

	@Override
	public void execute() throws Exception {
		for (final Process p : this.strategyProcessList) {
			if (p.isAlive()) {
				p.destroyForcibly();
			}
		}
	}

}
