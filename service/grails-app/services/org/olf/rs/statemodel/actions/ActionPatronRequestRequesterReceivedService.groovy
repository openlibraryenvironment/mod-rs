package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

public class ActionPatronRequestRequesterReceivedService extends AbstractAction {

	static String[] TO_STATES = [
								];
	
	@Override
	String name() {
		return("requesterReceived");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {
		
		// This will trigger an NCIP acceptItem as well
		if (!reshareActionService.sendRequesterReceived(request, parameters)) {
			actionResultDetails.result = ActionResult.INVALID_PARAMETERS;
			actionResultDetails.auditMessage = 'NCIP AcceptItem call failed';
			actionResultDetails.responseResult.code = -3; // NCIP action failed
			actionResultDetails.responseResult.message = actionResultDetails.auditMessage;
		};

		return(actionResultDetails);
	}
}
