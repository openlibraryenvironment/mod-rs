package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderItemReturnedService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.RESPONDER_AWAITING_RETURN_SHIPPING
								];
	
	@Override
	String name() {
		return(Actions.ACTION_RESPONDER_ITEM_RETURNED);
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

		// Just change the status to await return shipping
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAITING_RETURN_SHIPPING);
		actionResultDetails.responseResult.status = true;
		
		return(actionResultDetails);
	}
}
