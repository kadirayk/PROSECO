package de.upb.crc901.proseco.commons.controller;

import java.io.File;
import java.util.Map;

public class DefaultPrototypeExtractor implements IPrototypeExtractor {

	@Override
	public String getPrototype(String domain, Map<String, String> answers) throws PrototypeCouldNotBeExtractedException {
		String prototype = null;
		if (answers != null) {
			prototype = answers.get("Please select prototype");
			if (prototype == null) {
				prototype = answers.get("prototype");
			}
		}

		if (prototype == null || !prototypeExists(domain, prototype)) {
			throw new PrototypeCouldNotBeExtractedException();
		}
		return prototype;
	}
	
	private static boolean prototypeExists(String domain, String prototype) {
		boolean exists = false;
		File prototypesFolder = new File("domains/" + domain + "/prototypes");
		for (File file : prototypesFolder.listFiles()) {
			if (file.getName().equalsIgnoreCase(prototype)) {
				exists = true;
				break;
			}
		}
		return exists;
	}

}
