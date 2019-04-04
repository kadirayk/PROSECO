package de.upb.crc901.proseco.commons.processstatus;

public enum EProcessState {

	DOMAIN_DEFINITION("domain"), INTERVIEW("interview"), SEARCH_STRATEGIES("search"), GROUNDING("grounding"), DEPLOYMENT("deployment"), DONE("done");

	private String name;

	private EProcessState(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
