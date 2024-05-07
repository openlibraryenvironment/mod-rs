package org.olf.rs.statemodel.events

import groovy.util.logging.Slf4j
import org.apache.commons.lang3.ObjectUtils
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.*
import org.olf.rs.statemodel.*

import java.time.LocalDate
/**
 * Service that processes the Request-ind event
 * @author Chas
 */
@Slf4j
public class EventMessageRequestIndService extends AbstractEvent {
    static final String ADDRESS_SEPARATOR = ' '

    ProtocolMessageBuildingService protocolMessageBuildingService;
    ProtocolMessageService protocolMessageService;
    SharedIndexService sharedIndexService;
    StatusService statusService;

    @Override
    String name() {
        return(Events.EVENT_MESSAGE_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        // We are dealing with the transaction directly
        return(EventFetchRequestMethod.HANDLED_BY_EVENT_HANDLER);
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // In our scenario the request will be null, as we do everything ourselves, so never reference that parameter
        // We use the responseResult field for returning data back to the caller

        /**
         * A new request has been received from an external PEER institution using some comms protocol.
         * We will need to create a request where isRequester==false
         * This should return everything that ISO18626Controller needs to build a confirmation message
         */

        Map result = [:];

        // Check that we understand both the requestingAgencyId (our peer)and the SupplyingAgencyId (us)
        if ((eventData.bibliographicInfo != null) && (eventData.header != null)) {
            Map header = eventData.header

            Symbol resolvedSupplyingAgency = reshareApplicationEventHandlerService.resolveSymbol(header.supplyingAgencyId?.agencyIdType, header.supplyingAgencyId?.agencyIdValue);
            Symbol resolvedRequestingAgency = reshareApplicationEventHandlerService.resolveSymbol(header.requestingAgencyId?.agencyIdType, header.requestingAgencyId?.agencyIdValue);

            log.debug('*** Create new request ***')
            def newParams = eventData.bibliographicInfo.subMap(ReshareApplicationEventHandlerService.preserveFields)
            for (final def record in eventData.bibliographicInfo.bibliographicRecordId) {
                newParams.put(record.bibliographicRecordIdentifierCode, record.bibliographicRecordIdentifier)
            }
            String id = eventData.header.requestingAgencyRequestId ?  eventData.header.requestingAgencyRequestId : eventData.header.supplyingAgencyRequestId
            PatronRequest pr = lookupPatronRequest(id, true)
            if (pr) {
                newParams.each { key, value ->
                    if (pr.hasProperty(key) && ObjectUtils.isNotEmpty(value)) {
                        pr."$key" = value
                    }
                }
            } else {
                pr = new PatronRequest(newParams)
            }

            // Add publisher information to Patron Request
            if (eventData.publicationInfo != null) {
                Map publicationInfo = eventData.publicationInfo
                if (publicationInfo != null) {
                    if (publicationInfo.publisher) {
                        pr.publisher = publicationInfo.publisher;
                    }
                    if (publicationInfo.publicationType) {
                        pr.publicationType = pr.lookupPublicationType(publicationInfo.publicationType);
                    }
                    if (publicationInfo.publicationDate) {
                        pr.publicationDate = publicationInfo.publicationDate;
                    }
                    if (publicationInfo.publicationDateOfComponent) {
                        pr.publicationDateOfComponent = publicationInfo.publicationDateOfComponent;
                    }
                    if (publicationInfo.placeOfPublication) {
                        pr.placeOfPublication = publicationInfo.placeOfPublication;
                    }
                }
            }

            // Add service information to Patron Request
            if (eventData.serviceInfo != null) {
                Map serviceInfo = eventData.serviceInfo

                if (serviceInfo != null) {
                    if (serviceInfo.serviceType) {
                        pr.serviceType = pr.lookupServiceType(serviceInfo.serviceType);
                    }
                    if (serviceInfo.needBeforeDate) {
                        // This will come in as a string, will need parsing
                        try {
                            pr.neededBy = LocalDate.parse(serviceInfo.needBeforeDate);
                        } catch (Exception e) {
                            log.debug("Failed to parse neededBy date (${serviceInfo.needBeforeDate}): ${e.message}");
                        }
                    }
                    if (serviceInfo.note) {
                        // We mave have a sequence number that needs to be extracted
                        Map sequenceResult = protocolMessageBuildingService.extractSequenceFromNote(serviceInfo.note);
                        pr.patronNote = sequenceResult.note;
                        pr.lastSequenceReceived = sequenceResult.sequence;
                    }
                }
            }

            // UGH! Protocol delivery info is not remotely compatible with the UX prototypes - sort this later
            if (eventData.requestedDeliveryInfo instanceof Map) {
                if (eventData.requestedDeliveryInfo?.address instanceof Map) {
                    if (eventData.requestedDeliveryInfo?.address.physicalAddress instanceof Map) {
                        log.debug("Incoming request contains delivery info: ${eventData.requestedDeliveryInfo?.address?.physicalAddress}");
                        // We join all the lines of physical address and stuff them into pickup location for now.
                        String stringifiedPickupLocation = eventData.requestedDeliveryInfo?.address?.physicalAddress.collect { k, v -> v }.join(ADDRESS_SEPARATOR);

                        // If we've not been given any address information, don't translate that into a pickup location
                        if (stringifiedPickupLocation?.trim()?.length() > 0) {
                            pr.pickupLocation = stringifiedPickupLocation.trim();
                        }
                    }

                    // Since ISO18626-2017 doesn't yet offer DeliveryMethod here we encode it as an ElectronicAddressType
                    if (eventData.requestedDeliveryInfo?.address.electronicAddress instanceof Map) {
                        pr.deliveryMethod = pr.lookupDeliveryMethod(eventData.requestedDeliveryInfo?.address?.electronicAddress?.electronicAddressType);
                    }
                }
            }

            // Add patron information to Patron Request
            if (eventData.patronInfo != null) {
                Map patronInfo = eventData.patronInfo
                if (patronInfo != null) {
                    if (patronInfo.patronId) {
                        pr.patronIdentifier = patronInfo.patronId;
                    }
                    if (patronInfo.surname) {
                        pr.patronSurname = patronInfo.surname;
                    }
                    if (patronInfo.givenName) {
                        pr.patronGivenName = patronInfo.givenName;
                    }
                    if (patronInfo.patronType) {
                        pr.patronType = patronInfo.patronType;
                    }
                    if (patronInfo.patronReference) {
                        pr.patronReference = patronInfo.patronReference;
                    }
                }
            }

            pr.supplyingInstitutionSymbol = "${header.supplyingAgencyId?.agencyIdType}:${header.supplyingAgencyId?.agencyIdValue}";
            pr.requestingInstitutionSymbol = "${header.requestingAgencyId?.agencyIdType}:${header.requestingAgencyId?.agencyIdValue}";

            pr.resolvedRequester = resolvedRequestingAgency;
            pr.resolvedSupplier = resolvedSupplyingAgency;
            pr.peerRequestIdentifier = header.requestingAgencyRequestId;

            // For reshare - we assume that the requester is sending us a globally unique HRID and we would like to be
            // able to use that for our request.
            pr.hrid = protocolMessageService.extractIdFromProtocolId(header?.requestingAgencyRequestId);

            if ((pr.bibliographicRecordId != null) && (pr.bibliographicRecordId.length() > 0)) {
                log.debug("Incoming request with pr.bibliographicRecordId - calling fetchSharedIndexRecords ${pr.bibliographicRecordId}");
                List<String> bibRecords = sharedIndexService.getSharedIndexActions().fetchSharedIndexRecords([systemInstanceIdentifier: pr.bibliographicRecordId]);
                if (bibRecords?.size() > 0) {
                    pr.bibRecord = bibRecords[0];
                    if (bibRecords?.size() > 1) {
                        reshareApplicationEventHandlerService.auditEntry(pr, null, pr.state, "WARNING: shared index ID ${pr.bibliographicRecordId} matched multiple records", null);
                    }
                }
            }

            log.debug("new request from ${pr.requestingInstitutionSymbol} to ${pr.supplyingInstitutionSymbol}");

            // Status change message is assign to service EventISO18626IncomingRequesterService and it is processing only request with isRequester=true
            pr.isRequester = "PatronRequest" == (eventData.serviceInfo?.requestSubType)
            pr.stateModel = statusService.getStateModel(pr)
            pr.state = pr.stateModel.initialState;
            reshareApplicationEventHandlerService.auditEntry(pr, null, pr.state, 'New request (Lender role) created as a result of protocol interaction', null);

            log.debug("Saving new PatronRequest(SupplyingAgency) - Req:${pr.resolvedRequester} Res:${pr.resolvedSupplier} PeerId:${pr.peerRequestIdentifier}");
            pr.save(flush:true, failOnError:true)

            result.messageType = Iso18626Constants.REQUEST
            result.supIdType = header.supplyingAgencyId?.agencyIdType;// supplyingAgencyId can be null
            result.supId = header.supplyingAgencyId?.agencyIdValue;// supplyingAgencyId can be null
            result.reqAgencyIdType = header.requestingAgencyId.agencyIdType;
            result.reqAgencyId = header.requestingAgencyId.agencyIdValue;
            result.reqId = header.requestingAgencyRequestId;
            result.timeRec = header.timestamp;

            result.status = EventISO18626IncomingAbstractService.STATUS_OK
            result.newRequestId = pr.id;
        } else {
            log.error("A REQUEST indication must contain a request key with properties defining the sought item - eg request.title - GOT ${eventData}");
        }

        // I didn't go through changing everywhere result was mentioned to eventResultDetails.responseResult
        eventResultDetails.responseResult = result;

        log.debug('EventMessageRequestIndService::processEvent complete');
        return(eventResultDetails);
    }

    PatronRequest lookupPatronRequest(String id, boolean withLock = false) {
        log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole(${id},${withLock})")
        PatronRequest result = PatronRequest.createCriteria().get {
            and {
                or {
                    eq('id', id)
                    eq('hrid', id)
                    eq('peerRequestIdentifier', id)
                }
            }
            lock withLock
        }

        log.debug("LOCKING EventMessageRequestIndService::lookupPatronRequest located ${result?.id}/${result?.hrid}");

        return result;
    }
}
