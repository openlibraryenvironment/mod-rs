package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestShippedReturnService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER
								];
	
	@Override
	String name() {
		return(Actions.ACTION_REQUESTER_SHIPPED_RETURN);
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		// Decrement the active borrowing counter - we are returning the item
		statisticsService.decrementCounter('/activeBorrowing');
	
		reshareActionService.sendRequestingAgencyMessage(request, 'ShippedReturn', parameters);
	
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER);
		actionResultDetails.responseResult.status = true;

		return(actionResultDetails);
	}
}
