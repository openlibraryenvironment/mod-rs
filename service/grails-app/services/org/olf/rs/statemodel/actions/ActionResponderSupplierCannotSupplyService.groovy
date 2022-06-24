package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

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

        // Now set the new status and audit message
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_UNFILLED);
        actionResultDetails.auditMessage = 'Request manually flagged unable to supply';

        return(actionResultDetails);
    }
}
