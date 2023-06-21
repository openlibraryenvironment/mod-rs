package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * Action that deals with the ISO18626 RequestResponse message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626RequestResponseService extends ActionISO18626RequesterService {

    @Override
    String name() {
        return(ReasonForMessage.MESSAGE_REASON_REQUEST_RESPONSE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Call the base class first
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Only continue if successful
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            // Add an audit entry
            actionResultDetails.auditMessage = 'Request Response message received';
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
