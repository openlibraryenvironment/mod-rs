package org.olf.rs.statemodel.events;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.RequestRouterService;
import org.olf.rs.routing.RankedSupplier;
import org.olf.rs.routing.RequestRouter;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

public class EventStatusReqValidatedIndService extends AbstractEvent {

	RequestRouterService requestRouterService;
	
	static String[] FROM_STATES = [
		Status.PATRON_REQUEST_VALIDATED
	];

	static String[] TO_STATES = [
		Status.PATRON_REQUEST_SOURCING_ITEM,
		Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED,
		Status.PATRON_REQUEST_END_OF_ROTA
	];

	String name() {
		return(Events.EVENT_STATUS_REQ_VALIDATED_INDICATION);
	}

	EventFetchRequestMethod fetchRequestMethod() {
		return(EventFetchRequestMethod.PAYLOAD_ID);
	}

	@Override
	Boolean canLeadToSameState() {
		return(false);
	}

	String[] toStates(String model) {
		return(TO_STATES);
	}
		
	String[] fromStates(String model) {
		return(FROM_STATES);
	}

	boolean supportsModel(String model) {
		// This event 
		return(model == StateModel.MODEL_REQUESTER);	
	}
	
  // This takes a request with the state of VALIDATED and changes the state to REQ_SOURCING_ITEM, 
  // and then on to REQ_SUPPLIER_IDENTIFIED if a rota could be established
	EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {

		// We only deal with requester requests that are in the state validated
		if ((request.isRequester == true) && (request.state?.code == Status.PATRON_REQUEST_VALIDATED)) {
			log.debug(" -> Request is currently " + Status.PATRON_REQUEST_VALIDATED + " - transition to " + Status.PATRON_REQUEST_SOURCING_ITEM);
			eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SOURCING_ITEM);
			eventResultDetails.auditMessage = 'Sourcing potential items';
			
			if (request.rota?.size() != 0) {
				log.debug(" -> Request is currently " + Status.PATRON_REQUEST_SOURCING_ITEM + " - transition to " + Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED);
				eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED);
				eventResultDetails.auditMessage = 'Request supplied with Lending String';
			} else {
				def operation_data = [ : ];
				operation_data.candidates=[];
			
				// We will shortly refactor this block to use requestRouterService to get the next block of requests
				RequestRouter selected_router = requestRouterService.getRequestRouter();
			
				if (selected_router == null) {
						throw new RuntimeException('Unable to locate router');
				}
			
				List<RankedSupplier> possible_suppliers = selected_router.findMoreSuppliers(request.getDescriptiveMetadata(), []);
			
				log.debug("Created ranked rota: ${possible_suppliers}");
			
				if (possible_suppliers.size() > 0) {
					int ctr = 0;
			
					// Pre-process the list of candidates
					possible_suppliers?.each { ranked_supplier ->
					if (ranked_supplier.supplier_symbol != null) {
						operation_data.candidates.add([symbol:ranked_supplier.supplier_symbol, message:"Added"]);
						if (ranked_supplier.ill_policy == 'Will lend') {
							log.debug("Adding to rota: ${ranked_supplier}");
			
							// Pull back any data we need from the shared index in order to sort the list of candidates
							request.addToRota(new PatronRequestRota(
								patronRequest : request,
								rotaPosition : ctr++,
							    directoryId : ranked_supplier.supplier_symbol,
								instanceIdentifier : ranked_supplier.instance_identifier,
								copyIdentifier : ranked_supplier.copy_identifier,
								state : reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_IDLE),
								loadBalancingScore : ranked_supplier.rank,
								loadBalancingReason : ranked_supplier.rankReason
							));
						} else {
							log.warn("ILL Policy was not Will lend");
							operation_data.candidates.add([symbol:ranked_supplier.supplier_symbol, message:"Skipping - illPolicy is \"${ranked_supplier.ill_policy}\""]);
							}
						} else {
							log.warn("requestRouterService returned an entry without a supplier symbol");
						}
					}

					// Procesing
					eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED);
					eventResultDetails.auditMessage = 'Ratio-Ranked lending string calculated by ' + selected_router.getRouterInfo()?.description;
				} else {
					// ToDo: Ethan: if LastResort app setting is set, add lenders to the request.
					log.error("Unable to identify any suppliers for patron request ID ${eventData.payload.id}")
					eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA);
					eventResultDetails.auditMessage =  'Unable to locate lenders';
				}
			}
		} else {
			log.warn("For request ${eventData.payload.id}, state != " + Status.PATRON_REQUEST_VALIDATED + " (${request?.state?.code}) or is not a requester request");
		}
		return(eventResultDetails);
	}
}
