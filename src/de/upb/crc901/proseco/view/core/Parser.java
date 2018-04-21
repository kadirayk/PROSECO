package de.upb.crc901.proseco.view.core;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.Question;
import de.upb.crc901.proseco.view.core.model.QuestionCollection;
import de.upb.crc901.proseco.view.core.model.State;
import de.upb.crc901.proseco.view.core.model.html.Input;
import de.upb.crc901.proseco.view.util.ListUtil;

/**
 * Interview Parser utility
 * 
 * @author kadirayk
 *
 */
public class Parser {

	/**
	 * Parses question repository with the given path
	 * 
	 * @param filePath
	 * @return
	 */
	public QuestionCollection parseQuestion(String filePath) {
		QuestionCollection qCollection = null;
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			qCollection = mapper.readValue(new File(filePath), QuestionCollection.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return qCollection;

	}

	/**
	 * Parses interview definition with the given path and returns interview
	 * object
	 * 
	 * @param filePath
	 * @return
	 */
	public Interview parseInterview(String filePath) {
		Interview interview = null;
		ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
		try {
			interview = mapper.readValue(new File(filePath), Interview.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (interview != null) {
			String questionPath = interview.getQuestionRepo();

			QuestionCollection qCollection = parseQuestion(questionPath);

			Set<String> questionSet = new HashSet<>();
			interview.setQuestionSet(questionSet);

			if (qCollection != null) {

				for (State s : interview.getStates()) {
					List<Question> questions = s.getQuestions();
					if (ListUtil.isNotEmpty(questions)) {
						for (Question q : questions) {
							StringBuilder questionSetItem = new StringBuilder(s.getName());
							questionSetItem.append(".").append(q.getId());
							String qId = q.getQuestionId();
							Question question = qCollection.getQuestionById(qId);
							if (question != null) {
								q.setContent(question.getContent());
								q.setUiElement(question.getUiElement());
							}
							questionSet.add(questionSetItem.toString());
						}
					}
				}

			}
			setTimeOutQuestion(interview);
			String id = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
			interview.setId(id);

		}

		return interview;
	}

	private void setTimeOutQuestion(Interview interview) {
		List<State> states = interview.getStates();
		State stateBeforeTimeout = states.get(states.size() - 2);
		State stateAfterTimeout = states.get(states.size() - 1);
		Map<String, String> transition = new HashMap<>();
		transition.put("default", "timeout");
		stateBeforeTimeout.setTransition(transition);
		State timeOutState = new State();
		timeOutState.setName("timeout");
		Map<String, String> timeoutTransition = new HashMap<>();
		timeoutTransition.put("default", stateAfterTimeout.getName());
		timeOutState.setTransition(timeoutTransition);
		List<Question> questions = new ArrayList<>();
		Question q = new Question();
		q.setId("timeout");
		q.setQuestionId("timeout_1");
		q.setContent("Please specify a time out value in seconds");
		Input input = new Input();
		Map<String, String> attributeMap = new HashMap<>();
		attributeMap.put("name", "timeout");
		attributeMap.put("type", "number");
		input.setAttributes(attributeMap);
		q.setUiElement(input);
		questions.add(q);
		timeOutState.setQuestions(questions);
		states.add(timeOutState);
		Map<String, State> stateMap = interview.getStateMap();
		stateMap.put("timeout", timeOutState);
		Set<String> questionSet = interview.getQuestionSet();
		questionSet.add("timeout.timeout");
	}

}
