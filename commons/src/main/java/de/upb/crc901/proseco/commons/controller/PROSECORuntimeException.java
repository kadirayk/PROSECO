package de.upb.crc901.proseco.commons.controller;

/**
 * Notifies a runtime exception
 *
 */
public class PROSECORuntimeException extends RuntimeException {

	/**
	 *
	 * @param msg message
	 * @param e exception
	 */
	public PROSECORuntimeException(final String msg, final Exception e) {
		super(msg, e);
	}

	/**
	 *
	 * @param msg message
	 */
	public PROSECORuntimeException(final String msg) {
		super(msg);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -1170425841486603201L;

}
