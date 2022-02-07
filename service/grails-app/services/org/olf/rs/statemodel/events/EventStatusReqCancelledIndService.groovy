package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

public class EventStatusReqCancelledIndService extends EventTriggerNoticesService {

	static String[] FROM_STATES = [
	];

	static String[] TO_STATES = [
	];

	String name() {
		return(Events.EVENT_STATUS_REQ_CANCELLED_WITH_SUPPLIER_INDICATION);
	}

	EventFetchRequestMethod fetchRequestMethod() {
		return(EventFetchRequestMethod.PAYLOAD_ID);
	}

	String[] toStates(String model) {
		return(TO_STATES);
	}
		
	String[] fromStates(String model) {
		return(FROM_STATES);
	}

	EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

		// all we need to do is call triggerNotice
		triggerNotice(request, "Request cancelled");
		
		return(eventResultDetails);
	}
}
