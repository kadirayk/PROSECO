package de.upb.crc901.proseco.view.app.model;

import java.util.List;

import de.upb.crc901.proseco.core.interview.InterviewFillout;
import de.upb.crc901.proseco.core.interview.Question;
import de.upb.crc901.proseco.core.interview.State;
import de.upb.crc901.proseco.view.html.HTMLConstants;
import de.upb.crc901.proseco.view.util.ListUtil;

/**
 * Interview Data Transfer Object class
 * 
 * 
 * @author kadirayk
 *
 */
public class InterviewDTO {

	private String processId; // the PROSECO service construction process id to which this interview belongs
	
	private String content;

	private InterviewFillout interviewFillout;

	private String interviewHTML;

	private String debugHTML;

	private boolean showSubmit;

	private boolean showConfigurationPane;
	
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

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String id) {
		this.processId = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public InterviewFillout getInterviewFillout() {
		return interviewFillout;
	}

	/**
	 * Do not show submit button for questions without uiElements (i.e. pages
	 * that do not require input)
	 */
	private void setShowSubmitValue() {
		List<Question> questions = interviewFillout.getCurrentState().getQuestions();
		if (ListUtil.isNotEmpty(questions)) {
			for (Question q : questions) {
				if (q.getUiElement() != null) {
					showSubmit = true;
				}
			}
		}

	}

	public void setInterviewFillout(InterviewFillout interviewFillout) {
		this.interviewFillout = interviewFillout;
		this.interviewHTML = interviewFillout.getHTMLOfOpenQuestionsInCurrentState();
		setShowSubmitValue();
		setDebugHtml(interviewFillout);
	}

	/**
	 * Creates debug HTML table for interview
	 * 
	 * @param interview
	 */
	private void setDebugHtml(InterviewFillout interviewFillout) {
			StringBuilder htmlElement = new StringBuilder();
			htmlElement.append("<div>");
			htmlElement.append(HTMLConstants.LINE_BREAK).append("Debug: ");
			htmlElement.append("<table style=\"width:30%\" border=\"1\"><tr><th>").append("State").append("</th><th>")
					.append("qId").append("</th><th>").append("Question").append("</th><th>").append("answer").append("</th></tr>");
			for (State state : interviewFillout.getInterview().getStates()) {
				if (state.getName().equals("timeout")) {
					continue;
				}
				htmlElement.append("<tr");
				if (state.getName().equals(interviewFillout.getCurrentState().getName())) {
					htmlElement.append(" bgcolor=\"#b4ff99\" ");
				}
				htmlElement.append("><td rowspan=\"").append(state.getQuestions().size()).append("\">")
						.append(state.getName()).append("</td>");

				boolean isFirstLoop = false;
				for (Question question : state.getQuestions()) {
					if (isFirstLoop) {
						htmlElement.append("<tr");
						if (state.getName().equals(interviewFillout.getCurrentState().getName())) {
							htmlElement.append(" bgcolor=\"#b4ff99\" ");
						}
						htmlElement.append(">");
					}
					htmlElement.append("<td>").append(question.getId()).append("</td>");
					htmlElement.append("<td>").append(question.getContent()).append("</td>");
					htmlElement.append("<td>");
					if (interviewFillout.getAnswers().containsKey(question.getId())) {
						htmlElement.append(interviewFillout.getAnswers().get(question.getId()));
					}
					htmlElement.append("</td></tr>");
					isFirstLoop = true;
				}

			}
			htmlElement.append("</table>").append("</div>");
			this.debugHTML = htmlElement.toString();
	}

	public boolean isShowConfigurationPane() {
		return showConfigurationPane;
	}

	public void setShowConfigurationPane(boolean showConfigurationPane) {
		this.showConfigurationPane = showConfigurationPane;
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

	@Override
	public String toString() {
		return "InterviewDTO [processId=" + processId + ", content=" + content + ", interviewFillout=" + interviewFillout + ", interviewHTML=" + interviewHTML + ", debugHTML="
				+ debugHTML + ", showSubmit=" + showSubmit + ", showConsole=" + showConsole + ", upload=" + upload + "]";
	}
}
