package org.olf.rs.statemodel.events;

import org.olf.rs.statemodel.Events;

public class EventStatusReqSupplierIdentifiedIndService extends EventSendToNextLenderService {

	String name() {
		return(Events.EVENT_STATUS_REQ_SUPPLIER_IDENTIFIED_INDICATION);
	}
}
