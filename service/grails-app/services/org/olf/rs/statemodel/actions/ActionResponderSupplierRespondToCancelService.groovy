package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierRespondToCancelService extends ActionResponderService {
  
	static String[] TO_STATES = [
								 Status.RESPONDER_AWAIT_PICKING,
								 Status.RESPONDER_AWAIT_PROXY_BORROWER,
								 Status.RESPONDER_AWAIT_SHIP,
								 Status.RESPONDER_CHECKED_IN_TO_RESHARE,
								 Status.RESPONDER_IDLE,
								 Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
								 Status.RESPONDER_PENDING_CONDITIONAL_ANSWER
								];
	
	@Override
	String name() {
		return(Actions.ACTION_RESPONDER_SUPPLIER_RESPOND_TO_CANCEL);
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Send the response to the requester
		reshareActionService.sendSupplierCancelResponse(request, parameters);

		// If the cancellation is denied, switch the cancel flag back to false, otherwise send request to complete
		if (parameters?.cancelResponse == "no") {
			// We set the new status, to the saved status
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, request.previousStates[request.state.code]);
			actionResultDetails.auditMessage = 'Cancellation denied';
		} else {
			actionResultDetails.auditMessage = 'Cancellation accepted';
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCELLED);
		}
		
		// Always clear out the saved state
		request.previousStates[request.state.code] = null;
		
		return(actionResultDetails);
	}
}
