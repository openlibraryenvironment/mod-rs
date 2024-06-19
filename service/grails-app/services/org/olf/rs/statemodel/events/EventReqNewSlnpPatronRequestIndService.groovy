package org.olf.rs.statemodel.events


import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.NewRequestService
import org.olf.rs.PatronRequest
import org.olf.rs.patronRequest.PickupLocationService
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events
/**
 * This event service takes a new requester SLNP patron request and validates and generates HRID.
 */
public class EventReqNewSlnpPatronRequestIndService extends AbstractEvent {


    PickupLocationService pickupLocationService
    NewRequestService newRequestService

    @Override
    String name() {
        return(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION)
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID)
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id} isRequester=${request?.isRequester} StateModel=${eventData.stateModel}")
            return (eventResultDetails)
        }

        // Generate a human readable ID to use
        if (!request.hrid) {
            request.hrid = newRequestService.generateHrid()
            log.debug("set request.hrid to ${request.hrid}")
        }

        if (request.requestingInstitutionSymbol != null) {
            Symbol s = reshareApplicationEventHandlerService.resolveCombinedSymbol(request.requestingInstitutionSymbol)
            if (s != null) {
                request.resolvedRequester = s
            }
        }

        if (!request.resolvedPickupLocation) {
            pickupLocationService.checkByName(request)
        }

        request.needsAttention = false
        eventResultDetails.auditMessage = "Request validation done"
        eventResultDetails.saveData = true

        return(eventResultDetails)
    }
}
