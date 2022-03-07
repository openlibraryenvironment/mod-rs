package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Action that deals with filling the request locally
 * @author Chas
 *
 */
public class ActionPatronRequestFillLocallyService extends AbstractAction {

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_FILLED_LOCALLY
    ];

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_FILL_LOCALLY);
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
        // Just set the status
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_FILLED_LOCALLY);
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
