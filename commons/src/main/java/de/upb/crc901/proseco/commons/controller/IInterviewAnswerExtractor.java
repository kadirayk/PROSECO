package de.upb.crc901.proseco.commons.controller;

import de.upb.crc901.proseco.commons.interview.Question;

/**
 * Interface describing answer extraction from an interview
 *
 * @author kadirayk
 *
 * @param <T> context description
 */
public interface IInterviewAnswerExtractor<T> {

	/**
	 * Returns an answer from the given contextDescription and a question
	 * 
	 * @param contextDescription
	 * @param question
	 * @return
	 */
	String getAnswer(T contextDescription, Question question);

}
