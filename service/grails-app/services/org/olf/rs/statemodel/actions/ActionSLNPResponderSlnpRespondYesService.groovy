package org.olf.rs.statemodel.actions

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
import org.olf.rs.lms.ItemLocation
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.StateModel

/**
 * Performs an answer will supply action for the responder
 *
 */
public class ActionSLNPResponderSlnpRespondYesService extends ActionResponderService {

    ReshareActionService reshareActionService
    HostLMSService hostLMSService
    DirectoryEntryService directoryEntryService
    SettingsService settingsService

    public static final String VOLUME_STATUS_REQUESTED_FROM_THE_ILS = 'requested_from_the_ils'
    private static final String SETTING_REQUEST_ITEM_NCIP = "ncip"

    @Override
    String name() {
        return(Actions.ACTION_SLNP_RESPONDER_RESPOND_YES);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // Loan Auto Responder
        if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_RESPONDER)) {
            String autoLoanSetting = AppSetting.findByKey('auto_responder_status')?.value
            autoRespond(request, autoLoanSetting, actionResultDetails)
        }

        return(actionResultDetails);
    }

    private void autoRespond(PatronRequest request, String autoRespondVariant, ActionResultDetails actionResultDetails) {
        log.debug("Attempt hold with RequestItem")
        CustomProperty institutionalPatronId = directoryEntryService.extractCustomPropertyFromDirectoryEntry(request.resolvedRequester?.owner, Directory.KEY_LOCAL_INSTITUTION_PATRON_ID)
        String institutionalPatronIdValue = institutionalPatronId?.value
        if (!institutionalPatronIdValue) {
            // If nothing on the Directory Entry then fallback to the default in settings
            AppSetting defaultInstitutionalPatronId = AppSetting.findByKey(SettingsData.SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID)
            institutionalPatronIdValue = defaultInstitutionalPatronId?.value
        }
        if (settingsService.hasSettingValue(SettingsData.SETTING_USE_REQUEST_ITEM, SETTING_REQUEST_ITEM_NCIP)) {
            String folioLocationFilter = directoryEntryService.extractCustomPropertyFromDirectoryEntry(
                    request.resolvedSupplier?.owner, Directory.KEY_FOLIO_LOCATION_FILTER)?.value
            Map requestItemResult = hostLMSService.requestItem(request,
                    request.resolvedSupplier?.owner?.lmsLocationCode, folioLocationFilter,
                    request.supplierUniqueRecordId, institutionalPatronIdValue)
            //is request item enabled for this responder?
            if (requestItemResult.result == true) {
                if (autoRespondVariant == "on:_loaned_and_cannot_supply") {
                    log.debug("Send response Loaned to ${request.requestingInstitutionSymbol}")
                    reshareActionService.sendResponse(request, "Loaned", [:], actionResultDetails)
                    actionResultDetails.auditMessage = "Shipped"
                }

                if (requestItemResult.location) {
                    request.pickupLocation = requestItemResult.location + ", " + requestItemResult.library
                    ItemLocation location = new ItemLocation(location: requestItemResult.library,
                            shelvingLocation: requestItemResult.location,
                            callNumber: requestItemResult.callNumber)
                    reshareApplicationEventHandlerService.routeRequestToLocation(request, location)
                }
                if (requestItemResult.itemId) {
                    RequestVolume rv = request.volumes.find { rv -> rv.itemId == requestItemResult.itemId }
                    // If there's no rv
                    if (!rv) {
                        request.volumes?.clear()
                        rv = new RequestVolume(
                                name: request.volume ?: requestItemResult.itemId,
                                itemId: requestItemResult.itemId,
                                status: RequestVolume.lookupStatus(VOLUME_STATUS_REQUESTED_FROM_THE_ILS)
                        )
                        rv.callNumber = requestItemResult.callNumber
                        request.addToVolumes(rv)
                    } else {
                        rv.callNumber = requestItemResult.callNumber
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
            } else {
                log.debug("Send response Unfilled to ${request.requestingInstitutionSymbol}")
                reshareActionService.sendResponse(request, "Unfilled", [:], actionResultDetails)
                actionResultDetails.auditMessage = "Cannot Supply"
                handleUnfilledResponse(actionResultDetails, autoRespondVariant)
            }
        } else {
            log.debug("NCIP not configured. Send response Unfilled to ${request.requestingInstitutionSymbol}")
            reshareActionService.sendResponse(request, "Unfilled", [:], actionResultDetails)
            actionResultDetails.auditMessage = "Cannot Supply. NCIP not configured."
            handleUnfilledResponse(actionResultDetails, autoRespondVariant)
        }
    }

    private static void handleUnfilledResponse(ActionResultDetails actionResultDetails, String autoRespondVariant) {
        if (autoRespondVariant == "off") {
            actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_UNFILLED
        }
    }
}