package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestRequesterRejectConditionsService extends ActionPatronRequestCancelService {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_CANCEL_PENDING
								];
	
	@Override
	String name() {
		return("requesterRejectConditions");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

        request.previousStates[Status.PATRON_REQUEST_CANCEL_PENDING] = request.state.code;
        sendCancel(request, "requesterRejectConditions", parameters);
		actionResultDetails.auditMessage = 'Rejected loan conditions';
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCEL_PENDING);

		return(actionResultDetails);
	}
}
