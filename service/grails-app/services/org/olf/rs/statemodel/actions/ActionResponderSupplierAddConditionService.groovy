package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierAddConditionService extends ActionResponderService {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Add the condition and send it to the requester
		reshareActionService.addCondition(request, parameters);
		reshareActionService.sendSupplierConditionalWarning(request, parameters);
		
		// Do we need to hold the request
		if (parameters.isNull('holdingState') || parameters.holdingState == "no") {
			// The supplying agency wants to continue with the request
			actionResultDetails.auditMessage = 'Added loan condition to request, request continuing';
		} else {
			// The supplying agency wants to go into a holding state
			request.previousStates.put(Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, request.state.code)
			actionResultDetails.auditMessage = 'Condition added to request, placed in hold state';
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER);
		}
		return(actionResultDetails);
	}
}
