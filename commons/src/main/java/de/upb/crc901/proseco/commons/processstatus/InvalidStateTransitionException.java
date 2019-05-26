package de.upb.crc901.proseco.commons.processstatus;

public class InvalidStateTransitionException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 4385610322796895379L;

	public InvalidStateTransitionException() {
		super();
	}

	public InvalidStateTransitionException(String msg) {
		super(msg);
	}

}
