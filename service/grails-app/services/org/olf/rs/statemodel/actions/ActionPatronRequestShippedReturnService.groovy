package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

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
        // Decrement the active borrowing counter - we are returning the item
        statisticsService.decrementCounter('/activeBorrowing');

        reshareActionService.sendRequestingAgencyMessage(request, 'ShippedReturn', parameters);

        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SHIPPED_TO_SUPPLIER);
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
