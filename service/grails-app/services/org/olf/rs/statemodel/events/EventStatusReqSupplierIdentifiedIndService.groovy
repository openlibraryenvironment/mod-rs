package org.olf.rs.statemodel.events;

import org.olf.rs.statemodel.Events;
import com.k_int.web.toolkit.refdata.RefdataValue;

public class EventStatusReqSupplierIdentifiedIndService extends EventSendToNextLenderService {

  PatronNoticeService patronNoticeService;

	String name() {
		return(Events.EVENT_STATUS_REQ_SUPPLIER_IDENTIFIED_INDICATION);
	}

  @Override
  EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
    patronNoticeService.triggerNotices(request, RefdataValue.lookupOrCreate('noticeTriggers', 'New request'));
    return super.processEvent(request,eventData,eventResultDetails);
  }
}
