package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Responder is performing a manual Check Out
 * @author Chas
 *
 */
public class ActionResponderSupplierManualCheckoutService extends AbstractAction {

    private static final String[] TO_STATES = [
        Status.RESPONDER_AWAIT_SHIP
    ];

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT);
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
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just change the status to await ship
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP);
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
