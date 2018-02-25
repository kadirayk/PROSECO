package de.upb.crc901.proseco.view.core;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.Question;
import de.upb.crc901.proseco.view.core.model.QuestionCollection;
import de.upb.crc901.proseco.view.core.model.State;
import de.upb.crc901.proseco.view.util.ListUtil;

public class Parser {

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
							if(question!=null){
								q.setContent(question.getContent());
								q.setUiElement(question.getUiElement());	
							}
							questionSet.add(questionSetItem.toString());
						}
					}
				}

			}
			String id = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
			interview.setId(id);

		}

		return interview;
	}

}
