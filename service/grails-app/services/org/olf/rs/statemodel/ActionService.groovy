package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;

/**
 * Checks the incoming action to ensure it is valid and dispatches it to the appropriate service
 */
public class ActionService {

	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;

	/**
	 * Checks whether an action being performed is valid
	 */
	boolean isValid(boolean isRequester, Status status, String action) {

		// We default to not being valid
		boolean isValid = false;

		// Can only continue if we have been supplied the values
		if (action && status) {
			// The action message is available to all states except the terminal ones and the actions messageSeen and messageAllSeen are available for all states
			if (((action == "message") && !status.terminal) ||
				(action == "messageSeen") ||
				(action == "messagesAllSeen")) {
				isValid = true;
			} else {
				// Get hold of the state model id
				StateModel stateModel = StateModel.getStateModel(isRequester);
				if (stateModel) {
					// It is a valid state model
					// Now is this a valid action for this state
					isValid = (AvailableAction.countByModelAndFromStateAndActionCode(stateModel, status, action) == 1);
				}
			}
		}
		return(isValid);
	}

  	ActionResultDetails performAction(String action, PatronRequest request, Object parameters) {
		ActionResultDetails resultDetails = new ActionResultDetails();
		Status currentState = request.state;

		// Default a few fields
		resultDetails.newStatus = currentState;
		resultDetails.result = ActionResult.SUCCESS;
		resultDetails.auditMessage = "Executing action: " + action;
		resultDetails.auditData = parameters;

		// Get hold of the action
		AbstractAction actionBean = AvailableAction.getServiceAction(action, request.isRequester);
		if (actionBean == null) {
			resultDetails.result = ActionResult.ERROR;
			resultDetails.auditMessage = "Failed to find class for action: " + action;
		} else {
			// Just tell the class to do its stuff
			actionBean.performAction(request, parameters, resultDetails);
		}

		// Set the status of the request
		request.state = resultDetails.newStatus;

		// Adding an audit entry so we can see what states we are going to for the event
		// Do not commit this uncommented, here to aid seeing what transition changes we allow
//		reshareApplicationEventHandlerService.auditEntry(request, currentState, request.state, "Action: " + action + ", State change: " + currentState.code + " -> "  + request.state.code, null);

		// Create the audit entry
		reshareApplicationEventHandlerService.auditEntry(
			request,
			currentState,
			request.state,
			resultDetails.auditMessage,
			resultDetails.auditData);

		// Finally Save the request
		request.save(flush:true, failOnError:true);

		// Return the result to the caller
		return(resultDetails);
	}
}
