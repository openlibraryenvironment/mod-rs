package org.olf.rs.statemodel.actions.iso18626

import org.olf.rs.PatronRequest
import org.olf.rs.iso18626.ReasonForMessage
import org.olf.rs.statemodel.ActionResultDetails
/**
 * Action that deals with the ISO18626 Notification message
 *
 */
public class ActionSLNPRequesterSlnpISO18626LoanedService extends ActionISO18626RequesterService {

    @Override
    String name() {
        return(ReasonForMessage.MESSAGE_REASON_CANCEL_RESPONSE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // TODO: Implement this protocol type action

        // Now just call the base class
        return(actionResultDetails);
    }
}
