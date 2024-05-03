package org.olf.rs.statemodel.events


import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest
import org.olf.rs.ReshareActionService
import org.olf.rs.statemodel.*
/**
 * This event service takes a new SLNP responder patron request
 * to perform validation and respond automatically depending on configured settings.
 */
public class EventRespNewSlnpPatronRequestIndService extends AbstractEvent {

    ReshareActionService reshareActionService
    HostLMSService hostLMSService

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

        // Auto Responder
        try {
            String autoLoanSetting = AppSetting.findByKey('auto_responder_status')?.value
            autoRespond(request, autoLoanSetting.toLowerCase(), eventResultDetails)
        } catch (Exception e) {
            log.error("Problem in auto respond: ${e.getMessage()}", e)
        }

        return(eventResultDetails)
    }

    private void autoRespond(PatronRequest request, String autoRespondVariant, EventResultDetails eventResultDetails) {
        log.debug('autoRespond....')

        log.debug("Attempt hold with RequestItem")
        Map requestItemResult = hostLMSService.requestItem(request, request.hrid,
                request.supplierUniqueRecordId, institutionalPatronIdValue)

        if (requestItemResult.result == true) {
            log.debug("Send WillSupply response to ${request.requestingInstitutionSymbol}")
            eventResultDetails.auditMessage = 'Will Supply'
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM

            // Respond 'Loaned' depending on setting
            checkSettingAndSendResponse(autoRespondVariant, ActionEventResultQualifier.QUALIFIER_LOANED, 'Loaned', request, eventResultDetails)
        } else {
            // Respond 'Unfilled' depending on setting
            eventResultDetails.auditMessage = 'Failed to place hold for item with bibliographicid ' + request.supplierUniqueRecordId
            checkSettingAndSendResponse(autoRespondVariant, ActionEventResultQualifier.QUALIFIER_UNFILLED, 'Unfilled', request, eventResultDetails)
        }
    }

    private void checkSettingAndSendResponse(String autoRespondVariant, ActionEventResultQualifier qualifier, String status, PatronRequest request, EventResultDetails eventResultDetails) {
        if (autoRespondVariant == 'on:_auto_loan') {
            log.debug("Send status ${status} change to ${request.requestingInstitutionSymbol}")
            reshareActionService.sendStatusChange(request, eventResultDetails, status)
            eventResultDetails.qualifier = qualifier
        } else {
            eventResultDetails.auditMessage = "Auto responder is ${autoRespondSetting} - manual checking needed"
            request.needsAttention = true
        }
    }
}
