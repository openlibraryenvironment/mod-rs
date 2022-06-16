package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Event that is triggered after the request is cancelled
 * @author Chas
 *
 */
public class EventStatusReqCancelledIndService extends EventTriggerNoticesService {

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_CANCELLED_WITH_SUPPLIER_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // all we need to do is call triggerNotice
        triggerNotice(request, 'Request cancelled');

        return(eventResultDetails);
    }
}
