package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.custprops.CustomProperty
import com.k_int.web.toolkit.settings.AppSetting
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.olf.rs.DirectoryEntryService
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest
import org.olf.rs.RequestVolume
import org.olf.rs.ReshareActionService
import org.olf.rs.SettingsService
import org.olf.rs.constants.Directory
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.*

/**
 * This event service takes a new SLNP responder patron request
 * to perform validation and respond automatically depending on configured settings.
 */
public class EventRespNewSlnpPatronRequestIndService extends AbstractEvent {
    public static final String VOLUME_STATUS_REQUESTED_FROM_THE_ILS = 'requested_from_the_ils'
    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip"

    ReshareActionService reshareActionService
    HostLMSService hostLMSService
    DirectoryEntryService directoryEntryService
    SettingsService settingsService

    @Override
    String name() {
        return(Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION)
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID)
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id}} isRequester=${request?.isRequester} StateModel=${request.stateModel.shortcode}")
        }

        // Validation
        request.needsAttention = false
        eventResultDetails.saveData = true
        eventResultDetails.auditMessage = "Request validation done"

        // Loan Auto Responder
        if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_RESPONDER)) {
            try {
                log.debug('autoRespond....')
                String autoLoanSetting = AppSetting.findByKey('auto_responder_status')?.value
                autoRespond(request, autoLoanSetting.toLowerCase(), eventResultDetails)
            } catch (Exception e) {
                log.error("Problem in NCIP Request Item call: ${e.getMessage()}", e)
                eventResultDetails.auditMessage = String.format("NCIP Request Item call failure: %s", e.getMessage())
                request.needsAttention = true
            }
        }

        return(eventResultDetails)
    }

    /**
     * Auto responder which makes the host LMS service call for request item and if the call is successful, we create audit message 'WillSupply' and change the status to 'SLNP_RES_AWAIT_PICKING'.
     * Following the successful result we next verify that the auto-loan setting is turned ON and if yes we send 'Loaned' status change message which triggers state change to 'SLNP_RES_AWAIT_PICKING'.
     * In case of unsuccessful call to host LMS and auto-loan turned ON we send 'Unfilled' status change message which triggers state change to 'SLNP_RES_UNFILLED'.
     *
     * @param request - Responder Patron request object
     * @param autoRespondVariant - Setting type, for example, auto-loan
     * @param eventResultDetails - Object containing results for the event such as audit, qualifier, result etc...
     */
    private void autoRespond(PatronRequest request, String autoRespondVariant, EventResultDetails eventResultDetails) {
        if (autoRespondVariant == "off") {
            log.debug("Auto responder is off, manual checking is required!")
            return
        }

        log.debug("Attempt hold with RequestItem")
        CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(request.resolvedRequester?.owner, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID)
        String institutionalPatronIdValue = institutionalPatronId?.value
        if (!institutionalPatronIdValue) {
            // If nothing on the Directory Entry then fallback to the default in settings
            AppSetting defaultInstitutionalPatronId = AppSetting.findByKey(SettingsData.SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID)
            institutionalPatronIdValue = defaultInstitutionalPatronId?.value
        }
        if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) {
            Map requestItemResult = hostLMSService.requestItem(request, request.hrid,
                    request.supplierUniqueRecordId, institutionalPatronIdValue)
            //is request item enabled for this responder?
            if (requestItemResult.result == true) {
                log.debug("Will supply: ${requestItemResult}")
                eventResultDetails.auditMessage = "Will Supply"
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM
                if (requestItemResult.location) {
                    request.pickupLocation = requestItemResult.location
                }
                if (requestItemResult.callNumber) {
                    request.localCallNumber = requestItemResult.callNumber
                }
                if (requestItemResult.itemId) {
                    RequestVolume rv = request.volumes.find { rv -> rv.itemId == requestItemResult.itemId }
                    // If there's no rv
                    if (!rv) {
                        rv = new RequestVolume(
                                name: request.volume ?: requestItemResult.itemId,
                                itemId: requestItemResult.itemId,
                                status: RequestVolume.lookupStatus(VOLUME_STATUS_REQUESTED_FROM_THE_ILS)
                        )
                        rv.callNumber = requestItemResult.callNumber
                        request.addToVolumes(rv)
                    }
                }
                if (requestItemResult.userUuid || requestItemResult.requestId) {
                    Map customIdentifiersMap = [:]
                    if (request.customIdentifiers) {
                        customIdentifiersMap = new JsonSlurper().parseText(request.customIdentifiers)
                    }
                    if (requestItemResult.userUuid) {
                        customIdentifiersMap.put("patronUuid", requestItemResult.userUuid)
                    }
                    if (requestItemResult.requestId) {
                        customIdentifiersMap.put("requestUuid", requestItemResult.requestId)
                        request.externalHoldRequestId = requestItemResult.requestId
                    }
                    request.customIdentifiers = new JsonBuilder(customIdentifiersMap).toPrettyString()
                }

                if (autoRespondVariant == "on:_loaned_and_cannot_supply") {
                    log.debug("Send response Loaned to ${request.requestingInstitutionSymbol}")
                    reshareActionService.sendResponse(request, "Loaned", [:], eventResultDetails)
                    eventResultDetails.auditMessage = "Shipped"
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM
                }
            } else {
                log.debug("Send response Unfilled to ${request.requestingInstitutionSymbol}")
                reshareActionService.sendResponse(request, "Unfilled", [:], eventResultDetails)
                eventResultDetails.auditMessage = "Cannot Supply"
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_UNFILLED
            }
        } else {
            log.debug("NCIP not configured. Send response Unfilled to ${request.requestingInstitutionSymbol}")
            reshareActionService.sendResponse(request, "Unfilled", [:], eventResultDetails)
            eventResultDetails.auditMessage = "Cannot Supply. NCIP not configured."
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_UNFILLED
        }
    }
}
