package de.upb.crc901.proseco.view.core.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.upb.crc901.proseco.view.core.Parser;

public class QuestionTest {
	Parser parser;

	@Before
	public void init() {
		parser = new Parser();
	}

//	@Test
	public void parseTest() {
		String filePath = "test/data/ml_questions.yaml";
		QuestionCollection formCollection = parser.parseQuestion(filePath);
		String firstFormId = formCollection.getQuestions().get(0).getId();

		assertEquals("ml_q1", firstFormId);

		String secondQuestions = formCollection.getQuestions().get(1).getContent();

		assertEquals("What is the benefit of your project?", secondQuestions);

	}

}
