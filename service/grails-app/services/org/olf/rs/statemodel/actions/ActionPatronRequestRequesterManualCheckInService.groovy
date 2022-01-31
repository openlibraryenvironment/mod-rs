package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestRequesterManualCheckInService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_CHECKED_IN
								];
	
	@Override
	String name() {
		return("requesterManualCheckIn");
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

		// Just set the status
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN);
		actionResultDetails.responseResult.status = true;

		return(actionResultDetails);
	}
}
