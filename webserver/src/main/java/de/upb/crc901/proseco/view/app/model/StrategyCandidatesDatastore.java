package de.upb.crc901.proseco.view.app.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StrategyCandidatesDatastore {

	private final Map<String, List<StrategyCandidateFoundEvent>> eventStore;

	public StrategyCandidatesDatastore() {
		this.eventStore = new HashMap<>();
	}

	public boolean put(final String strategyID, final StrategyCandidateFoundEvent e) {
		List<StrategyCandidateFoundEvent> eventList = this.eventStore.get(strategyID);
		if (eventList == null) {
			eventList = new LinkedList<>();
			this.eventStore.put(strategyID, eventList);
		}
		return eventList.add(e);
	}

	public double[] getEvaluationsSortedByTimestamp(final String strategyID) {
		if (!this.eventStore.containsKey(strategyID)) {
			return new double[0];
		}

		return new LinkedList<>(this.eventStore.get(strategyID)).stream().sorted(new Comparator<StrategyCandidateFoundEvent>() {
			@Override
			public int compare(final StrategyCandidateFoundEvent arg0, final StrategyCandidateFoundEvent arg1) {
				return Long.compare(arg0.getCandidateFoundTimestamp(), arg1.getCandidateFoundTimestamp());
			}
		}).mapToDouble(x -> x.getCandidateEvaluation()).toArray();

	}

	public int size() {
		return this.eventStore.values().stream().mapToInt(x -> x.size()).sum();
	}

}
