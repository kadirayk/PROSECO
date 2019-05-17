package de.upb.crc901.proseco.commons.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.upb.crc901.proseco.commons.config.PROSECOConfig;

public class DefaultDomainScoreComputer implements IDomainScoreComputer<String> {

	@Override
	public Double getDomainScore(String description, String domain) throws DomainCouldNotBeDetectedException {
		if (StringUtils.isEmpty(description) || StringUtils.isEmpty(domain)) {
			throw new DomainCouldNotBeDetectedException();
		}
		if (description.contains(domain)) {
			return 1.0;
		}
		return 0.0;
	}

	public List<String> getAvailableDomains(PROSECOConfig prosecoConfig) {
		List<String> domains = new ArrayList<>();
		File domainsDir = prosecoConfig.getDirectoryForDomains();
		for (File domain : domainsDir.listFiles()) {
			domains.add(domain.getName());
		}
		return domains;
	}

}
