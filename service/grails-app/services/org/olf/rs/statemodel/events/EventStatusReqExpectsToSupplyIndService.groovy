package org.olf.rs.statemodel.events

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events


class EventStatusReqExpectsToSupplyIndService extends AbstractEvent {

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_EXPECTS_TO_SUPPLY)
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID)
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        log.debug("Expect to supply check for request ${request.hrid}")
        return(eventResultDetails)
    }
}
