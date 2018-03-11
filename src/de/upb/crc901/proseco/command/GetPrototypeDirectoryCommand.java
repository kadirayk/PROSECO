package de.upb.crc901.proseco.command;

import java.io.File;

import de.upb.crc901.proseco.util.Config;
import de.upb.crc901.proseco.util.Messages;

public class GetPrototypeDirectoryCommand implements Command {
	private String prototypeName;
	private File prototypeDirectory;

	public GetPrototypeDirectoryCommand(String prototypeName) {
		this.prototypeName = prototypeName;
		this.prototypeDirectory = new File(Config.PROTOTYPES.getAbsolutePath() + File.separator + this.prototypeName);
	}

	@Override
	public void execute() throws Exception {
		if (!this.prototypeDirectory.exists() || !prototypeDirectory.isDirectory()) {
			throw new Exception(Messages.NO_SUCH_PROTOTYPE);
		}

	}

	public File getPrototypeDirectory() {
		return prototypeDirectory;
	}

}
