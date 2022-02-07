package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

public class EventNoImplementationService extends AbstractEvent {

	static String[] FROM_STATES = [
	];

	static String[] TO_STATES = [
	];

	String name() {
		return(Events.EVENT_NO_IMPLEMENTATION);
	}

	EventFetchRequestMethod fetchRequestMethod() {
		// We assume anything that is not implemented will have the request id in the payload
		return(EventFetchRequestMethod.PAYLOAD_ID);
	}

	String[] toStates(String model) {
		return(TO_STATES);
	}
		
	String[] fromStates(String model) {
		return(FROM_STATES);
	}

	boolean SupportsModel(String model) {
		// As this is the fallback for all those not implemented, we support all models 
		return(true);	
	}
	
	EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

		// There is nothing to do
		log.error("Event " + eventData.event.toString() + " has not been implemented");
		
		// No need to save the request or add an audit entry
		eventResultDetails.saveData = false;
		return(eventResultDetails);
	}
}
