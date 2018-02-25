package de.upb.crc901.proseco.view.core.model.html;

import java.util.Map;

public class Input extends UIElement {
	private static final String TAG = "input";

	public Input() {
		setTag(TAG);
	}

	public Input(String content, Map<String, String> attributes) {
		setTag(TAG);
		setContent(content);
		setAttributes(attributes);
	}

}
