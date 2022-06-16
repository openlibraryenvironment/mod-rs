package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Action that means the supplier has received the item back from the requester
 * @author Chas
 *
 */
public class ActionResponderItemReturnedService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_ITEM_RETURNED);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just change the status to await return shipping
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAITING_RETURN_SHIPPING);
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
