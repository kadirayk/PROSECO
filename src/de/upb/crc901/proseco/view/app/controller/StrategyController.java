package de.upb.crc901.proseco.view.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import de.upb.crc901.proseco.view.app.model.StrategyCandidateFoundEvent;
import de.upb.crc901.proseco.view.app.model.StrategyCandidatesDatastore;

@RestController
public class StrategyController {

	/* logging. */
	private static final Logger L = LoggerFactory.getLogger(StrategyController.class);

	private final StrategyCandidatesDatastore datastore;

	public StrategyController() {
		this.datastore = new StrategyCandidatesDatastore();
	}

	@RequestMapping(method = RequestMethod.POST, value = "/api/strategy/candidateEval/{id}")
	public ResponseEntity<Object> postCandidateFoundEvent(@PathVariable("id") final String id, @RequestBody final StrategyCandidateFoundEvent e) {
		Map<String, Object> result = new HashMap<>();
		System.out.println("Received candidate from strategy " + id + ":\n" + e);
		result.put("status", this.datastore.put(id, e));
		result.put("data", this.datastore.getEvaluationsSortedByTimestamp(id));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/api/strategy/EvaluationsByTimestamp/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getEvaluationsSortedByTimestamp(@PathVariable("id") final String id) {
		Map<String, Object> result = new HashMap<>();
		result.put("status", "true");
		result.put("data", this.datastore.getEvaluationsSortedByTimestamp(id));
		return new ResponseEntity<>(result, HttpStatus.OK);
	}
}
