package de.upb.crc901.proseco.commons.processstatus;

public class InvalidStateTransitionException extends Exception {
	public InvalidStateTransitionException() {
		super();
	}
	
	public InvalidStateTransitionException(String msg) {
		super(msg);
	}

}