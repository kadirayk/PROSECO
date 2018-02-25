package de.upb.crc901.proseco.view.core.model.html;

import java.util.Map;

public class Option extends UIElement {

	private static final String TAG = "option";

	/**
	 * empty constructor needed for YAML parser
	 */
	public Option() {
		setTag(TAG);
	}

	public Option(String content, Map<String, String> attributes) {
		setTag(TAG);
		setContent(content);
		setAttributes(attributes);
	}

}
