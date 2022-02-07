package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.settings.AppSetting;

public class EventStatusResCheckedInToReshareIndService extends AbstractEvent {

	ReshareActionService reshareActionService;
	
	static String[] FROM_STATES = [
	];

	static String[] TO_STATES = [
		Status.RESPONDER_AWAIT_SHIP
	];

	String name() {
		return(Events.EVENT_STATUS_RES_CANCEL_REQUEST_RECEIVED_INDICATION);
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

		/**
		 * It's not clear if the system will ever need to differentiate between the status of checked in and
		 * await shipping, so for now we leave the 2 states in place and just automatically transition  between them
		 * this method exists largely as a place to put functions and workflows that diverge from that model
		 */
		eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_AWAIT_SHIP);
		eventResultDetails.auditMessage = 'Request awaits shipping';
  
		return(eventResultDetails);
	}
}
