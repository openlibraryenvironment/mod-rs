package org.olf.rs.statemodel.events


import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

/**
 * This event service takes a new SLNP responder patron request and responds automatically.
 */
public class EventRespNewSlnpPatronRequestIndService extends AbstractEvent {

    @Override
    String name() {
        return(Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id}} isRequester=${request?.isRequester} StateModel=${request.stateModel.shortcode}");
        }

        // TODO: respondYes action - depending on the response from client we will see if it should be automatic or stay manual as currently implemented.
        return(eventResultDetails);
    }
}
