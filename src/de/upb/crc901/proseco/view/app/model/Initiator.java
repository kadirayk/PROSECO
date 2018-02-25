package de.upb.crc901.proseco.view.app.model;

import java.util.List;

import de.upb.crc901.proseco.view.core.model.Interview;
import de.upb.crc901.proseco.view.core.model.Question;
import de.upb.crc901.proseco.view.core.model.State;
import de.upb.crc901.proseco.view.core.model.html.HTMLConstants;
import de.upb.crc901.proseco.view.util.ConfUtil;
import de.upb.crc901.proseco.view.util.ListUtil;

public class Initiator {

	private String content;

	private Interview interview;

	private String interviewHTML;

	private String debugHTML;

	private String id;

	private boolean showSubmit;

	private boolean upload;

	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
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

	private void setIsUploadValue() {
		List<Question> questions = interview.getCurrentState().getQuestions();
		if (ListUtil.isNotEmpty(questions)) {
			for (Question q : questions) {
				if (q.getUiElement() != null) {
					if (q.getUiElement().getAttributes() != null) {
						if ("file".equals(q.getUiElement().getAttributes().get("type"))) {
							upload = true;
						}
					}
				}
			}
		}
	}

	public void setInterview(Interview interview) {
		this.interview = interview;
		this.id = interview.getId();
		this.interviewHTML = interview.getCurrentState().toHTML();
		setShowSubmitValue();

		StringBuilder htmlElement = new StringBuilder();

		if (ConfUtil.getValue(ConfUtil.DEBUG)) {
			htmlElement.append("<div style=\"position:fixed;bottom:0;margin-bottom:50px;width:100%\">");
			htmlElement.append(HTMLConstants.LINE_BREAK).append("Debug: ");
			htmlElement.append("<table style=\"width:30%\" border=\"1\"><tr><th>").append("State").append("</th><th>")
					.append("question").append("</th><th>").append("answer").append("</th></tr>");
			for (State state : interview.getStates()) {
				htmlElement.append("<tr");
				if (state == interview.getCurrentState()) {
					htmlElement.append(" bgcolor=\"#b4ff99\" ");
				}
				htmlElement.append("><td rowspan=\"").append(state.getQuestions().size()).append("\">")
						.append(state.getName()).append("</td>");

				boolean isFirstLoop = false;
				for (Question question : state.getQuestions()) {
					if (isFirstLoop) {
						htmlElement.append("<tr");
						if (state == interview.getCurrentState()) {
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
