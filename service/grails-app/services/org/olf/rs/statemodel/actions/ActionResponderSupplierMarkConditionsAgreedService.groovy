package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierMarkConditionsAgreedService extends ActionResponderService {

	static String[] TO_STATES = [Status.RESPONDER_AWAIT_PICKING,
								 Status.RESPONDER_AWAIT_PROXY_BORROWER,
								 Status.RESPONDER_AWAIT_SHIP,
								 Status.RESPONDER_CHECKED_IN_TO_RESHARE,
								 Status.RESPONDER_IDLE,
								 Status.RESPONDER_NEW_AWAIT_PULL_SLIP
								];
	
	@Override
	String name() {
		return("supplierMarkConditionsAgreed");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	Boolean canLeadToSameState() {
	    // We do not return the same state, so we need to override and return false
		return(false);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {
		
		// Mark all conditions as accepted
		reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(request)

		actionResultDetails.auditMessage = 'Conditions manually marked as agreed';
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, request.previousStates[Status.RESPONDER_PENDING_CONDITIONAL_ANSWER]);

		// No longer need to have the state saved prior to the conditions being added
		request.previousStates[Status.RESPONDER_PENDING_CONDITIONAL_ANSWER] = null;
		
		return(actionResultDetails);
	}
}
