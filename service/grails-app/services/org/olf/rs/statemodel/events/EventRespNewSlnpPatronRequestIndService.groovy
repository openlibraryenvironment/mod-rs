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

    /**
     * Auto responder which makes the host LMS service call for request item and if the call is successful, we create audit message 'WillSupply' and change the status to 'SLNP_RES_AWAIT_PICKING'.
     * Following the successful result we next verify that the auto-loan setting is turned ON and if yes we send 'Loaned' status change message which triggers state change to 'SLNP_RES_ITEM_SHIPPED'.
     * In case of unsuccessful call to host LMS and auto-loan turned ON we send 'Unfilled' status change message which triggers state change to 'SLNP_RES_UNFILLED'.
     *
     * @param request - Responder Patron request object
     * @param autoRespondVariant - Setting type, for example, auto-loan
     * @param eventResultDetails - Object containing results for the event such as audit, qualifier, result etc...
     */
    private void autoRespond(PatronRequest request, String autoRespondVariant, EventResultDetails eventResultDetails) {
        log.debug('autoRespond....')

        log.debug("Attempt hold with RequestItem")
        Map requestItemResult = hostLMSService.requestItem(request, request.hrid,
                request.supplierUniqueRecordId, request.patronIdentifier)

        if (requestItemResult.result == true) {
            log.debug("Send WillSupply response to ${request.requestingInstitutionSymbol}")
            eventResultDetails.auditMessage = 'Will Supply'
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCATED_REQUEST_ITEM

            // Send 'Loaned' message
            checkSettingAndSendStatusChangeMessage(autoRespondVariant, ActionEventResultQualifier.QUALIFIER_LOANED, 'Loaned', request, eventResultDetails, 'Shipped')
        } else {
            // Send 'Unfilled' message
            eventResultDetails.auditMessage = 'Failed to place hold for item with bibliographicid ' + request.supplierUniqueRecordId
            checkSettingAndSendStatusChangeMessage(autoRespondVariant, ActionEventResultQualifier.QUALIFIER_UNFILLED, 'Unfilled', request, eventResultDetails, 'Cannot Supply')
        }
    }

    private void checkSettingAndSendStatusChangeMessage(String autoRespondVariant, String qualifier, String status, PatronRequest request,
                                                            EventResultDetails eventResultDetails, String auditMessage) {
        if (autoRespondVariant == 'on:_auto_loan') {
            log.debug("Send response ${status} to ${request.requestingInstitutionSymbol}")
            reshareActionService.sendResponse(request, status, [:], eventResultDetails)
            eventResultDetails.auditMessage = auditMessage;
            eventResultDetails.qualifier = qualifier
        } else {
            eventResultDetails.auditMessage = "Auto responder is ${autoRespondVariant} - manual checking needed"
//            Does customer wants records to be automatically marked with attention needed flag for SLNP?
//            request.needsAttention = true
        }
    }
}
