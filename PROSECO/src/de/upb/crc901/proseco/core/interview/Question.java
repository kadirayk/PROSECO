package de.upb.crc901.proseco.core.interview;

import java.io.Serializable;

import de.upb.crc901.proseco.view.html.UIElement;

/**
 * Question is the building block of Interview which consists of UI element,
 * question content, and answer
 * 
 * @author kadirayk
 *
 */
@SuppressWarnings("serial")
public class Question implements Serializable {

	/**
	 * 
	 */
	private String id;
	private String content;
	private UIElement uiElement;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public UIElement getUiElement() {
		return uiElement;
	}

	public void setUiElement(UIElement uiElement) {
		this.uiElement = uiElement;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((uiElement == null) ? 0 : uiElement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (uiElement == null) {
			if (other.uiElement != null)
				return false;
		} else if (!uiElement.equals(other.uiElement))
			return false;
		return true;
	}
}
