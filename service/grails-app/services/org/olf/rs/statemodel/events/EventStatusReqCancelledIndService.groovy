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

    private static final String[] FROM_STATES = [
    ];

    private static final String[] TO_STATES = [
    ];

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_CANCELLED_WITH_SUPPLIER_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    String[] toStates(String model) {
        return(TO_STATES);
    }

    @Override
    String[] fromStates(String model) {
        return(FROM_STATES);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // all we need to do is call triggerNotice
        triggerNotice(request, 'Request cancelled');

        return(eventResultDetails);
    }
}
