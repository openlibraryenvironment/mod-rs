package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class ActionPatronRequestLocalSupplierCannotSupplyService extends AbstractAction {

	static String[] TO_STATES = [
								 Status.PATRON_REQUEST_UNFILLED
								];
	
	@Override
	String name() {
		return(Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY);
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

		PatronRequestRota prr = request.rota.find( { it.rotaPosition == request.rotaPosition } );
		if (prr) {
			def rota_state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_NOT_SUPPLIED);
			prr.state = rota_state;
			prr.save(flush:true, failOnError: true);
		}

		actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED);
		actionResultDetails.auditMessage = "Request locally flagged as unable to supply";

		return(actionResultDetails);
	}
}
