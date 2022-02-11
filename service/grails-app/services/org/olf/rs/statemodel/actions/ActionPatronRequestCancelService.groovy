package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;

public abstract class ActionPatronRequestCancelService extends AbstractAction {

	public boolean sendCancel(PatronRequest request, String action, Object parameters) {
		switch (action) {
			case 'requesterRejectedConditions':
				request.requestToContinue = true;
				break;
			case 'requesterCancel':
				request.requestToContinue = false;
				break;
			default:
				log.error("Action ${action} should not be able to send a cancel message");
				break;
		}
		
		reshareActionService.sendRequestingAgencyMessage(request, 'Cancel', parameters);
	}
}