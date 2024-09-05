package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

/**
 * Event triggered by the supplier saying the request cannot be filled
 * @author Chas
 *
 */
public class EventStatusReqUnfilledIndService extends EventSendToNextLenderService {

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        super.processEvent(request, eventData, eventResultDetails);
    }

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION);
    }
}
