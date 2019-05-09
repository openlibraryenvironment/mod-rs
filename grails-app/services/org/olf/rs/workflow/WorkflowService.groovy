package org.olf.rs.workflow

import org.olf.rs.PatronRequest;

class WorkflowService {

	public List<Action> GetValidActions(PatronRequest patronRequest) {
		// By default no actions are available
		List<Action> validActions = [ ];

		// No actions are available if we are in the process of performing an action or waiting for a protocol response
		if (!patronRequest.awaitingProtocolResponse && (patronRequest.pendingAction == null)) {
			// We are able to perform an action on the request so look to see what we can do
			List<StateTransition> validTransitions = StateTransition.findAllByFromStatusAndQualifier(patronRequest.State, StateTransition.GetQualifer(patronRequest.isRequester));
			if (validTransitions != null) {
				// We have found some transitions
				validTransitions.forEach() { StateTransition transition ->
					// Only interested in the actions that are selectable by the user
					if (transition.action.selectable) {
						validActions.add(transition.action);
					}
				}
			}
		}
		return(validActions);
	} 
}
