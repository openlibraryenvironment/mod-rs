package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 RequestResponse message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626RequestResponseService extends ActionISO18626RequesterService {

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.MESSAGE_REASON_REQUEST_RESPONSE);
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
        // Call the base class first
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Only continue if successful
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            // Add an audit entry
            actionResultDetails.auditMessage = 'Request Response message recieved';
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
