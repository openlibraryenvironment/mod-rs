package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestBorrowerCheckOverrideService extends AbstractAction {

	static String[] TO_STATES = [Status.PATRON_REQUEST_VALIDATED];
	
	@Override
	String name() {
		return(Actions.ACTION_REQUESTER_BORROWER_CHECK_OVERRIDE);
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		Map localParameters = parameters;
		localParameters.override = true;

		// Perform the borrower check
		Map borrower_check = reshareActionService.lookupPatron(request, localParameters);
		
		// borrower_check.patronValid should ALWAYS be true in this action
		actionResultDetails.responseResult.status = borrower_check?.callSuccess
		if (actionResultDetails.responseResult.status) {
			actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED);
		} else {
			// The Host LMS check call has failed, stay in current state
			request.needsAttention = true;
			actionResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + borrower_check?.problems;
		}

		return(actionResultDetails);
	}
}
