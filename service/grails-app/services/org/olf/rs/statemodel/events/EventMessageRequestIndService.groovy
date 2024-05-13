package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.refdata.RefdataValue
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.ObjectUtils
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.*
import org.olf.rs.constants.CustomIdentifiersScheme
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.*

import javax.servlet.http.HttpServletRequest
import java.time.LocalDate
/**
 * Service that processes the Request-ind event
 * @author Chas
 */
@Slf4j
public class EventMessageRequestIndService extends AbstractEvent {
    static final String ADDRESS_SEPARATOR = ' '
    private static final Map<String, String> PATRON_REQUEST_PROPERTY_NAMES = new HashMap<>()

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
            def newParams = [:]
            if (eventData.bibliographicInfo instanceof Map) {
                eventData.bibliographicInfo.subMap(ReshareApplicationEventHandlerService.preserveFields)
                mapBibliographicRecordId(eventData, newParams)
                mapBibliographicItemId(eventData, newParams)
            }

            PatronRequest pr = findOrCreatePatronRequest(eventData, newParams, result)
            if (pr) {

                if (ObjectUtils.isNotEmpty(eventData.identifiers) && eventData.scheme) {
                    Map customIdentifiersMap = [scheme : eventData.scheme]
                    customIdentifiersMap.put('identifiers', eventData.identifiers)
                    pr.customIdentifiers = new JsonBuilder(customIdentifiersMap).toPrettyString()
                }

                // Add publisher information to Patron Request
                if (eventData.publicationInfo instanceof Map) {
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
                if (eventData.serviceInfo instanceof Map) {
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
                if (eventData.patronInfo instanceof Map) {
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
                pr.save(flush: true, failOnError: true)

                result.status = EventISO18626IncomingAbstractService.STATUS_OK
                result.newRequestId = pr.id
            }

            result.messageType = Iso18626Constants.REQUEST
            result.supIdType = header.supplyingAgencyId?.agencyIdType // supplyingAgencyId can be null
            result.supId = header.supplyingAgencyId?.agencyIdValue // supplyingAgencyId can be null
            result.reqAgencyIdType = header.requestingAgencyId.agencyIdType
            result.reqAgencyId = header.requestingAgencyId.agencyIdValue
            result.reqId = header.requestingAgencyRequestId
            result.timeRec = header.timestamp

        } else {
            log.error("A REQUEST indication must contain a request key with properties defining the sought item - eg request.title - GOT ${eventData}");
        }

        // I didn't go through changing everywhere result was mentioned to eventResultDetails.responseResult
        eventResultDetails.responseResult = result;

        log.debug('EventMessageRequestIndService::processEvent complete');
        return(eventResultDetails);
    }

    List<PatronRequest> lookupPatronRequests(String id, boolean withLock = false) {
        log.debug("LOCKING ReshareApplicationEventHandlerService::lookupPatronRequestWithRole(${id},${withLock})")
        List<PatronRequest> results = PatronRequest.createCriteria().list {
            and {
                or {
                    eq('id', id)
                    eq('hrid', id)
                    eq('peerRequestIdentifier', id)
                }
            }
            lock withLock
        }

        log.debug("LOCKING EventMessageRequestIndService::lookupPatronRequest located results ${results?.size()}")

        return results;
    }

    static void mapBibliographicItemId(Map eventData, Map newParams) {
        if (eventData.bibliographicInfo.bibliographicItemId) {
            def bibliographicItemId = eventData.bibliographicInfo.bibliographicItemId
            if (bibliographicItemId instanceof ArrayList) {
                for (def item in bibliographicItemId) {
                    newParams.put(getPatronRequestPropertyNames().get(item.bibliographicItemIdentifierCode?.toLowerCase()),
                            item.bibliographicItemIdentifier)
                }
            } else {
                newParams.put(getPatronRequestPropertyNames().get(bibliographicItemId.bibliographicItemIdentifierCode?.toLowerCase()),
                        bibliographicItemId.bibliographicItemIdentifier)
            }
            newParams.remove(null)
        }
    }

    static void mapBibliographicRecordId(Map eventData, Map newParams) {
        if (eventData.bibliographicInfo.bibliographicRecordId) {
            def bibliographicRecordId = eventData.bibliographicInfo.bibliographicRecordId
            if (bibliographicRecordId instanceof ArrayList) {
                for (def record in bibliographicRecordId) {
                    newParams.put(getPatronRequestPropertyNames().get(record.bibliographicRecordIdentifierCode?.toLowerCase()), record.bibliographicRecordIdentifier)}
            } else {
                newParams.put(getPatronRequestPropertyNames().get(bibliographicRecordId.bibliographicRecordIdentifierCode?.toLowerCase()), bibliographicRecordId.bibliographicRecordIdentifier)
            }
            newParams.remove(null)
        }
    }

    PatronRequest findOrCreatePatronRequest(Map eventData, Map newParams, Map result){
        PatronRequest pr = null
        String id = eventData.header.requestingAgencyRequestId ?  eventData.header.requestingAgencyRequestId : eventData.header.supplyingAgencyRequestId
        List<PatronRequest> prs = lookupPatronRequests(id, true)
        boolean retry = eventData?.serviceInfo?.requestType == EventISO18626IncomingAbstractService.SERVICE_REQUEST_TYPE_RETRY
        if (prs.size() == 1 && retry) {
            pr = prs.get(0)
            newParams.each { key, value ->
                if (pr.hasProperty(key) && ObjectUtils.isNotEmpty(value)) {
                    pr."$key" = value
                }
            }
        } else if (prs.isEmpty() && !retry) {
            pr = new PatronRequest(newParams)
        } else {
            result.status = EventISO18626IncomingAbstractService.STATUS_ERROR
            result.errorType = EventISO18626IncomingAbstractService.ERROR_TYPE_REQUEST_ID_ALREADY_EXISTS
            result.errorValue = retry ? "Request update failed because found ${prs.size()} records" :
                    "Failed to create new request because in DB there already is record with such id"
        }
        return pr
    }

    static Map<String, String> getPatronRequestPropertyNames(){
        if (PATRON_REQUEST_PROPERTY_NAMES.isEmpty()) {
            def propertyNames = PatronRequest.metaClass.properties.collect { it.name }
            // Convert the list to a map with lowercase keys
            Map<String, String> propertyMap = propertyNames.collectEntries { propertyName ->
                [(propertyName.toLowerCase()): propertyName]
            }
            PATRON_REQUEST_PROPERTY_NAMES.putAll(propertyMap)
        }
        return PATRON_REQUEST_PROPERTY_NAMES
    }

    static Map createNewCustomIdentifiers(HttpServletRequest request, Map mr) {
        Map newMap = new HashMap(mr)
        String settingValue = null

        RefdataCategory customIdentifiersScheme = RefdataCategory.findByDesc(RefdataValueData.VOCABULARY_CUSTOM_IDENTIFIERS_SCHEME)
        if (customIdentifiersScheme) {
            List<RefdataValue> values = RefdataValue.findAllByOwner(customIdentifiersScheme)
            if (values && values.size() == 1) {
                settingValue = values.get(0).value.toUpperCase()
            } else {
                log.debug("Multiple values found for customIdentifiers scheme, only one is acceptable.")
                return newMap
            }
        }

        if (!settingValue) {
            return newMap
        }

        def slurper = new groovy.xml.XmlSlurper().parseText(groovy.xml.XmlUtil.serialize(request.XML))

        def codesBySchemaValue = slurper.'**'.findAll { node ->
            node.attributes().find { it.key == '{http://illtransactions.org/2013/iso18626}scheme' && it.value == settingValue }
        }

        def customIdentifiers = []
        slurper.request.bibliographicInfo.bibliographicRecordId.each { bri ->
            if (bri.bibliographicRecordIdentifierCode in codesBySchemaValue) {
                customIdentifiers.add([key: bri.bibliographicRecordIdentifierCode.text(), value: bri.bibliographicRecordIdentifier.text()])
            }
        }

        if (customIdentifiers && customIdentifiers.size() > 0) {
            newMap.put("scheme", settingValue)
            newMap.put("identifiers", customIdentifiers)
        }

        return newMap
    }
}
