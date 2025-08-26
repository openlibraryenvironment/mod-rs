package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.refdata.RefdataValue
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.DirectoryEntryService
import org.olf.rs.NewRequestService
import org.olf.rs.PatronNoticeService
import org.olf.rs.PatronRequest
import org.olf.rs.ReshareActionService
import org.olf.rs.ReshareApplicationEventHandlerService
import org.olf.rs.SettingsService
import org.olf.rs.patronRequest.PickupLocationService
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

public class EventNonreturnableRequesterNewPatronRequestIndService extends AbstractEvent {

    PatronNoticeService patronNoticeService;
    ReshareActionService reshareActionService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    NewRequestService newRequestService;
    PickupLocationService pickupLocationService;
    SettingsService settingsService;

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id} isRequester=${request?.isRequester}");
            return eventResultDetails;
        }

        String requestRouterSetting = settingsService.getSettingValue('routing_adapter');



        //Shim so that we can populate this value via the systemInstanceIdentifier
        if (requestRouterSetting == 'disabled') {
            if (!request.supplierUniqueRecordId && request.systemInstanceIdentifier != null) {
                request.supplierUniqueRecordId = request.systemInstanceIdentifier;
            }
        }



        if (!request.hrid) {
            request.hrid = newRequestService.generateHrid();
        }

        // If we were supplied a pickup location, attempt to resolve it
        if (!request.resolvedPickupLocation) {
            pickupLocationService.check(request)
        }

        String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);
        String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);

        if (request.requestingInstitutionSymbol != null || defaultRequestSymbolString != null) {
            Symbol requestingSymbol = DirectoryEntryService.resolveCombinedSymbol(request.requestingInstitutionSymbol);
            if (requestingSymbol != null) {
                request.resolvedRequester = requestingSymbol;
            }

            Map lookupPatron = reshareActionService.lookupPatron(request, null);
            if (lookupPatron.callSuccess) {
                boolean patronValid = lookupPatron.patronValid;
                if (requestingSymbol == null && defaultRequestSymbolString == null) {
                    request.needsAttention = true;
                    log.warn("Unknown requesting institution symbol : ${request.requestingInstitutionSymbol}");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO_INSTITUTION_SYMBOL;
                    eventResultDetails.auditMessage = 'Unknown Requesting Institution Symbol: ' + request.requestingInstitutionSymbol;
                } else if (!patronValid) {
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_INVALID_PATRON;
                    String errors = (lookupPatron?.problems == null) ? '' : (' (Errors: ' + lookupPatron.problems + ')');
                    String status = lookupPatron?.status == null ? '' : (' (Patron state = ' + lookupPatron.status + ')');
                    eventResultDetails.auditMessage = "Failed to validate patron with id: \"${request.patronIdentifier}\".${status}${errors}".toString();
                    request.needsAttention = true;
                } else if (newRequestService.isOverLimit(request)) {
                    log.debug("Request is over limit");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_OVER_LIMIT;
                    patronNoticeService.triggerNotices(request,
                            RefdataValue.lookupOrCreate('noticeTriggers', RefdataValueData.NOTICE_TRIGGER_OVER_LIMIT));
                } else if (!newRequestService.hasClusterID(request)) {
                    request.needsAttention = true;
                    log.warn("No cluster id set for request ${request.id}");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_BLANK_FORM_REVIEW;
                    eventResultDetails.auditMessage = 'Blank Request Form, needs review';
                } else {
                    log.debug("Got request ${request}");
                }
            } else {
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

        return eventResultDetails;
    }

    @Override
    String name() {
        return Events.EVENT_NONRETURNABLE_REQUESTER_NEW_PATRON_REQUEST_INDICATION;
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return EventFetchRequestMethod.PAYLOAD_ID;
    }
}
