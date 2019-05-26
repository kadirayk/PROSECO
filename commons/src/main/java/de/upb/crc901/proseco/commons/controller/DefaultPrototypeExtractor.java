package de.upb.crc901.proseco.commons.controller;

import java.util.Map;

public class DefaultPrototypeExtractor implements IPrototypeExtractor {

	@Override
	public String getPrototype(String domain, Map<String, String> answers) throws PrototypeCouldNotBeExtractedException {
		// TODO get available protoypes for domain and compare if detected prototype
		// exist for domain
		String prototype = null;
		if (answers != null) {
			prototype = answers.get("Please select prototype");
			if (prototype == null) {
				prototype = answers.get("prototype");
			}
		}
		if (prototype == null) {
			throw new PrototypeCouldNotBeExtractedException();
		}
		return prototype;
	}

}
