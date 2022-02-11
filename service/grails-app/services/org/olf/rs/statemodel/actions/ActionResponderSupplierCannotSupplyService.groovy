package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierCannotSupplyService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.RESPONDER_UNFILLED
								];
	
	@Override
	String name() {
		return(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY);
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

		// Just send the message of unfilled
		reshareActionService.sendResponse(request, 'Unfilled', parameters);
		
		// Now set the new status and audit message
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_UNFILLED);
		actionResultDetails.auditMessage = 'Request manually flagged unable to supply';

		return(actionResultDetails);
	}
}
