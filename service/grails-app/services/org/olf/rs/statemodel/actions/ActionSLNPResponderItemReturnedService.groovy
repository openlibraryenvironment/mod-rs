package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

/**
 * Action that means the supplier has received the item back from the requester
 * @author Chas
 *
 */
public class ActionSLNPResponderItemReturnedService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_ITEM_RETURNED);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just mark it as successful
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
