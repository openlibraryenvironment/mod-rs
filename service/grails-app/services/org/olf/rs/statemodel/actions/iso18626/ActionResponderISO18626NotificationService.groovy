package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Notification message
 * @author Chas
 *
 */
public class ActionResponderISO18626NotificationService extends ActionISO18626ResponderService {

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.ACTION_NOTIFICATION);
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

        // Call the base class
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            /* If the message is preceded by #ReShareLoanConditionAgreeResponse#
             * then we'll need to check whether or not we need to change state.
             */
            Map messageData = parameters.activeSection;
            if ((messageData.note != null) &&
                (messageData.note.startsWith('#ReShareLoanConditionAgreeResponse#'))) {
                // First check we're in the state where we need to change states, otherwise we just ignore this and treat as a regular message, albeit with warning
                if (request.state.code == Status.RESPONDER_PENDING_CONDITIONAL_ANSWER) {
                    // We need to change the state to the saved state
                    actionResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, request.previousStates[request.state.code]);
                    actionResultDetails.auditMessage = 'Requester agreed to loan conditions, moving request forward';
                    request.previousStates[request.state.code] = null;

                    // Maek all conditions agreed
                    reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(request);
                } else {
                    // Loan conditions were already marked as agreed
                    actionResultDetails.auditMessage = 'Requester agreed to loan conditions, no action required on supplier side';
                }
            } else {
                actionResultDetails.auditMessage = "Notification message received from requesting agency: ${messageData.note}";
            }
        }

        // Now return the result to the caller
        return(actionResultDetails);
    }
}
