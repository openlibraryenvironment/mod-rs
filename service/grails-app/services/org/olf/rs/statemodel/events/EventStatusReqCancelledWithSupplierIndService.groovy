package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Event that is triggered when a request is cancelled with a supplier
 * @author Chas
 *
 */
public class EventStatusReqCancelledWithSupplierIndService extends AbstractEvent {

    private static final String[] FROM_STATES = [
        Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER
    ];

    private static final String[] TO_STATES = [
        StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED
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
    boolean supportsModel(String model) {
        // This event
        return(model == StateModel.MODEL_REQUESTER);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // We must have found the request, and it as to be in a state of cancelled with supplier
        if (request.state?.code == Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER) {
            if (request.requestToContinue == true) {
                eventResultDetails.auditMessage = 'Request to continue, sending to next lender';
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNFILLED);
                log.debug(eventResultDetails.auditMessage);
            } else {
                log.debug('Cancelling request')
                eventResultDetails.auditMessage = 'Request cancelled';
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_CANCELLED);
            }
            request.save(flush:true, failOnError: true)
        } else {
            log.warn('Request not in the correct state of ' + Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER + " (${request?.state?.code}).");
        }
        return(eventResultDetails);
    }
}
