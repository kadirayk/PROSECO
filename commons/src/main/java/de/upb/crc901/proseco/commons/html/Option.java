package de.upb.crc901.proseco.commons.html;

import java.util.Map;

/**
 * HTML Option element &lt;option\&gt;
 * 
 * @author kadirayk
 *
 */
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
