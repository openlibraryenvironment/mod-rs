package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.refdata.RefdataValue
import groovy.json.JsonBuilder
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.ObjectUtils
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.*
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.referenceData.SettingsData
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

    NewDirectoryService newDirectoryService;
    ProtocolMessageBuildingService protocolMessageBuildingService;
    ProtocolMessageService protocolMessageService;
    SettingsService settingsService;
    SharedIndexService sharedIndexService;
    StatusService statusService;
    ReshareActionService reshareActionService;

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

            Symbol resolvedSupplyingAgency = DirectoryEntryService.resolveSymbol(header.supplyingAgencyId?.agencyIdType, header.supplyingAgencyId?.agencyIdValue);
            Symbol resolvedRequestingAgency = DirectoryEntryService.resolveSymbol(header.requestingAgencyId?.agencyIdType, header.requestingAgencyId?.agencyIdValue);

            log.debug('*** Create new request ***');
            log.debug("Creating request from eventData ${eventData}");
            def newParams = [:]
            if (eventData.bibliographicInfo instanceof Map) {
                newParams.putAll(eventData.bibliographicInfo.subMap(ReshareApplicationEventHandlerService.preserveFields))
                mapBibliographicRecordId(eventData, newParams)
                mapBibliographicItemId(eventData, newParams)
            }

            PatronRequest pr = findOrCreatePatronRequest(eventData, newParams, result)
            if (pr) {

                if (ObjectUtils.isNotEmpty(eventData.identifiers) && eventData.schemeValue) {
                    Map customIdentifiersMap = [schemeValue : eventData.schemeValue]
                    customIdentifiersMap.put('identifiers', eventData.identifiers)
                    pr.customIdentifiers = new JsonBuilder(customIdentifiersMap).toPrettyString()
                }

                // Add publisher information to Patron Request
                if (eventData.publicationInfo instanceof Map) {
                    Map publicationInfo = eventData.publicationInfo
                    if (publicationInfo != null) {
                        if (publicationInfo.publisher) {
                            pr.publisher = publicationInfo.publisher
                        }
                        if (publicationInfo.publicationType) {
                            pr.publicationType = pr.lookupPublicationType(publicationInfo.publicationType)
                        }
                        if (publicationInfo.publicationDate) {
                            pr.publicationDate = publicationInfo.publicationDate
                        }
                        if (publicationInfo.publicationDateOfComponent) {
                            pr.publicationDateOfComponent = publicationInfo.publicationDateOfComponent
                        }
                        if (publicationInfo.placeOfPublication) {
                            pr.placeOfPublication = publicationInfo.placeOfPublication
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
                        if (serviceInfo.copyrightCompliance) {
                            pr.copyrightType = findCopyrightType(serviceInfo.copyrightCompliance);
                        }
                        if (serviceInfo.serviceLevel) {
                            RefdataValue rdv = findRefdataValue(serviceInfo.serviceLevel, RefdataValueData.VOCABULARY_SERVICE_LEVELS);
                            pr.serviceLevel = rdv;
                        }
                    }
                }

                // UGH! Protocol delivery info is not remotely compatible with the UX prototypes - sort this later
                def address = null;
                if (eventData.requestedDeliveryInfo instanceof Map) {
                    address = eventData.requestedDeliveryInfo.address;
                } else if (eventData.requestedDeliveryInfo instanceof List) {
                    Map rdiMap = listToMap(eventData.requestedDeliveryInfo);
                    address = rdiMap.address
                }
                if (address instanceof Map) {
                    if (address.physicalAddress instanceof Map) {
                        log.debug("Incoming request contains delivery info: ${eventData.requestedDeliveryInfo?.address?.physicalAddress}");
                        // We join all the lines of physical address and stuff them into pickup location for now.
                        String stringifiedPickupLocation = address?.physicalAddress.collect { k, v -> v }.join(ADDRESS_SEPARATOR);

                        // If we've not been given any address information, don't translate that into a pickup location
                        if (stringifiedPickupLocation?.trim()?.length() > 0) {
                            pr.pickupLocation = stringifiedPickupLocation.trim();
                        }

                        // The above was for situations where it was largely used to stash a shipping ID.
                        // In case it's actually an address, let's also format it as a multi-line string.
                        pr.deliveryAddress = formatPhysicalAddress(address?.physicalAddress)
                    }

                    // Since ISO18626-2017 doesn't yet offer DeliveryMethod here we encode it as an ElectronicAddressType
                    if (address.electronicAddress instanceof Map) {
                        pr.deliveryMethod = pr.lookupDeliveryMethod(address?.electronicAddress?.electronicAddressType);
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

                if (eventData.billingInfo instanceof Map) {
                    Map billingInfo = eventData.billingInfo
                    if (billingInfo != null) {
                        if (billingInfo.maximumCosts instanceof Map) {
                            Map maximumCosts = billingInfo.maximumCosts;
                            if (maximumCosts.monetaryValue) {
                                pr.maximumCostsMonetaryValue = new BigDecimal(maximumCosts.monetaryValue);
                            }
                            if (maximumCosts.currencyCode) {
                                pr.maximumCostsCurrencyCode = findRefdataValue(maximumCosts.currencyCode, RefdataValueData.VOCABULARY_CURRENCY_CODES);
                            }
                        }
                    }
                }

                pr.supplyingInstitutionSymbol = "${header.supplyingAgencyId?.agencyIdType}:${header.supplyingAgencyId?.agencyIdValue}";
                if (!pr.requestingInstitutionSymbol || header.requestingAgencyId?.agencyIdValue) {
                    pr.requestingInstitutionSymbol = "${header.requestingAgencyId?.agencyIdType}:${header.requestingAgencyId?.agencyIdValue}";
                }

                pr.resolvedRequester = resolvedRequestingAgency;
                if (pr.resolvedSupplier == null || resolvedRequestingAgency != null) {
                    pr.resolvedSupplier = resolvedSupplyingAgency;
                }
                pr.peerRequestIdentifier = header.requestingAgencyRequestId;

                String requestRouterSetting = settingsService.getSettingValue(SettingsData.SETTING_ROUTING_ADAPTER);
                if (requestRouterSetting == "disabled") {
                    pr.returnAddress = newDirectoryService.shippingAddressForEntry(newDirectoryService.institutionEntryBySymbol(pr.supplyingInstitutionSymbol));
                }

                // For reshare - we assume that the requester is sending us a globally unique HRID and we would like to be
                // able to use that for our request.
                pr.hrid = protocolMessageService.extractIdFromProtocolId(header?.requestingAgencyRequestId);

                if (eventData.supplierInfo instanceof Map){
                    Map supplierInfo = eventData.supplierInfo
                    if (supplierInfo.callNumber) {
                        RequestVolume rv = pr.volumes.find { rv -> rv.callNumber == supplierInfo.callNumber }
                        if (!rv) {
                            rv = new RequestVolume(
                                    name: pr.hrid,
                                    itemId: "--",
                                    status: RequestVolume.lookupStatus(EventRespNewSlnpPatronRequestIndService.VOLUME_STATUS_REQUESTED_FROM_THE_ILS)
                            )
                            rv.callNumber = supplierInfo.callNumber
                            pr.addToVolumes(rv)
                        }
                    }
                }

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

                log.debug("New request from ${pr.requestingInstitutionSymbol} to ${pr.supplyingInstitutionSymbol}");

                // Set State, StateModel specific data, call NCIP lookup patron for SLNP Requester
                setStateModelData(pr, eventData)

                //special handling for preceded-by bibliographicItemIdentifierCode
                def biid = eventData?.bibliographicInfo?.bibliographicItemId;
                if (biid instanceof ArrayList) {
                    biid.each {
                        if (it.bibliographicItemIdentifierCode == 'preceded-by') {
                            log.debug("Attempting to find preceding request via HRID");
                            PatronRequest preceedingPr = getPatronRequestByHrid(it.bibliographicItemIdentifier, pr.isRequester ? true : false);
                            if (pr) {
                                log.debug("Found request associated with HRID ${it.bibliographicItemIdentifier}");
                                pr.precededBy = preceedingPr;
                                preceedingPr.succeededBy = pr;
                                preceedingPr.save();
                            }
                        }
                    }
                }

                log.debug("Saving new PatronRequest(SupplyingAgency) - Req:${pr.resolvedRequester} Res:${pr.resolvedSupplier} PeerId:${pr.peerRequestIdentifier}");
                pr.save(flush: true, failOnError: true)

                // In case of SLNP Requester and NCIP call failure set Error message, otherwise OK
                buildResponseMessage(pr, result)
            }

            result.messageType = Iso18626Constants.REQUEST
            result.supIdType = header.supplyingAgencyId?.agencyIdType // supplyingAgencyId can be null
            result.supId = header.supplyingAgencyId?.agencyIdValue // supplyingAgencyId can be null
            if (header.requestingAgencyId.agencyIdValue) {
                result.reqAgencyIdType = header.requestingAgencyId.agencyIdType
                result.reqAgencyId = header.requestingAgencyId.agencyIdValue
            } else {
                List<String> parts = pr.requestingInstitutionSymbol.split(':')
                result.reqAgencyIdType = parts[0]
                result.reqAgencyId = parts[1]
            }
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
            log.debug("Creating new patron request from params")
            pr = new PatronRequest(newParams)
        } else {
            result.status = EventISO18626IncomingAbstractService.STATUS_ERROR
            result.errorType = EventISO18626IncomingAbstractService.ERROR_TYPE_REQUEST_ID_ALREADY_EXISTS
            result.errorValue = retry ? "Request update failed because found ${prs.size()} records" :
                    "Failed to create new request because in DB there already is record with such id"
        }
        return pr
    }

    RefdataValue findCopyrightType(String label) {
        RefdataCategory cat = RefdataCategory.findByDesc(RefdataValueData.VOCABULARY_COPYRIGHT_TYPE);
        RefdataValue copyrightType = RefdataValue.findByOwnerAndValue(cat, label);
        return copyrightType;
    }

    RefdataValue findRefdataValue(String label, String vocabulary) {
        RefdataCategory cat = RefdataCategory.findByDesc(vocabulary);
        if (cat) {
            RefdataValue rdv = RefdataValue.findByOwnerAndValue(cat, label);
            return rdv;
        }
        return null;
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
            newMap.put("schemeValue", settingValue)
            newMap.put("identifiers", customIdentifiers)
        }

        return newMap
    }

    static String buildErrorAuditMessage(PatronRequest request, Map lookupPatron) {
        String errors = (lookupPatron?.problems == null) ? '' : (' (Errors: ' + lookupPatron.problems + ')')
        String status = lookupPatron?.status == null ? '' : (' (Patron state = ' + lookupPatron.status + ')')
        return "Failed to validate patron with id: \"${request.patronIdentifier}\".${status}${errors}".toString()
    }

    private void setStateModelData(PatronRequest pr, Map eventData) {
        pr.isRequester = "PatronRequest" == (eventData.serviceInfo?.requestSubType)
        StateModel stateModel = statusService.getStateModel(pr)
        pr.stateModel = stateModel

        if (isSlnpRequesterStateModel(pr)) {
            String errorAuditMessage = null
            try {
                Map lookupPatron = reshareActionService.lookupPatron(pr, null)
                if (lookupPatron.callSuccess) {
                    log.debug("Patron lookup success: ${lookupPatron}")
                    boolean patronValid = lookupPatron.patronValid
                    if (!patronValid) {
                        errorAuditMessage = buildErrorAuditMessage(pr, lookupPatron)
                    }
                } else {
                    errorAuditMessage = buildErrorAuditMessage(pr, lookupPatron)
                }
            } catch (Exception e) {
                errorAuditMessage = String.format("NCIP lookup patron call failure: %s", e.getMessage())
            }

            if (errorAuditMessage) {
                Status updatedStatus = Status.lookup(Status.SLNP_REQUESTER_PATRON_INVALID)
                pr.state = updatedStatus
                reshareApplicationEventHandlerService.auditEntry(pr, null, pr.state, errorAuditMessage, null)
            } else {
                pr.state = pr.stateModel.initialState
                reshareApplicationEventHandlerService.auditEntry(pr, null, pr.state, "NCIP call successful for patron identifier: ${pr.patronIdentifier}", null)
            }
        } else {
            pr.state = pr.stateModel.initialState
            reshareApplicationEventHandlerService.auditEntry(pr, null, pr.state, "New request (Lender role) created as a result of protocol interaction", null)
        }
    }

    static void buildResponseMessage(PatronRequest pr, Map result) {
        if (isSlnpRequesterStateModel(pr) && pr.state.code.equalsIgnoreCase(Status.SLNP_REQUESTER_PATRON_INVALID)) {
            result.status = EventISO18626IncomingAbstractService.STATUS_ERROR
            result.errorType = EventISO18626IncomingAbstractService.ERROR_TYPE_INVALID_PATRON_REQUEST
            result.errorValue = "NCIP lookup patron call failure for patron identifier: ${pr.patronIdentifier}"
        } else {
            result.status = EventISO18626IncomingAbstractService.STATUS_OK
        }
        result.newRequestId = pr.id
    }

    static String formatPhysicalAddress(Map pa) {
        if (!pa) return null

        def lines = []

        if (pa.line1) lines << pa.line1
        if (pa.line2) lines << pa.line2

        def cityLine = [pa.locality, pa.region, pa.postalCode]
            .findAll() // filter out non-truthy elements
            .join(', ')
        if (cityLine) lines << cityLine

        if (pa.country) lines << pa.country

        return lines.join('\n')
    }

    static boolean isSlnpRequesterStateModel(PatronRequest pr) {
        String shortcode = pr.stateModel.shortcode
        return shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_REQUESTER) ||
                shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER)
    }

    public PatronRequest getPatronRequestByHrid(String id, boolean isRequester) {
        PatronRequest result = PatronRequest.createCriteria().get {
            and {
                eq('hrid', id)
                eq('isRequester', isRequester)
            }
            lock false
        }
        return result;
    }

    Map listToMap(List list) {
        Map result = [ address : [:] ];
        list.each({ item -> {
                if (item.address instanceof Map) {
                    item.address.each({ k, v ->
                        result.address[k] = v;
                    });
                }
            }
        });
    }
}
