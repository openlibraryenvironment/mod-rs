package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestShippedReturnService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER
								];
	
	@Override
	String name() {
		return("shippedReturn");
	}

	@Override
	String[] toStates() {
		return(TO_STATES);
	}

	@Override
	ActionResultDetails performAction(PatronRequest request, def parameters, ActionResultDetails actionResultDetails) {

		reshareActionService.sendRequesterShippedReturn(request, parameters);
		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER);
		actionResultDetails.responseResult.status = true;

		return(actionResultDetails);
	}
}
