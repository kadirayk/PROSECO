package de.upb.crc901.proseco.commons.controller;

import de.upb.crc901.proseco.commons.interview.Question;

/**
 * Interface describing answer extraction from an interview
 *
 * @author kadirayk
 *
 * @param <T>
 */
public interface IInterviewAnswerExtractor<T> {

	/**
	 *
	 * @param contextDescription
	 * @param question
	 * @return
	 */
	String getAnswer(T contextDescription, Question question);

}
