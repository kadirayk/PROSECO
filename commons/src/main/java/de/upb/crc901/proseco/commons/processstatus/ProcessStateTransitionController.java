package de.upb.crc901.proseco.commons.processstatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessStateTransitionController {

	// currentState, possible next states
	private static Map<EProcessState, List<EProcessState>> transitionMap = new HashMap<>();

	static {
		List<EProcessState> initStates = new ArrayList<>();
		initStates.add(EProcessState.CREATED);
		initStates.add(EProcessState.INIT);
		transitionMap.put(EProcessState.INIT, initStates);

		List<EProcessState> createdStates = new ArrayList<>();
		createdStates.add(EProcessState.DOMAIN_DEFINITION);
		transitionMap.put(EProcessState.CREATED, createdStates);

		List<EProcessState> domainStates = new ArrayList<>();
		domainStates.add(EProcessState.INTERVIEW);
		transitionMap.put(EProcessState.DOMAIN_DEFINITION, domainStates);

		List<EProcessState> interviewStates = new ArrayList<>();
		interviewStates.add(EProcessState.INTERVIEW);
		interviewStates.add(EProcessState.COMPOSITION);
		transitionMap.put(EProcessState.INTERVIEW, interviewStates);

		List<EProcessState> compositionStates = new ArrayList<>();
		compositionStates.add(EProcessState.PROTOTYPE_EXTRACTED);
		transitionMap.put(EProcessState.COMPOSITION, compositionStates);

		List<EProcessState> prototypeStates = new ArrayList<>();
		prototypeStates.add(EProcessState.STRATEGY_CHOSEN);
		transitionMap.put(EProcessState.PROTOTYPE_EXTRACTED, prototypeStates);

		List<EProcessState> searchStates = new ArrayList<>();
		searchStates.add(EProcessState.GROUNDING);
		transitionMap.put(EProcessState.STRATEGY_CHOSEN, searchStates);

		List<EProcessState> groundingStates = new ArrayList<>();
		groundingStates.add(EProcessState.DEPLOYMENT);
		transitionMap.put(EProcessState.GROUNDING, groundingStates);

		List<EProcessState> deploymentStates = new ArrayList<>();
		deploymentStates.add(EProcessState.DONE);
		transitionMap.put(EProcessState.DEPLOYMENT, deploymentStates);
		
		List<EProcessState> doneStates = new ArrayList<>();
		transitionMap.put(EProcessState.DONE, doneStates);
		
	}

	public static EProcessState moveToNextState(EProcessState currentState, EProcessState nextState)
			throws InvalidStateTransitionException {
		if (transitionMap.get(currentState).contains(nextState)) {
			return nextState;
		} else {
			throw new InvalidStateTransitionException();
		}
	}

}
