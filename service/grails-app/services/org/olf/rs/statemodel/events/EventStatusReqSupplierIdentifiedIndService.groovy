package org.olf.rs.statemodel.events;

import org.olf.rs.statemodel.Events;
import com.k_int.web.toolkit.refdata.RefdataValue;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronNoticeService;

/**
 * Event raised when a supplier has been identified
 * @author Chas
 *
 */
public class EventStatusReqSupplierIdentifiedIndService extends EventSendToNextLenderService {

    PatronNoticeService patronNoticeService;

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_SUPPLIER_IDENTIFIED_INDICATION);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        patronNoticeService.triggerNotices(request, RefdataValue.lookupOrCreate('noticeTriggers', 'New request'));
        return super.processEvent(request, eventData, eventResultDetails);
    }
}
