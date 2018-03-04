package de.upb.crc901.proseco.view.core.model;

import java.io.Serializable;

import de.upb.crc901.proseco.view.core.model.html.UIElement;

/**
 * Question is the building block of Interview which consists of UI element,
 * question content, and answer
 * 
 * @author kadirayk
 *
 */
public class Question implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7211131201645636546L;
	private String id;
	private String questionId;
	private String content;
	private UIElement uiElement;
	private String answer;

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

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

}
