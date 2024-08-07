package org.olf.rs.statemodel.events

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.HostLMSService
import org.olf.rs.NewRequestService
import org.olf.rs.PatronRequest
import org.olf.rs.SettingsService
import org.olf.rs.lms.HostLMSActions
import org.olf.rs.logging.INcipLogDetails
import org.olf.rs.logging.ProtocolAuditService
import org.olf.rs.patronRequest.PickupLocationService
import org.olf.rs.statemodel.AbstractEvent
import org.olf.rs.statemodel.EventFetchRequestMethod
import org.olf.rs.statemodel.EventResultDetails
import org.olf.rs.statemodel.Events

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
            String userId = request.patronIdentifier

            try {
                Map userFiscalTransactionResult = hostLMSActions.createUserFiscalTransaction(settingsService, userId, ncipLogDetails)

                if (userFiscalTransactionResult?.result == true) {
                    String message = "Receive succeeded for (userId: ${userId}). ${userFiscalTransactionResult.reason == REASON_SPOOFED ? '(No host LMS integration configured for create user fiscal transaction call)' : 'Host LMS integration: CreateUserFiscalTransaction call succeeded.'}"

                    if (userFiscalTransactionResult.userUuid && userFiscalTransactionResult.feeUuid) {
                        Map customIdentifiersMap = [:]
                        if (request.customIdentifiers) {
                            customIdentifiersMap = new JsonSlurper().parseText(request.customIdentifiers)
                        }
                        customIdentifiersMap.put("userUuid", userFiscalTransactionResult.userUuid)
                        customIdentifiersMap.put("feeUuid", userFiscalTransactionResult.feeUuid)
                        request.customIdentifiers = new JsonBuilder(customIdentifiersMap).toPrettyString()
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
