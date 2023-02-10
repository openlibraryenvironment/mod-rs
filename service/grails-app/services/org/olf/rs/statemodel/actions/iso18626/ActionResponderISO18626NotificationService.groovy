package org.olf.rs.statemodel.actions.iso18626;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StatusStage;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Notification message
 * @author Chas
 *
 */
public class ActionResponderISO18626NotificationService extends ActionISO18626ResponderService {

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.ACTION_NOTIFICATION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Call the base class
        actionResultDetails = super.performAction(request, parameters, actionResultDetails);

        // Were we successful
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            /* If the message is preceded by #ReShareLoanConditionAgreeResponse#
             * then we'll need to check whether or not we need to change state.
             */
            Map messageData = parameters.activeSection;
            if (messageData.note != null) {
                // Check for the reshare special of loan conditions agreed
                if (messageData.note.startsWith('#ReShareLoanConditionAgreeResponse#')) {
                    // First check we're in the state where we need to change states, otherwise we just ignore this and treat as a regular message, albeit with warning
                    if (request.state.stage == StatusStage.ACTIVE_PENDING_CONDITIONAL_ANSWER) {
                        // We need to change the state to the saved state
                        actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CONDITIONS_AGREED;
                        actionResultDetails.auditMessage = 'Requester agreed to loan conditions, moving request forward';

                        // Make all conditions agreed
                        reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(request);
                    } else {
                        // Loan conditions were already marked as agreed
                        actionResultDetails.auditMessage = 'Requester agreed to loan conditions, no action required on supplier side';
                    }
                } else {
                    // Look to see if there is a pickup location in the note field
                    String updatedPickupLocation = extractFieldFromNote(messageData.note, "PickupLocation");
                    if (updatedPickupLocation != null) {
                        // There is so we can set it
                        request.pickupLocation = updatedPickupLocation;

                        // Add an appropriate audit message
                        actionResultDetails.auditMessage = "Pickup location updated to \"" + updatedPickupLocation + "\"";
                    } else {
                        actionResultDetails.auditMessage = "Notification message received from requesting agency: ${messageData.note}";
                    }
                }
            }
        }

        // Now return the result to the caller
        return(actionResultDetails);
    }

    /**
     * Extracts a field from the note
     * @param note The note that has been sent
     * @param field The field to be extracted
     * @return The extracted field value
     */
    private String extractFieldFromNote(String note, String field) {
        String result = null;

        // The field label we need to look for
        String fieldLabel = "#ReShareUpdatedField-" + field + ':';

        // Lets see if we can find this label
        int fieldStart = note.indexOf(fieldLabel);
        if (fieldStart > -1) {
            // We have found this label, so move the start to the end
            fieldStart += fieldLabel.length();

            // Let us find the end of the data
            int fieldEnd = note.indexOf("#", fieldStart);

            // Have we found a field end
            if (fieldEnd > -1) {
                // We have, do not forget to exclude the terminator
                result = note.substring(fieldStart, fieldEnd);
            }
        }

        // Return the result to the caller
        return(result);
    }
}
