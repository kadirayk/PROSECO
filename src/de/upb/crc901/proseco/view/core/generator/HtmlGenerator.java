package de.upb.crc901.proseco.view.core.generator;

import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.util.FileUtil;

/**
 * HTML Generator for Interview definition Converts UI elements of the interview
 * to concrete HTML elements
 * 
 * @author kadirayk
 *
 */
public class HtmlGenerator {
	private String filePath;

	public HtmlGenerator(String filePath) {
		this.filePath = filePath;
	}

	public String generatePage(Interview interview) {
		StringBuilder html = new StringBuilder();

		if (interview != null) {
			html.append("<html>\n");
			html.append("<body>\n");
			html.append(interview.toHTML());
			html.append("</body\n>");
			html.append("</html>");
		}

		FileUtil.writeToFile(filePath + interview.getPrototypeName() + ".html", html.toString());

		return html.toString();
	}

}
