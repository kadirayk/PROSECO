package de.upb.crc901.proseco.command;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;

import de.upb.crc901.proseco.PrototypeBasedComposer;
import de.upb.crc901.proseco.view.util.ListUtil;

public class ShutDownPrototypeProcessesCommand implements Command {

	private List<Process> strategyProcessList;
	private Process benckmarkService;

	public ShutDownPrototypeProcessesCommand(String prototypeId) {
		this.strategyProcessList = PrototypeBasedComposer.prototypeProcesses.get(prototypeId);
		this.benckmarkService = PrototypeBasedComposer.prototypeBenchmarkProcess.get(prototypeId);

	}

	@Override
	public void execute() throws Exception {

		if (this.benckmarkService != null && this.benckmarkService.isAlive()) {
			this.benckmarkService.destroy();
			if (this.benckmarkService.isAlive()) {
				this.benckmarkService.destroyForcibly();
			}
		}

		if (ListUtil.isNotEmpty(this.strategyProcessList)) {
			for (final Process p : this.strategyProcessList) {
				if (p.isAlive()) {
					p.destroy();
					if (p.isAlive()) {
						p.destroyForcibly();
					}
				}
			}
		}
	}

}
