package de.upb.crc901.proseco.view.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.upb.crc901.proseco.view.core.AnswerInterpreter;
import de.upb.crc901.proseco.view.core.NextStateNotFoundException;
import de.upb.crc901.proseco.view.util.ListUtil;

public class Interview implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9198421035407778684L;

	private String context;
	private String questionRepo;
	private List<State> states;
	private Map<String, State> stateMap;
	private State currentState;
	private String id;
	private Set<String> questionSet;

	/*
	 * "step1.q1"
	 */
	public Question getQuestionByPath(String path) {
		Question q = null;
		if (path.contains(".")) {
			String state = path.split("\\.")[0];
			String question = path.split("\\.")[1];
			State s = stateMap.get(state);
			q = s.getQuestionById(question);
		}

		return q;

	}

	public Set<String> getQuestionSet() {
		return questionSet;
	}

	public void setQuestionSet(Set<String> questionSet) {
		this.questionSet = questionSet;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
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

	public State getCurrentState() {
		return currentState;
	}

	public List<State> getStates() {
		return states;
	}

	public void setStates(List<State> states) {
		this.states = states;
		if (ListUtil.isNotEmpty(states)) {
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
