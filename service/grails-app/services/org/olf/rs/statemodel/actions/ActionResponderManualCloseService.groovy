package org.olf.rs.statemodel.actions;

import org.olf.rs.statemodel.Status;

public class ActionResponderManualCloseService extends ActionManualCloseService {

	static String[] TO_STATES = [
								 Status.RESPONDER_CANCELLED,
								 Status.RESPONDER_COMPLETE,
								 Status.RESPONDER_NOT_SUPPLIED,
								 Status.RESPONDER_UNFILLED
								];
	
	@Override
	String[] toStates() {
		return(TO_STATES);
	}
}
