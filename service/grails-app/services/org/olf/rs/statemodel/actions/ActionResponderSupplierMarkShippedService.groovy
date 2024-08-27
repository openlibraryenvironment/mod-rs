package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Responder has sent the item on its way to the requester
 * @author Chas
 *
 */
public class ActionResponderSupplierMarkShippedService extends ActionResponderService {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_MARK_SHIPPED);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Send the message that it is on its way
        reshareActionService.sendResponse(request, 'Loaned', parameters, actionResultDetails);
        actionResultDetails.auditMessage = 'Shipped';

        return(actionResultDetails);
    }
}