package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierConditionalSupplyService extends ActionResponderService {

	/**
	 * Method that all classes derive from this one that actually performs the action
	 * @param request The request the action is being performed against
	 * @param parameters Any parameters required for the action
	 * @param actionResultDetails The result of performing the action
	 * @return The actionResultDetails 
	 */
	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Check the pickup location and route
		if (validatePickupLocationAndRoute(request, parameters, actionResultDetails).result == ActionResult.SUCCESS) {
			reshareActionService.sendResponse(request, 'ExpectToSupply', parameters);
			reshareActionService.sendSupplierConditionalWarning(request, parameters);
  
			if (parameters.isNull('holdingState') || parameters.holdingState == "no") {
				// The supplying agency wants to continue with the request
				actionResultDetails.auditMessage = 'Request responded to conditionally, request continuing';
				actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP);
			} else {
				// The supplying agency wants to go into a holding state
				// In this case we want to "pretend" the previous state was actually the next one, for later when it looks up the previous state
				request.previousStates.put(Status.RESPONDER_PENDING_CONDITIONAL_ANSWER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP)
				actionResultDetails.auditMessage = 'Request responded to conditionally, placed in hold state';
				actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_PENDING_CONDITIONAL_ANSWER);
			}
		}

		return(actionResultDetails);
	}
}
