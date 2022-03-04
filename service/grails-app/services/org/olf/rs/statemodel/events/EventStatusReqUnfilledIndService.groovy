package org.olf.rs.statemodel.events;

import org.olf.rs.statemodel.Events;

/**
 * Event triggered by the supplier saying the request cannot be filled
 * @author Chas
 *
 */
public class EventStatusReqUnfilledIndService extends EventSendToNextLenderService {

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_UNFILLED_INDICATION);
    }
}
