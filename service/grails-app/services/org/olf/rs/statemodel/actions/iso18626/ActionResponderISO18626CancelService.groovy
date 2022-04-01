package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Received message
 * @author Chas
 *
 */
public class ActionResponderISO18626CancelService extends ActionISO18626ResponderService {

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.ACTION_CANCEL);
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
            // Just set the audit message and state
            actionResultDetails.auditMessage = 'Requester requested cancellation of the request';
            actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_CANCEL_REQUEST_RECEIVED);;

            // Save the current state, so we can set it back
            request.previousStates[Status.RESPONDER_CANCEL_REQUEST_RECEIVED] = request.state.code;
        }

        // Now return the result to the caller
        return(actionResultDetails);
    }
}
