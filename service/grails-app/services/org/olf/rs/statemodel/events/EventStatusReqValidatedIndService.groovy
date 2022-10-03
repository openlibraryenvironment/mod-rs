package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.RequestRouterService;
import org.olf.rs.routing.RankedSupplier;
import org.olf.rs.routing.RequestRouter;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.Status;

/**
 * Event triggered when the request requires validation
 * @author Chas
 *
 */
public class EventStatusReqValidatedIndService extends AbstractEvent {

    RequestRouterService requestRouterService;

    @Override
    String name() {
        return(Events.EVENT_STATUS_REQ_VALIDATED_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    // This takes a request with the state of VALIDATED and changes the state to REQ_SOURCING_ITEM,
    // and then on to REQ_SUPPLIER_IDENTIFIED if a rota could be established
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // We only deal with requester requests that are in the state validated
        if ((request.isRequester == true) && (request.state?.code == Status.PATRON_REQUEST_VALIDATED)) {
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_SOURCING;
            eventResultDetails.auditMessage = 'Sourcing potential items';

            if (request.rota?.size() != 0) {
                log.debug(' -> Request is currently ' + Status.PATRON_REQUEST_SOURCING_ITEM + ' - transition to ' + Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED);
                eventResultDetails.qualifier = null;
                eventResultDetails.auditMessage = 'Request supplied with Lending String';
            } else {
                Map operationData = [ : ];
                operationData.candidates = [];

                // We will shortly refactor this block to use requestRouterService to get the next block of requests
                RequestRouter selectedRouter = requestRouterService.getRequestRouter();

                if (selectedRouter == null) {
                    throw new RuntimeException('Unable to locate router');
                }

                List<RankedSupplier> possibleSuppliers = selectedRouter.findMoreSuppliers(request.getDescriptiveMetadata(), []);

                log.debug("Created ranked rota: ${possibleSuppliers}");

                if (possibleSuppliers.size() > 0) {
                    int ctr = 0;

                    // Pre-process the list of candidates
                    possibleSuppliers?.each { rankedSupplier  ->
                        if (rankedSupplier .supplier_symbol != null) {
                            operationData.candidates.add([symbol:rankedSupplier .supplier_symbol, message:'Added']);
                            if (rankedSupplier .ill_policy == 'Will lend') {
                                log.debug("Adding to rota: ${rankedSupplier }");

                                // Pull back any data we need from the shared index in order to sort the list of candidates
                                request.addToRota(new PatronRequestRota(
                                    patronRequest : request,
                                    rotaPosition : ctr++,
                                    directoryId : rankedSupplier .supplier_symbol,
                                    instanceIdentifier : rankedSupplier .instance_identifier,
                                    copyIdentifier : rankedSupplier .copy_identifier,
                                    loadBalancingScore : rankedSupplier .rank,
                                    loadBalancingReason : rankedSupplier .rankReason
                                )
                            );
                        } else {
                                log.warn('ILL Policy was not Will lend');
                                operationData.candidates.add([symbol:rankedSupplier .supplier_symbol, message:"Skipping - illPolicy is \"${rankedSupplier .ill_policy}\""]);
                            }
                        } else {
                            log.warn('requestRouterService returned an entry without a supplier symbol');
                        }
                    }

                    // Procesing
                    eventResultDetails.qualifier = null;
                    eventResultDetails.auditMessage = 'Ratio-Ranked lending string calculated by ' + selectedRouter.getRouterInfo()?.description;
                } else {
                    // ToDo: Ethan: if LastResort app setting is set, add lenders to the request.
                    log.error("Unable to identify any suppliers for patron request ID ${eventData.payload.id}")
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_END_OF_ROTA;
                    eventResultDetails.auditMessage =  'Unable to locate lenders';
                }
            }
        } else {
            log.warn("For request ${eventData.payload.id}, state != " + Status.PATRON_REQUEST_VALIDATED + " (${request?.state?.code}) or is not a requester request");
        }
        return(eventResultDetails);
    }
}
