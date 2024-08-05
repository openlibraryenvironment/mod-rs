package org.olf.rs.statemodel.events

import com.k_int.web.toolkit.settings.AppSetting
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.HostLMSService
import org.olf.rs.NewRequestService
import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.lms.HostLMSActions
import org.olf.rs.logging.INcipLogDetails
import org.olf.rs.logging.ProtocolAuditService
import org.olf.rs.patronRequest.PickupLocationService
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.ActionEventResultQualifier
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events
import org.olf.rs.statemodel.StateModel

/**
 * This event service takes a new requester SLNP patron request and validates and generates HRID.
 */
public class EventReqNewSlnpPatronRequestIndService extends AbstractEvent {


    PickupLocationService pickupLocationService
    NewRequestService newRequestService
    HostLMSService hostLMSService
    ProtocolAuditService protocolAuditService
    SettingsService settingsService

    private static final String REASON_SPOOFED = 'spoofed'

    @Override
    String name() {
        return(Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION)
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID)
    }

    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id} isRequester=${request?.isRequester} StateModel=${eventData.stateModel}")
            return (eventResultDetails)
        }

        // Generate a human readable ID to use
        if (!request.hrid) {
            request.hrid = newRequestService.generateHrid()
            log.debug("set request.hrid to ${request.hrid}")
        }

        if (request.requestingInstitutionSymbol != null) {
            Symbol s = reshareApplicationEventHandlerService.resolveCombinedSymbol(request.requestingInstitutionSymbol)
            if (s != null) {
                request.resolvedRequester = s
            }
        }

        if (!request.resolvedPickupLocation) {
            pickupLocationService.checkByName(request)
        }

        HostLMSActions hostLMSActions = hostLMSService.getHostLMSActions()

        if (hostLMSActions) {
            log.debug('Auto Supply....')
            INcipLogDetails ncipLogDetails = protocolAuditService.getNcipLogDetails()
            String userId = request.externalHoldRequestId

            try {
                Map userFiscalTransactionResult = hostLMSActions.createUserFiscalTransaction(settingsService, userId, ncipLogDetails)

                if (userFiscalTransactionResult?.result == true) {
                    String message = "Receive succeeded for (userId: ${userId}). ${userFiscalTransactionResult.reason == REASON_SPOOFED ? '(No host LMS integration configured for create user fiscal transaction call)' : 'Host LMS integration: CreateUserFiscalTransaction call succeeded.'}"

                    // For SLNP non-returnables we need to set the Qualifier depending on the non-returnable setting
                    // BVB -> SLNP_REQ_DOCUMENT_SUPPLIED, BSZ -> SLNP_REQ_DOCUMENT_AVAILABLE
                    if (request.stateModel.shortcode.equalsIgnoreCase(StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER)) {
                        String autoSupplySetting = AppSetting.findByKey(SettingsData.SETTING_AUTO_RESPONDER_REQUESTER_NON_RETURNABLE)?.value
                        if (autoSupplySetting) {
                            autoSupplySetting = autoSupplySetting.toLowerCase()
                            if (autoSupplySetting == "on:_available") {
                                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DOCUMENT_AVAILABLE
                            } else if (autoSupplySetting == "on:_supplied") {
                                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DOCUMENT_SUPPLIED
                            } else {
                                log.debug('Auto supply is turned off!')
                            }
                        }
                    }

                    reshareApplicationEventHandlerService.auditEntry(request,
                            request.state,
                            request.state,
                            message,
                            null)
                } else {
                    String message = "Host LMS integration: NCIP CreateUserFiscalTransaction call failed for userId: ${userId}. Review configuration and try again or deconfigure host LMS integration in settings."
                    reshareApplicationEventHandlerService.auditEntry(request,
                            request.state,
                            request.state,
                            message + userFiscalTransactionResult?.problems,
                            null)
                }
            } catch (Exception e) {
                reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, "Host LMS integration: NCIP CreateUserFiscalTransaction call failed for userId: ${userId}. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message, null)
            }
        }

        request.needsAttention = false
        eventResultDetails.auditMessage = "Request validation done"
        eventResultDetails.saveData = true

        return(eventResultDetails)
    }
}
