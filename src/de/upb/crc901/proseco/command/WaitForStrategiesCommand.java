package de.upb.crc901.proseco.command;

import java.util.List;

/**
 * WaitForStrategiesCommand, waits (busy waiting) for the processes to terminate
 * that have been started to execute the different strategies.
 * 
 * @author kadirayk
 *
 */
public class WaitForStrategiesCommand implements Command {
	private List<Process> strategyProcessList;

	public WaitForStrategiesCommand(List<Process> strategyProcessList) {
		this.strategyProcessList = strategyProcessList;
	}

	@Override
	public void execute() throws Exception {
		System.out.println("PBC: Wait for strategies to terminate.");
		boolean oneRunning = true;
		while (oneRunning) {
			oneRunning = false;

			for (final Process p : this.strategyProcessList) {
				if (p.isAlive()) {
					oneRunning = true;
					break;
				}
			}
			try {
				Thread.sleep(2000);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("PBC: Wait for strategies to terminate.");
	}

}
