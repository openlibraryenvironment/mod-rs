package org.olf.rs.statemodel.events;

import org.olf.rs.statemodel.Events;

public class EventStatusReqUnfilledIndService extends EventSendToNextLenderService {

	String name() {
		return(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION);
	}
}
