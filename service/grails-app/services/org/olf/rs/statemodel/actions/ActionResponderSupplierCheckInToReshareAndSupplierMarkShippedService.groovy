package org.olf.rs.statemodel.actions;

import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.Status;

/**
 * Action that occurs when the responder checks the item into reshare from the LMS
 * @author Ethan Freestone
 */
public class ActionResponderSupplierCheckInToReshareAndSupplierMarkShippedService extends ActionResponderService {

    ActionResponderSupplierCheckInToReshareService actionResponderSupplierCheckInToReshareService;
    ActionResponderSupplierMarkShippedService actionResponderSupplierMarkShippedService;

    private static final String REASON_SPOOFED = 'spoofed';

    private static final String[] TO_STATES = [
        Status.RESPONDER_ITEM_SHIPPED,
    ];

    HostLMSService hostLMSService;
    DirectoryEntryService directoryEntryService;

    @Override
    String name() {
        return(Actions.ACTION_RESPONDER_SUPPLIER_CHECK_INTO_RESHARE_AND_MARK_SHIPPED);
    }

    @Override
    String[] toStates() {
        return(TO_STATES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Create ourselves an ActionResultDetails that we will pass to each of the actions we want to call
        ActionResultDetails resultDetails = new ActionResultDetails();

        // Default the result as being a success
        resultDetails.result = ActionResult.SUCCESS;

        String returnAuditMessage;

        if (actionResponderSupplierCheckInToReshareService.performAction(request, parameters, resultDetails).result == ActionResult.SUCCESS) {
            // Copy in the new status in case the second action fails
            actionResultDetails.newStatus = resultDetails.newStatus;

            // Store auditMessage from check in call in case of success
            if (resultDetails.auditMessage) {
                returnAuditMessage = "Combined action. Check in success: ${resultDetails.auditMessage}"

                // Unset auditMessage on resultDetails so we don't get the same audit message twice
                resultDetails.auditMessage = null;
            }

            // Now we can mark it as being shipped
            if (actionResponderSupplierMarkShippedService.performAction(request, parameters, resultDetails).result == ActionResult.SUCCESS) {
                // Its a success, so copy in the new status and response result
                actionResultDetails.newStatus = resultDetails.newStatus;
                actionResultDetails.responseResult = resultDetails.responseResult;

                // Store auditMessage from check in call in case of success
                if (resultDetails.auditMessage && returnAuditMessage) {
                    returnAuditMessage += " Mark item shipped success: ${resultDetails.auditMessage}.";
                } else if (resultDetails.auditMessage) {
                    returnAuditMessage = "Combined action. Mark item shipped success: ${resultDetails.auditMessage}."
                }
            } else {
                actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CHECKED_IN
            }
        }

        // At least one of our two calls failed
        if (resultDetails.result != ActionResult.SUCCESS) {
            // Failed so copy back the appropriate details so it can be diagnosed
            actionResultDetails.responseResult = resultDetails.responseResult;
            actionResultDetails.responseResult = resultDetails.responseResult;
            actionResultDetails.result = resultDetails.result;

            // Audit trail is slightly different. We might have success information from the first call
            if (resultDetails.auditMessage) {
                if (returnAuditMessage) {
                    // The first call must have succeeded, so the second call has failed
                    returnAuditMessage += " Mark item shipped failed: ${resultDetails.auditMessage}"
                } else {
                    // We don't know what has failed, only that something has
                    returnAuditMessage = "Combined action. Failure: ${resultDetails.auditMessage}"
                }
            }
        }

        // Even if both succeeded we may want audit message
        if (returnAuditMessage) {
            actionResultDetails.auditMessage = returnAuditMessage;
        }

        return(actionResultDetails);
    }
}
