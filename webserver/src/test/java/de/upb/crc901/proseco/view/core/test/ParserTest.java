package de.upb.crc901.proseco.view.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

import de.upb.crc901.proseco.commons.interview.Interview;
import de.upb.crc901.proseco.view.core.Parser;

public class ParserTest {
	Parser parser;

	@Before
	public void init() {
		parser = new Parser();
	}

	@Test
	public void parseInterviewTest() throws Exception {
		String filePath = "testdata/interview/interview.yaml";
		File file = new File(filePath);
		Interview interview = parser.initializeInterviewFromConfig(file);
		assertTrue(interview != null);

		assertEquals("step0", interview.getStates().get(0).getName());

		assertEquals("prototype", interview.getStates().get(0).getQuestions().get(0).getId());

	}

	@Test(expected = FileNotFoundException.class)
	public void testExceptionCase() throws Exception {
		String filePath = "wrongpath";
		File file = new File(filePath);
		parser.initializeInterviewFromConfig(file);
	}

}
