package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.iso18626.NoteSpecials;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * Abstract action that handles conditions being added to the request
 * @author Chas
 *
 */
public abstract class ActionResponderConditionService extends ActionResponderService {

    public boolean sendSupplierConditionalWarning(PatronRequest request, Object parameters, ActionResultDetails resultDetails) {
        /* This method will send a specialised notification message either warning the requesting agency that their request is in statis until confirmation
         * is received that the loan conditions are agreed to, or warning that the conditions are assumed to be agreed to by default.
         */

        log.debug("supplierConditionalNotification(${request})");
        boolean result = false;

        Map warningParams = [:]

        if (parameters.isNull('holdingState') || parameters.holdingState == 'no') {
            warningParams.note = NoteSpecials.CONDITIONS_ASSUMED_AGREED;
        } else {
            warningParams.note = NoteSpecials.AWAITING_CONDITION_CONFIRMED;
        }

        // Only the supplier should ever be able to send one of these messages, otherwise something has gone wrong.
        if (request.isRequester == false) {
            result = reshareActionService.sendSupplyingAgencyMessage(request, ReasonForMessage.MESSAGE_REASON_NOTIFICATION, null, warningParams, resultDetails);
        } else {
            log.warn('The requesting agency should not be able to call sendSupplierConditionalWarning.');
        }
        return result;
    }
}
