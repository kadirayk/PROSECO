package de.upb.crc901.proseco.commons.controller;

public interface IDomainScoreComputer<T> {

	/**
	 * 
	 * @param description
	 * @param domain
	 * @return confidence value between 0.0 - 1.0
	 * @throws DomainCouldNotBeDetectedException
	 */
	Double getDomainScore(T description, String domain) throws DomainCouldNotBeDetectedException;

}
