package de.upb.crc901.proseco.commons.controller;

import de.upb.crc901.proseco.commons.interview.Question;

public interface IInterviewAnswerExtractor<T> {

	String getAnswer(T contextDescription, Question question);

}
