package de.upb.crc901.proseco.commons.controller;

import java.util.Map;

/**
 * Interface describing property extraction
 *
 * @author kadirayk
 *
 */
public interface IPrototypeExtractor {

	/**
	 * Returns a prototype name which is extracted from the given answers and a domain name
	 *
	 * @param domain
	 * @param answers
	 * @return prototype name
	 * @throws PrototypeCouldNotBeExtractedException
	 */
	String getPrototype(String domain, Map<String, String> answers) throws PrototypeCouldNotBeExtractedException;

}
