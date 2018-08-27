package de.upb.crc901.proseco.view.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.upb.crc901.proseco.view.app.model.Resolution;
import de.upb.crc901.proseco.view.core.AnswerInterpreter;
import de.upb.crc901.proseco.view.core.NextStateNotFoundException;
import de.upb.crc901.proseco.view.util.ListUtil;

/**
 * Interview is defined by states and their conditional transitions each state
 * of the interview can have multiple form inputs
 * 
 * @author kadirayk
 *
 */
public class Interview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9198421035407778684L;

	private String prototypeName;
	private String questionRepo;
	private List<State> states;
	private Map<String, State> stateMap;
	private State currentState;
	private String id;
	private Set<String> questionSet;
	@JsonIgnore
	private Resolution resolution;

	/**
	 * Returns question with the given path i.e. "step1.q1"
	 * 
	 * @param path
	 * @return
	 */
	public Question getQuestionByPath(String path) {
		Question q = null;
		if (path.contains(".")) {
			String state = path.split("\\.")[0];
			String question = path.split("\\.")[1];
			State s = stateMap.get(state);
			if (s != null) {
				q = s.getQuestionById(question);
			}
		}

		return q;

	}

	public Set<String> getQuestionSet() {
		return questionSet;
	}

	public void setQuestionSet(Set<String> questionSet) {
		this.questionSet = questionSet;
	}

	public String getPrototypeName() {
		return prototypeName;
	}

	public void setPrototypeName(String prototypeName) {
		this.prototypeName = prototypeName;
	}

	public String getQuestionRepo() {
		return questionRepo;
	}

	public void setQuestionRepo(String questionRepo) {
		this.questionRepo = questionRepo;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Resolution getResolution() {
		return resolution;
	}

	public void setResolution(Resolution resolution) {
		this.resolution = resolution;
	}

	/**
	 * return currentState if every previous question is answered, if not return
	 * first state without answer
	 * 
	 * @return
	 */
	public State getCurrentState() {
		if (currentState != null) {
			// if current state has unanwered questions return current state
			List<Question> questions = currentState.getQuestions();
			if (ListUtil.isNotEmpty(questions)) {
				for (Question q : questions) {
					if (StringUtils.isEmpty(q.getAnswer())) {
						return currentState;
					}
				}
			}
		}
		if (ListUtil.isNotEmpty(this.states)) {
			// set current state to a state with unanswered question
			for (State s : this.states) {
				List<Question> questions = s.getQuestions();
				if (ListUtil.isNotEmpty(questions)) {
					for (Question q : questions) {
						if (StringUtils.isEmpty(q.getAnswer())) {
							currentState = s;
							return currentState;
						}
					}
				}
			}
		}
		return currentState;
	}

	public List<State> getStates() {
		return states;
	}

	public void setStates(List<State> states) {
		if (ListUtil.isNotEmpty(states)) {
			this.states = states;
			currentState = states.get(0);
			stateMap = new HashMap<>();
			for (State s : states) {
				stateMap.put(s.getName(), s);
			}
		}

	}

	public void nextState() throws NextStateNotFoundException {
		String nextStateName = AnswerInterpreter.findNextState(this, currentState);
		if (nextStateName != null) {
			currentState = stateMap.get(nextStateName);
		} // else there is no next step i.e. last step
	}

	public void prevState() {
		String nextStateName = stateMap.get(currentState.getName()).getTransition().get("prev");
		if (nextStateName != null) {
			currentState = stateMap.get(nextStateName);
		} // else there is no next step i.e. last step
	}

	public Map<String, State> getStateMap() {
		return stateMap;
	}

	public String toHTML() {
		StringBuilder html = new StringBuilder();
		for (State s : states) {
			html.append(s.toHTML()).append("\n");
		}
		return html.toString();
	}

}
