package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;

public abstract class ActionResponderConditionService extends ActionResponderService {

	public boolean sendSupplierConditionalWarning(PatronRequest request, Object parameters) {
	    /* This method will send a specialised notification message either warning the requesting agency that their request is in statis until confirmation
	     * is received that the loan conditions are agreed to, or warning that the conditions are assumed to be agreed to by default.
	     */
	    
	    log.debug("supplierConditionalNotification(${request})");
	    boolean result = false;
	
	    Map warningParams = [:]
	
	    if (parameters.isNull("holdingState") || parameters.holdingState == 'no') {
			warningParams.note = "#ReShareSupplierConditionsAssumedAgreed#";
	    } else {
			warningParams.note = "#ReShareSupplierAwaitingConditionConfirmation#";
	    }
	    
	    // Only the supplier should ever be able to send one of these messages, otherwise something has gone wrong.
	    if (request.isRequester == false) {
			result = reshareActionService.sendSupplyingAgencyMessage(request, "Notification", null, warningParams);
	    } else {
			log.warn("The requesting agency should not be able to call sendSupplierConditionalWarning.");
	    }
	    return result;
	}
}
