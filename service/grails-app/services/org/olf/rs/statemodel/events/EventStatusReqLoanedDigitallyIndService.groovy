package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.refdata.RefdataValue
import org.olf.rs.PatronRequest
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

/**
 * Event service triggered when a digital loan is initiated for a requested item
 */
public class EventStatusReqLoanedDigitallyIndService extends EventTriggerNoticesService {

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_LOANED_DIGITALLY_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // all we need to do is call triggerNotice
        triggerNotice(request, RefdataValueData.NOTICE_TRIGGER_LOANED_DIGITALLY);

        return(eventResultDetails);
    }
}
