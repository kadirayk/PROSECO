package de.upb.crc901.proseco.command;

/**
 * Command, interface for
 * <a href="https://en.wikipedia.org/wiki/Command_pattern">Command Pattern</a>
 * 
 * @author kadirayk
 *
 */
public interface Command {
	/**
	 * A command should be executable
	 * 
	 * @throws Exception
	 */
	public void execute() throws Exception;
}
