package de.upb.crc901.proseco.view.core;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import de.upb.crc901.proseco.view.core.model.QuestionCollection;
import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.html.Input;
import de.upb.crc901.proseco.view.core.model.html.Select;
import de.upb.crc901.proseco.view.core.model.html.UIElement;

public class ParserTest {
	Parser parser;

	@Before
	public void init() {
		parser = new Parser();
	}

//	@Test
	public void parseInterviewTest() {
		String filePath = "test/data/ml_interview.yaml";
		Interview interview = parser.parseInterview(filePath);
		assertEquals("Machine Learning", interview.getPrototypeName());

		assertEquals("step1", interview.getStates().get(0).getName());

		// first ui element of first state is an html input element and its tpye
		// is text
		UIElement inputElement = interview.getStates().get(0).getQuestions().get(0).getUiElement();
		assertThat(inputElement, instanceOf(Input.class));

		Input inputField = (Input) inputElement;
		assertEquals("text", inputField.getAttributes().get("type"));

		// first ui element of second state is an html select element and its
		// first option is option1
		UIElement selectElement = interview.getStates().get(1).getQuestions().get(0).getUiElement();
		assertThat(selectElement, instanceOf(Select.class));

		Select selectField = (Select) selectElement;
		assertEquals("supervised", selectField.getOptions().get(0).getAttributes().get("value"));

		assertEquals("Supervised Learning", selectField.getOptions().get(0).getContent());

	}
	
	@Test
	public void parseGameInterviewTest() {
		String filePath = "test/data/game_interview.yaml";
		Interview interview = parser.parseInterview(filePath);
		
		String a = "";
	}

//	@Test
	public void testExceptionCase() {
		String filePath = "wrognpath";
		Interview interview = parser.parseInterview(filePath);
		assertEquals(null, interview);
		QuestionCollection formCollection = parser.parseQuestion(filePath);
		assertEquals(null, formCollection);
	}

}
