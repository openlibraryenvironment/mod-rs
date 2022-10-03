package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusStage;

/**
 * Event that is triggered when a request is cancelled with a supplier
 * @author Chas
 *
 */
public class EventStatusReqCancelledWithSupplierIndService extends AbstractEvent {

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
        // We must have found the request, and it has to be in a stage of completed
        if (request.state?.stage == StatusStage.COMPLETED) {
            if (request.requestToContinue == true) {
                eventResultDetails.auditMessage = 'Request to continue, sending to next lender';
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CONTINUE;
                log.debug(eventResultDetails.auditMessage);
            } else {
                log.debug('Cancelling request')
                eventResultDetails.auditMessage = 'Request cancelled';
            }
        } else {
            log.warn('Request not in the correct state of ' + Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER + " (${request?.state?.code}).");
        }
        return(eventResultDetails);
    }
}
