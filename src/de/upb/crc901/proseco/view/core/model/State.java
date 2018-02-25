package de.upb.crc901.proseco.view.core.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

import de.upb.crc901.proseco.view.core.model.html.HTMLConstants;
import de.upb.crc901.proseco.view.core.model.html.UIElement;
import de.upb.crc901.proseco.view.util.ListUtil;

public class State implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -849218511658141465L;
	private String name;
	private Map<String, String> transition;
	private List<Question> questions;

	public Question getQuestionById(String id) {
		Question question = null;
		if (ListUtil.isNotEmpty(questions)) {
			for (Question q : questions) {
				if (id.equals(q.getId())) {
					return q;
				}
			}
		}
		return question;
	}

	public List<Question> getQuestions() {
		return questions;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonAnyGetter
	public Map<String, String> getTransition() {
		return transition;
	}

	@JsonAnySetter
	public void setTransition(Map<String, String> transition) {
		this.transition = transition;
	}

	public String toHTML() {
		StringBuilder htmlElement = new StringBuilder();

		if (ListUtil.isNotEmpty(questions)) {
			for (Question q : questions) {
				String formQuestion = q.getContent();
				if (formQuestion != null) {
					htmlElement.append(HTMLConstants.LINE_BREAK).append(formQuestion).append(HTMLConstants.LINE_BREAK);
				}
				UIElement formUiElement = q.getUiElement();
				if (formUiElement != null) {
					htmlElement.append(formUiElement.toHTML()).append(HTMLConstants.LINE_BREAK).append("\n");
				}
			}
		}

		return htmlElement.toString();
	}

}
