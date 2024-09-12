package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * Action that deals with the ISO18626 StatusChange message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626StatusChangeService extends ActionISO18626RequesterService {

    @Override
    String name() {
        return(ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // We have a hack where we use this  message to verify that the last one sent was actually received or not
        if (!checkForLastSequence(request, parameters.messageInfo?.note, actionResultDetails)) {
            // A normal message
            // Call the base class first
            actionResultDetails = super.performAction(request, parameters, actionResultDetails);

            // Only continue if successful
            if (actionResultDetails.result == ActionResult.SUCCESS) {
                // Add an audit entry
                actionResultDetails.auditMessage = 'Status Change message received';
            }
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
