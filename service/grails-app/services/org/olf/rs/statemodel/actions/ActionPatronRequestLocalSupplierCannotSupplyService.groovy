package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Action that deals with a cannot supply locally
 * @author Chas
 *
 */
public class ActionPatronRequestLocalSupplierCannotSupplyService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_LOCAL_SUPPLIER_CANNOT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {

        actionResultDetails.auditMessage = 'Request locally flagged as unable to supply';

        return(actionResultDetails);
    }
}
