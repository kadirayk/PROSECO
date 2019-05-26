package de.upb.crc901.proseco.commons.controller;

public class ProcessIdAlreadyExistsException extends Exception {
	/**
	 *
	 */
	private static final long serialVersionUID = 7958112245728714324L;

	public ProcessIdAlreadyExistsException(String errorMessage) {
		super(errorMessage);
	}

	public ProcessIdAlreadyExistsException() {
		super();
	}

}
