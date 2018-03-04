package de.upb.crc901.proseco.view.core.model.html;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 *  
 * @author kadirayk
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
@JsonSubTypes({ @JsonSubTypes.Type(value = Input.class, name = "Input"),
		@JsonSubTypes.Type(value = Select.class, name = "Select"),
		@JsonSubTypes.Type(value = Option.class, name = "Option") })
public abstract class UIElement implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5890195807308722546L;
	private String tag;
	private String content;
	private Map<String, String> attributes;

	public String getTag() {
		return tag;
	}

	protected void setTag(String tag) {
		this.tag = tag;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public UIElement() {
	}

	public String toHTML() {
		StringBuilder html = new StringBuilder("<");
		html.append(tag);
		if (attributes != null) {
			boolean isFile = false;
			if ("file".equals(attributes.get("type"))) {
				isFile = true;
			}
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				if (!isFile && entry.getKey().equals("name")) {
					entry.setValue("response");
				} else if (isFile && entry.getKey().equals("name")) {
					entry.setValue("file");
				}
				html.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
			}
		}
		html.append(">");
		if (content != null) {
			html.append(content);
		}
		html.append("</").append(tag).append(">");
		return html.toString();
	}

}
