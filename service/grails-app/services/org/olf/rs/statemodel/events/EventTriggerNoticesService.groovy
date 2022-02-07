package org.olf.rs.statemodel.events;

import org.olf.rs.PatronNoticeService;
import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.StateModel;

import com.k_int.web.toolkit.refdata.RefdataValue;

public abstract class EventTriggerNoticesService extends AbstractEvent {

	PatronNoticeService patronNoticeService;
	
	EventFetchRequestMethod fetchRequestMethod() {
		return(EventFetchRequestMethod.PAYLOAD_ID);
	}

	boolean SupportsModel(String model) {
		// This event 
		return(model == StateModel.MODEL_REQUESTER);	
	}
	
	public void triggerNotice(PatronRequest request, String trigger) {
		patronNoticeService.triggerNotices(request, RefdataValue.lookupOrCreate('noticeTriggers', trigger));
	}
}
