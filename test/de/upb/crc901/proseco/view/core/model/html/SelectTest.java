package de.upb.crc901.proseco.view.core.model.html;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import de.upb.crc901.proseco.view.html.Option;
import de.upb.crc901.proseco.view.html.Select;

public class SelectTest {

	@Test
	public void toHTMLTest() {
		Map<String, String> attributes = new HashMap<>();
		attributes.put("name", "selectName");

		List<Option> options = new ArrayList<>();

		Map<String, String> option1Attributes = new HashMap<>();
		option1Attributes.put("value", "option1");
		options.add(new Option("Value1", option1Attributes));

		Map<String, String> option2Attributes = new HashMap<>();
		option2Attributes.put("value", "option2");
		options.add(new Option("Value2", option2Attributes));

		Select select = new Select(null, attributes, options);

		String actual = select.toHTML();
		String expected = "<select name=\"response\">"
				+ "\n\t<option value=\"option1\">Value1</option>"
				+ "\n\t<option value=\"option2\">Value2</option>"
				+ "\n</select>";

		assertEquals(expected, actual);

	}

}
