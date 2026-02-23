package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * This action is performed when the requester ships the item back to the supplier
 * @author Chas
 *
 */
public class ActionPatronRequestShippedReturnService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_SHIPPED_RETURN);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {

        reshareActionService.sendRequestingAgencyMessage(request, 'ShippedReturn', parameters, actionResultDetails);

        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
