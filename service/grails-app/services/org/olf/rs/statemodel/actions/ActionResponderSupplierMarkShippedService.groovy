package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionResponderSupplierMarkShippedService extends ActionResponderService {

	static String[] TO_STATES = [
								 Status.RESPONDER_ITEM_SHIPPED
								];
	
	@Override
	String name() {
		return("supplierMarkShipped");
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

		// Send the message that it is on its way
		reshareActionService.sendResponse(request, 'Loaned', parameters);
		actionResultDetails.auditMessage = 'Shipped';
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_ITEM_SHIPPED);

		return(actionResultDetails);
	}
}