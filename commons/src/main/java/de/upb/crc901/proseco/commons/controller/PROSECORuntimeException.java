package de.upb.crc901.proseco.commons.controller;

public class PROSECORuntimeException extends RuntimeException {
	public PROSECORuntimeException(String msg, Exception e) {
		super(msg, e);
	}

	public PROSECORuntimeException(String msg) {
		super(msg);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -1170425841486603201L;

}
