package de.upb.crc901.proseco.view.core;

/**
 * Answer Interpreter for interview questions. Answers are checked for
 * predefined conditions.
 * 
 * 
 * @author kadirayk
 * 
 *
 */
public class AnswerInterpreter {

	private AnswerInterpreter() {
	}

//	/**
//	 * Finds next state of the interview
//	 * 
//	 * 
//	 * @param interview
//	 * @param state
//	 * @return
//	 * @throws NextStateNotFoundException
//	 */
//	public static String findNextState(InterviewFillout interview, State state) throws NextStateNotFoundException {
//		Map<String, String> transition = state.getTransition();
//		for (Map.Entry<String, String> e : transition.entrySet()) {
//			String condition = e.getKey();
//			if (evaluate(interview, condition)) {
//				return e.getValue();
//			}
//		}
//		throw new NextStateNotFoundException();
//
//	}

//	/**
//	 * evaluates the correctness of the condition
//	 * 
//	 * @param interviewFillout
//	 * @param condition
//	 * @return
//	 */
//	private static boolean evaluate(InterviewFillout interviewFillout, String condition) {
//		if (condition.equals("default")) {
//			return true;
//		}
//		String content = getConditionContent(condition);
//		for (String s : interviewFillout.getInterview().getQuestionSet()) {
//			if (content.contains(s)) {
//				Question q = interviewFillout.getInterview().getQuestionByPath(s);
//				if (StringUtils.isEmpty(interviewFillout.getAnswer(q))) {
//					return false;
//				}
//				content = content.replace(s, interviewFillout.getAnswer(q));
//			} else if (content.contains(s.replace(interviewFillout.getCurrentState().getName() + ".", ""))) {
//				Question q = interviewFillout.getInterview().getQuestionByPath(s);
//				if (StringUtils.isEmpty(interviewFillout.getAnswer(q))) {
//					return false;
//				}
//				String answer = interviewFillout.getAnswer(q) == null ? "null" : interviewFillout.getAnswer(q);
//				content = content.replace(s.replace(interviewFillout.getCurrentState().getName() + ".", ""), answer);
//			}
//		}
//
//		ExpressionEvaluator ev = new ExpressionEvaluator(content);
//		return ev.evaluateExpression();
//
//	}
//
//	private static String getConditionContent(String condition) {
//		int start = getStartIndexOfValue(condition);
//		int end = getEndIndexOfValue(condition);
//		return condition.substring(start, end);
//	}
//
//	private static int getEndIndexOfValue(String condition) {
//		int index = condition.indexOf(']');
//		if (index == -1) {
//			throw new IllegalArgumentException(
//					"Closing bracket \"]\" is missing. This condition must contain a value to compare within brackets. i.e [value]");
//		}
//		return index;
//	}
//
//	private static int getStartIndexOfValue(String condition) {
//		int index = condition.indexOf('[');
//		if (index == -1) {
//			throw new IllegalArgumentException(
//					"Opening bracket \"[\" is missing. This condition must contain a value to compare within brackets. i.e [value]");
//		}
//		return index + 1;
//	}

}
