package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Shipped Return message
 * @author Chas
 *
 */
public class ActionResponderISO18626ShippedReturnService extends ActionISO18626ResponderService {

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.ACTION_SHIPPED_RETURN);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Call the base class
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Call the base class
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            // Set the audit message
            actionResultDetails.auditMessage = 'Item(s) Returned by requester';

            // Set the items waiting to be checked back in
            request.volumes?.each { vol ->
                vol.status = vol.lookupStatus('awaiting_lms_check_in');
            }
        }

        // Now return the result to the caller
        return(actionResultDetails);
    }
}
