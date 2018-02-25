package de.upb.crc901.proseco.view.core;

public abstract class Node {
	private String value;

	public Node(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
