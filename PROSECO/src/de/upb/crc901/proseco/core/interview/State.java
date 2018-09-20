package de.upb.crc901.proseco.core.interview;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import de.upb.crc901.proseco.view.util.ListUtil;

/**
 * 
 * @author fmohr
 *
 */
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

	public Map<String, String> getTransition() {
		return transition;
	}

	public void setTransition(Map<String, String> transition) {
		this.transition = transition;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((questions == null) ? 0 : questions.hashCode());
		result = prime * result + ((transition == null) ? 0 : transition.hashCode());
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
		State other = (State) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (questions == null) {
			if (other.questions != null)
				return false;
		} else if (!questions.equals(other.questions))
			return false;
		if (transition == null) {
			if (other.transition != null)
				return false;
		} else if (!transition.equals(other.transition))
			return false;
		return true;
	}

}
