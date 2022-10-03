package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Responder is performing a manual Check Out
 * @author Chas
 *
 */
public class ActionResponderSupplierManualCheckoutService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_MANUAL_CHECKOUT);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Nowt to do
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
