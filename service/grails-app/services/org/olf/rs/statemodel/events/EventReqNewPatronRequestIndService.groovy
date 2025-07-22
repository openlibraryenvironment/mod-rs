package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.refdata.RefdataValue
import groovy.json.JsonSlurper
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.*
import org.olf.rs.patronRequest.PickupLocationService
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.*
/**
 * This event service takes a new requester patron request and validates it and tries to determine the rota
 * @author Chas
 */
public class EventReqNewPatronRequestIndService extends AbstractEvent {

    PatronNoticeService patronNoticeService;
    PickupLocationService pickupLocationService;
    ReshareActionService reshareActionService;
    SharedIndexService sharedIndexService;
    NewRequestService newRequestService;
    SettingsService settingsService;

    @Override
    String name() {
        return(Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    // Notify us of a new requester patron request in the database
    //
    // Requests are created with a STATE of IDLE, this handler validates the request and sets the state to VALIDATED, or ERROR
    // Called when a new patron request indication happens - usually in response to a new request being created but
    // also to re-validate
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id} isRequester=${request?.isRequester}");
            return(eventResultDetails);
        }

        String requestRouterSetting = settingsService.getSettingValue('routing_adapter');

        //Shim so that we can populate this value via the systemInstanceIdentifier
        if (requestRouterSetting == 'disabled') {
            if (!request.supplierUniqueRecordId && request.systemInstanceIdentifier != null) {
                request.supplierUniqueRecordId = request.systemInstanceIdentifier;
            }
        }

        // Generate a human readable ID to use
        if (!request.hrid) {
            request.hrid = newRequestService.generateHrid();
            log.debug("set request.hrid to ${request.hrid}");
        }

        if (request.serviceType == null) {
            request.serviceType = ProtocolReferenceDataValue.lookupServiceType(ProtocolReferenceDataValue.SERVICE_TYPE_LOAN);
        }

        // If we were supplied a pickup location, attempt to resolve it
        if (!request.resolvedPickupLocation) {
            pickupLocationService.check(request)
        }


        String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);
        String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);

        if (request.requestingInstitutionSymbol != null || defaultRequestSymbolString != null) {
            // We need to validate the requesting location - and check that we can act as requester for that symbol
            Symbol s = DirectoryEntryService.resolveCombinedSymbol(request.requestingInstitutionSymbol);
            if (s != null) {
                // We do this separately so that an invalid patron does not stop information being appended to the request
                request.resolvedRequester = s;
            }

            Map lookupPatron = reshareActionService.lookupPatron(request, null);
            if (lookupPatron.callSuccess) {
                boolean patronValid = lookupPatron.patronValid;

                if (s == null && defaultRequestSymbolString == null) {
                    // An unknown requesting institution symbol is a bigger deal than an invalid patron
                    request.needsAttention = true;
                    log.warn("Unknown requesting institution symbol : ${request.requestingInstitutionSymbol}");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO_INSTITUTION_SYMBOL;
                    eventResultDetails.auditMessage = 'Unknown Requesting Institution Symbol: ' + request.requestingInstitutionSymbol;
                } else if (!patronValid) {
                    // If we're here then the requesting institution symbol was fine but the patron is invalid
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_INVALID_PATRON;
                    String errors = (lookupPatron?.problems == null) ? '' : (' (Errors: ' + lookupPatron.problems + ')');
                    String status = lookupPatron?.status == null ? '' : (' (Patron state = ' + lookupPatron.status + ')');
                    eventResultDetails.auditMessage = "Failed to validate patron with id: \"${request.patronIdentifier}\".${status}${errors}".toString();
                    request.needsAttention = true;
                } else if (newRequestService.isOverLimit(request)) {
                    log.debug("Request is over limit");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_OVER_LIMIT;
                    patronNoticeService.triggerNotices(request,  RefdataValue.lookupOrCreate('noticeTriggers', RefdataValueData.NOTICE_TRIGGER_OVER_LIMIT));

                } else if (newRequestService.isPossibleDuplicate(request)) {
                    request.needsAttention = true;
                    log.warn("Request ${request.hrid} appears to be a duplicate (patronReference ${request.patronReference})");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DUPLICATE_REVIEW;
                } else if (!newRequestService.hasClusterID(request)) {
                    request.needsAttention = true;
                    log.warn("No cluster id set for request ${request.id}");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_BLANK_FORM_REVIEW;
                    eventResultDetails.auditMessage = 'Blank Request Form, needs review';
                } else {
                    // The request has passed validation
                    log.debug("Got request ${request}");
                }

            } else {
                // unexpected error in Host LMS call
                request.needsAttention = true;
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED;
                eventResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + lookupPatron?.problems;
            }
        } else {
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO_INSTITUTION_SYMBOL;
            request.needsAttention = true;
            eventResultDetails.auditMessage = 'No Requesting Institution Symbol';
        }

        if (requestRouterSetting == 'disabled') {
            request.requestingInstitutionSymbol = defaultRequestSymbolString;
            request.supplyingInstitutionSymbol = defaultPeerSymbolString;

            // TODO: consider making use of tiers an explicit setting rather than implicit based on routing
            if (request.maximumCostsMonetaryValue != null && request.maximumCostsCurrencyCode != null) {
                request.cost = request.maximumCostsMonetaryValue;
                request.costCurrency = request.maximumCostsCurrencyCode;
            }
        }

        // TODO: reconcile these two identifiers as both are in use
        if ((request.bibliographicRecordId == null) && (request.systemInstanceIdentifier != null)) {
            request.bibliographicRecordId = request.systemInstanceIdentifier
        }

        if ((request.bibliographicRecordId != null) && (request.bibliographicRecordId.length() > 0)) {
            log.debug('calling fetchSharedIndexRecords');
            List<String> bibRecords = sharedIndexService.getSharedIndexActions().fetchSharedIndexRecords([systemInstanceIdentifier: request.bibliographicRecordId]);
            if (bibRecords?.size() == 1) {
                request.bibRecord = bibRecords[0];
                // If our OCLC field isn't set, let's try to set it from our bibrecord
                if (!request.oclcNumber) {
                    try {
                        JsonSlurper slurper = new JsonSlurper();
                        Object bibJson = slurper.parseText(bibRecords[0]);
                        for (identifier in bibJson.identifiers) {
                            String oclcId = newRequestService.getOCLCId(identifier.value);
                            if (oclcId) {
                                log.debug("Setting request oclcNumber to ${oclcId}");
                                request.oclcNumber = oclcId;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Unable to parse bib json: ${e}");
                    }
                }
            }
        } else {
            log.debug("No request.bibliographicRecordId : ${request.bibliographicRecordId}");
        }

        return(eventResultDetails);
    }
}
