package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * This action performs the Manual Check In action for the requester
 * @author Chas
 *
 */
public class ActionPatronRequestRequesterManualCheckInService extends AbstractAction {

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REQUESTER_MANUAL_CHECKIN);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Just set the status
        actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CHECKED_IN);
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
