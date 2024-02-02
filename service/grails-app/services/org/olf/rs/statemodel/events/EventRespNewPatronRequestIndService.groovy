package org.olf.rs.statemodel.events;

import org.olf.rs.constants.Directory;
import org.olf.rs.DirectoryEntryService;
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolType;
import org.olf.rs.ReshareActionService;
import org.olf.rs.SettingsService;
import org.olf.rs.SharedIndexService;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;

import com.k_int.web.toolkit.settings.AppSetting;
import com.k_int.web.toolkit.custprops.CustomProperty;

/**
 * This event service takes a new responder patron request and attempts to locate the item if enabled
 * @author Chas
 */
public class EventRespNewPatronRequestIndService extends AbstractEvent {
    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip"; //refdata seems to set values to lowercase
    private static final String SETTING_INSTITUTIONAL_ID = 'default_institutional_patron_id';

    DirectoryEntryService directoryEntryService;
    HostLMSService hostLMSService;
    ReshareActionService reshareActionService;
    SettingsService settingsService;
    SharedIndexService sharedIndexService;

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

                    //Get the institutionalPatronID from the directory entry, or fall back on the default in settings
                    CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(
                        request.resolvedRequester?.owner, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID);
                    String institutionalPatronIdValue = institutionalPatronId?.value;
                    if (!institutionalPatronIdValue) {
                        // If nothing on the Directory Entry then fallback to the default in settings
                        AppSetting defaultInstitutionalPatronId = AppSetting.findByKey(SETTING_INSTITUTIONAL_ID);
                        institutionalPatronIdValue = defaultInstitutionalPatronId?.value;
                    }
                    //send the RequestItem request
                    log.debug("Attempt hold with RequestItem");
                    Map requestItemResult = hostLMSService.requestItem(request, request.hrid,
                            request.supplierUniqueRecordId, institutionalPatronIdValue);
                    if (requestItemResult.result == true) {
                        log.debug("Send ExpectToSupply response to ${request.requestingInstitutionSymbol}");
                        reshareActionService.sendResponse(request,  'ExpectToSupply', [:], eventResultDetails);
                        eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM;
                    } else {
                        unfilled = true;
                        eventResultDetails.auditMessage = 'Failed to place hold for item with bibliographicid '
                                + request.supplierUniqueRecordId;
                    }
                } else {
                    log.debug("Send ExpectToSupply response to ${request.requestingInstitutionSymbol}");
                    reshareActionService.sendResponse(request,  'ExpectToSupply', [:], eventResultDetails)
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
