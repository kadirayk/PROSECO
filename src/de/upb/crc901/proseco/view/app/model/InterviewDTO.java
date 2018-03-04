package de.upb.crc901.proseco.view.app.model;

import java.util.List;

import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.Question;
import de.upb.crc901.proseco.view.core.model.State;
import de.upb.crc901.proseco.view.core.model.html.HTMLConstants;
import de.upb.crc901.proseco.view.util.ConfUtil;
import de.upb.crc901.proseco.view.util.ListUtil;

/**
 * Interview Data Transfer Object class
 * 
 * 
 * @author kadirayk
 *
 */
public class InterviewDTO {

	private String content;

	private Interview interview;

	private String interviewHTML;

	private String debugHTML;

	private String id;

	private boolean showSubmit;

	private boolean showConsole;

	private boolean upload;

	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
	}

	public boolean isShowConsole() {
		return showConsole;
	}

	public void setShowConsole(boolean showConsole) {
		this.showConsole = showConsole;
	}

	public boolean isShowSubmit() {
		return showSubmit;
	}

	public void setShowSubmit(boolean showSubmit) {
		this.showSubmit = showSubmit;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Interview getInterview() {
		return interview;
	}

	/**
	 * Do not show submit button for questions without uiElements (i.e. pages
	 * that do not require input)
	 */
	private void setShowSubmitValue() {
		List<Question> questions = interview.getCurrentState().getQuestions();
		if (ListUtil.isNotEmpty(questions)) {
			for (Question q : questions) {
				if (q.getUiElement() != null) {
					showSubmit = true;
				}
			}
		}

	}

	public void setInterview(Interview interview) {
		this.interview = interview;
		this.id = interview.getId();
		this.interviewHTML = interview.getCurrentState().toHTML();
		setShowSubmitValue();

		setDebugHtml(interview);
	}

	/**
	 * Creates debug HTML table for interview
	 * 
	 * @param interview
	 */
	private void setDebugHtml(Interview interview) {
		if (ConfUtil.getValue(ConfUtil.DEBUG)) {
			StringBuilder htmlElement = new StringBuilder();
			htmlElement.append("<div>");
			htmlElement.append(HTMLConstants.LINE_BREAK).append("Debug: ");
			htmlElement.append("<table style=\"width:30%\" border=\"1\"><tr><th>").append("State").append("</th><th>")
					.append("question").append("</th><th>").append("answer").append("</th></tr>");
			for (State state : interview.getStates()) {
				htmlElement.append("<tr");
				if (state.getName().equals(interview.getCurrentState().getName())) {
					htmlElement.append(" bgcolor=\"#b4ff99\" ");
				}
				htmlElement.append("><td rowspan=\"").append(state.getQuestions().size()).append("\">")
						.append(state.getName()).append("</td>");

				boolean isFirstLoop = false;
				for (Question question : state.getQuestions()) {
					if (isFirstLoop) {
						htmlElement.append("<tr");
						if (state.getName().equals(interview.getCurrentState().getName())) {
							htmlElement.append(" bgcolor=\"#b4ff99\" ");
						}
						htmlElement.append(">");
					}
					htmlElement.append("<td>").append(question.getId()).append("</td><td>");
					if (question.getAnswer() != null) {
						htmlElement.append(question.getAnswer());
					}
					htmlElement.append("</td></tr>");
					isFirstLoop = true;
				}

			}
			htmlElement.append("</table>").append("</div>");
			this.debugHTML = htmlElement.toString();
		}
	}

	public String getInterviewHTML() {
		return interviewHTML;
	}

	public void setInterviewHTML(String interviewHTML) {
		this.interviewHTML = interviewHTML;
	}

	public String getDebugHTML() {
		return debugHTML;
	}

	public void setDebugHTML(String debugHTML) {
		this.debugHTML = debugHTML;
	}

}
