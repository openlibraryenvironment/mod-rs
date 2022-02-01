package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.StatisticsService;

public abstract class AbstractAction {

	private static final String POSSIBLE_FROM_STATES_QUERY='select distinct aa.actionCode from AvailableAction as aa where aa.fromState = :fromstate and aa.triggerType = :triggerType'
	
	// We automatically inject these 3 services as some if not all actions use them
	ReshareActionService reshareActionService;
	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
	StatisticsService statisticsService;
	
	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	abstract ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails);

	/**
	 * The name of the action	
	 * @return the action name
	 */
	abstract String name();

	/**
	 * Can this action lead to the state it came from ? We default to True, so override to return False if the action does not
	 * @return True if it can lead to the state it came from, otherwise False
	 */
	Boolean canLeadToSameState() {
		return(true);
	}

	/** 
	 * The states this action could lead to that are different from the state that it came from
	 * @return The states it could lead to
	 */
	abstract String[] toStates();
		
	/**
	 * All the possible states this action could lead to
	 * @return the possible states
	 */
	String[] possibleToStates(String model) {
		String[] states = canLeadToSameState() ? fromStates(model) : [];
		return(states + toStates());
	}

	/**
	 * The possible states that could lead to this action
	 * @return The possible states that could lead to this action	
	 */
	String[] fromStates(String model) {
		String[] possibleStates = AvailableAction.getFromStates(model, name());
		return(possibleStates);
	}
}
