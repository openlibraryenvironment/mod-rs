package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Performed when the responder has said he cannot supply
 * @author Chas
 *
 */
public class ActionResponderSupplierCannotSupplyService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just send the message of unfilled
        reshareActionService.sendResponse(request, 'Unfilled', parameters, actionResultDetails);

        // Now set the  audit message
        actionResultDetails.auditMessage = 'Request manually flagged unable to supply';

        return(actionResultDetails);
    }
}
