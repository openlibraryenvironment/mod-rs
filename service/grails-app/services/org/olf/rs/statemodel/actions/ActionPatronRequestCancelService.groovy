package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Abstract action class that deals with a cancel being requested
 * @author Chas
 *
 */
public abstract class ActionPatronRequestCancelService extends AbstractAction {

    public void sendCancel(PatronRequest request, String action, Object parameters, ActionResultDetails resultDetails) {
        switch (action) {
            case Actions.ACTION_REQUESTER_REQUESTER_REJECT_CONDITIONS:
                request.requestToContinue = true;
                break;

            case Actions.ACTION_REQUESTER_REQUESTER_CANCEL:
                request.requestToContinue = false;
                break;

            default:
                log.error("Action ${action} should not be able to send a cancel message");
                break;
        }

        reshareActionService.sendRequestingAgencyMessage(request, 'Cancel', parameters, resultDetails);
    }
}
