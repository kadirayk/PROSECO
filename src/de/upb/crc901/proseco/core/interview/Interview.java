package de.upb.crc901.proseco.core.interview;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.upb.crc901.proseco.view.util.ListUtil;

/**
 * Interview is defined by states and their conditional transitions each state of the interview can have multiple form inputs
 *
 * @author kadirayk
 *
 */
public class Interview implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -9198421035407778684L;

	private String questionRepo;
	private List<State> states;
	private Map<String, State> stateMap;

	/**
	 * Returns question with the given path i.e. "step1.q1"
	 *
	 * @param path
	 * @return
	 */
	public Question getQuestionByPath(final String path) {
		Question q = null;
		if (path.contains(".")) {
			String state = path.split("\\.")[0];
			String question = path.split("\\.")[1];
			State s = this.stateMap.get(state);
			q = s.getQuestionById(question);
		}

		return q;

	}

	public String getQuestionRepo() {
		return this.questionRepo;
	}

	public void setQuestionRepo(final String questionRepo) {
		this.questionRepo = questionRepo;
	}

	public List<State> getStates() {
		return this.states;
	}

	public void setStates(final List<State> states) {
		if (ListUtil.isNotEmpty(states)) {
			this.states = states;
			this.stateMap = new HashMap<>();
			for (State s : states) {
				this.stateMap.put(s.getName(), s);
			}
		}
	}

	public Map<String, State> getStateMap() {
		return this.stateMap;
	}

}
