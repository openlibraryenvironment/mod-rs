package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.custprops.CustomProperty
import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.*
import org.olf.rs.constants.Directory
import org.olf.rs.iso18626.TypeStatus
import org.olf.rs.lms.ItemLocation
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.*
/**
 * This event service takes a new responder patron request and attempts to locate the item if enabled
 * @author Chas
 */
public class EventRespNewPatronRequestIndService extends EventTriggerNoticesService {
    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip"; //refdata seems to set values to lowercase
    private static final String SETTING_INSTITUTIONAL_ID = 'default_institutional_patron_id';

    DirectoryEntryService directoryEntryService;
    HostLMSService hostLMSService;
    ReshareActionService reshareActionService;
    SettingsService settingsService;

    @Override
    String name() {
        return(Events.EVENT_RESPONDER_NEW_PATRON_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    // Notify us of a new responder patron request in the database
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request != null) {
            String requestRouterSetting = settingsService.getSettingValue('routing_adapter');
            if (requestRouterSetting == 'disabled') {
                // TODO: consider making use of tiers an explicit setting rather than implicit based on routing
                if (request.maximumCostsMonetaryValue != null && request.maximumCostsCurrencyCode != null) {
                    request.cost = request.maximumCostsMonetaryValue;
                    request.costCurrency = request.maximumCostsCurrencyCode;
                }
            }

            triggerNotice(request, RefdataValueData.NOTICE_TRIGGER_NEW_SUPPLY_REQUEST);
            def notExpedited = ['standard', 'normal'];
            if (request?.serviceLevel?.value != null && !(request.serviceLevel.value.toLowerCase() in notExpedited)) {
                triggerNotice(request, RefdataValueData.NOTICE_TRIGGER_NEW_SUPPLY_REQUEST_EXPEDITED);
            }

            try {
                log.debug('Launch auto responder for request');
                String autoRespondSetting = AppSetting.findByKey('auto_responder_status')?.value
                if (autoRespondSetting?.toLowerCase().startsWith('on')) {
                    autoRespond(request, autoRespondSetting.toLowerCase(), eventResultDetails);
                } else {
                    eventResultDetails.auditMessage = "Auto responder is ${autoRespondSetting} - manual checking needed";
                    request.needsAttention = true;
                }
            } catch (Exception e) {
                log.error("Problem in auto respond: ${e.getMessage()}", e);
            }
        } else {
            log.warn("Unable to locate request for ID ${eventData.payload.id}} isRequester=${request?.isRequester}");
        }

        return(eventResultDetails);
    }

    private void autoRespond(PatronRequest request, String autoRespondVariant, EventResultDetails eventResultDetails) {
        log.debug('autoRespond....');

        // Use the hostLMSService to determine the best location to send a pull-slip to
        ItemLocation location = hostLMSService.determineBestLocation(request, ProtocolType.Z3950_RESPONDER);
        log.debug("result of determineBestLocation = ${location}");

        // Were we able to locate a copy?
        boolean unfilled = false;
        if (location != null) {
            // set localCallNumber to whatever we managed to look up
            if (reshareApplicationEventHandlerService.routeRequestToLocation(request, location)) {
                eventResultDetails.auditMessage = 'autoRespond will-supply, determine location=' + location;
                if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) { //is request item enabled for this responder?

                    log.debug("Resolved requester ${request.resolvedRequester?.owner?.name}")
                    //Get the institutionalPatronID from the directory entry, or fall back on the default in settings
                    CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(
                        request.resolvedRequesterDirectoryEntry, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID);
                    String institutionalPatronIdValue = institutionalPatronId?.value;
                    if (!institutionalPatronIdValue) {
                        // If nothing on the Directory Entry then fallback to the default in settings
                        AppSetting defaultInstitutionalPatronId = AppSetting.findByKey(SETTING_INSTITUTIONAL_ID);
                        institutionalPatronIdValue = defaultInstitutionalPatronId?.value;
                    }
                    String folioLocationFilter = directoryEntryService.extractCustomPropertyFromDirectoryEntry(
                            request.resolvedSupplierDirectoryEntry, Directory.KEY_FOLIO_LOCATION_FILTER)?.value
                    //send the RequestItem request
                    log.debug("Attempt hold with RequestItem");
                    Map requestItemResult = hostLMSService.requestItem(request,
                            request.resolvedSupplier?.owner?.lmsLocationCode, folioLocationFilter,
                            request.supplierUniqueRecordId, institutionalPatronIdValue);
                    log.debug("Got RequestItem result: ${requestItemResult}");
                    if (requestItemResult.result == true) {
                        log.debug("Send WillSupply response to ${request.requestingInstitutionSymbol}");
                        reshareActionService.sendResponse(request,  TypeStatus.WILL_SUPPLY.value(), [:], eventResultDetails);
                        log.debug("Set externalHoldRequestId of PatronRequest to ${requestItemResult.requestId}");
                        request.externalHoldRequestId = requestItemResult.requestId;
                        eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM;
                    } else {
                        log.debug("Request Item Hold Failed: ${requestItemResult?.problems}")
                        unfilled = true;
                        eventResultDetails.auditMessage = "Failed to place hold for item with bibliographicid ${request.supplierUniqueRecordId}";
                    }
                } else {
                    log.debug("Send WillSupply response to ${request.requestingInstitutionSymbol}");
                    reshareActionService.sendResponse(request,  TypeStatus.WILL_SUPPLY.value(), [:], eventResultDetails)
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED;
                }
            } else {
                unfilled = true;
                eventResultDetails.auditMessage = 'AutoResponder Failed to route to location ' + location;
            }
        } else {
            // No - is the auto responder set up to sent not-supplied?
            if (autoRespondVariant == 'on:_will_supply_and_cannot_supply') {
                unfilled = true;
                eventResultDetails.auditMessage = 'AutoResponder cannot locate a copy.';
            }
        }

        // If it was unfilled then send a response
        if (unfilled == true) {
            log.debug("Send unfilled(No copy) response to ${request.requestingInstitutionSymbol}");
            reshareActionService.sendResponse(request,  'Unfilled', ['reason':'No copy'], eventResultDetails);
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_UNFILLED;
        }
    }
}
