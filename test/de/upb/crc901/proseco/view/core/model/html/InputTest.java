package de.upb.crc901.proseco.view.core.model.html;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class InputTest {

	@Test
	public void toHTMLTest() {
		Map<String, String> attributes = new HashMap<>();
		attributes.put("name", "inputName");
		attributes.put("type", "text");
		Input input = new Input(null, attributes);

		String actual = input.toHTML();
		String expected = "<input name=\"response\" type=\"text\"></input>";

		assertEquals(expected, actual);

	}

}
