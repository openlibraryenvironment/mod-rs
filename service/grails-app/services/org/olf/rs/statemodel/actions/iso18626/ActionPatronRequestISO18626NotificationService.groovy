package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * Action that deals with the ISO18626 Notification message
 * @author Chas
 *
 */
public class ActionPatronRequestISO18626NotificationService extends ActionISO18626RequesterService {

    @Override
    String name() {
        return(ReasonForMessage.MESSAGE_REASON_NOTIFICATION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Call the base class first
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Only continue if successful
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            if (parameters.statusInfo?.status == "Unfilled") {
                // Clear supplyingInstitutionSymbol when Unfilled comes in as a Notification
                request.supplyingInstitutionSymbol = null;

                // Set qualifier to UnfilledContinue for notifications to distinguish from StatusChange unfilled
                actionResultDetails.qualifier = "UnfilledContinue";
            }

            // Add an audit entry
            actionResultDetails.auditMessage = "Notification message received from supplying agency: ${protocolMessageBuildingService.extractSequenceFromNote(parameters.messageInfo?.note).note}";
        }

        // Now return the results to the caller
        return(actionResultDetails);
    }
}
