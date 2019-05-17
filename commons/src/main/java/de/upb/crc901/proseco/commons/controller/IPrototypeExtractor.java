package de.upb.crc901.proseco.commons.controller;

import java.util.Map;

public interface IPrototypeExtractor {

	String getPrototype(String domain, Map<String, String> answers) throws PrototypeCouldNotBeExtractedException;

}
