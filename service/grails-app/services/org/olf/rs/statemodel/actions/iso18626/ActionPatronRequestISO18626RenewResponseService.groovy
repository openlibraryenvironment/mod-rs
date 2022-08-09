package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 RenewResponse message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626RenewResponseService extends ActionISO18626RequesterService {

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.MESSAGE_REASON_RENEW_RESPONSE);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Call the base class first
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Only continue if successful
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            // Add an audit entry
            actionResultDetails.auditMessage = 'Renew Response message received';
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
