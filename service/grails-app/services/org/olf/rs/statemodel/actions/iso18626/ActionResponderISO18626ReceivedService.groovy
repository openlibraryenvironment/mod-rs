package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Received message
 * @author Chas
 *
 */
public class ActionResponderISO18626ReceivedService extends ActionISO18626ResponderService {

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.ACTION_RECEIVED);
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
        // Call the base class
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Were we successful
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            // Just set the audit message
            actionResultDetails.auditMessage = 'Shipment received by requester';
        }

        // Now return the result to the caller
        return(actionResultDetails);
    }
}
