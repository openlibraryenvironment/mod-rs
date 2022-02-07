package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;

public class EventStatusResOverdueIndService extends AbstractEvent {

	ReshareActionService reshareActionService;
	
	static String[] FROM_STATES = [
	];

	static String[] TO_STATES = [
	];

	String name() {
		return(Events.EVENT_STATUS_RES_OVERDUE_INDICATION);
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

	boolean supportsModel(String model) {
		// This event 
		return(model == StateModel.MODEL_RESPONDER);	
	}
	
	EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

		// We only deal with responder
		if (request.isRequester) {
			log.debug("pr ${request.id} is requester, not sending protocol message");
		} else {
			log.debug("Sending protocol message with overdue status change from PatronRequest ${request.id}");
			Map params = [ note : "Request is Overdue"]
			reshareActionService.sendStatusChange(request, 'Overdue', "Request is Overdue");
		}
	
		return(eventResultDetails);
	}
}
