package org.olf.rs.statemodel

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.olf.rs.HostLMSService
import org.olf.rs.PatronRequest
import org.olf.rs.lms.HostLMSActions

/**
 * Abstract SLNP non-returnables action for common logic - Accept Item NCIP call
 */
abstract class AbstractSlnpNonReturnableAction extends AbstractAction {

    HostLMSService hostLMSService

    private static final String REASON_SPOOFED = 'spoofed'

    protected ActionResultDetails performCommonAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails, String auditMessage) {
        String itemBarcode = "rs-${request.hrid}-${UUID.randomUUID().toString().replaceAll("-", "").substring(0, 4)}"
        try {
            Map acceptResult = hostLMSService.acceptItem(
                    request,
                    itemBarcode,
                    null
            )

            if (acceptResult?.result == true) {
                String message = "Receive succeeded for (temporaryItemBarcode: ${itemBarcode}). ${acceptResult.reason == REASON_SPOOFED ? '(No host LMS integration configured for accept item call)' : 'Host LMS integration: AcceptItem call succeeded.'}"

                // Set the selected item barcode to the new generated value
                request.selectedItemBarcode = itemBarcode

                if (acceptResult.requestUuid) {
                    Map customIdentifiersMap = [:]
                    if (request.customIdentifiers) {
                        customIdentifiersMap = new JsonSlurper().parseText(request.customIdentifiers)
                    }
                    if (acceptResult.requestUuid) {
                        customIdentifiersMap.put("requestUuid", acceptResult.requestUuid)
                    }
                    request.customIdentifiers = new JsonBuilder(customIdentifiersMap).toPrettyString()
                }

                reshareApplicationEventHandlerService.auditEntry(request,
                        request.state,
                        request.state,
                        message,
                        null)
            } else {
                String message = "Host LMS integration: NCIP AcceptItem call failed for temporary item barcode: ${itemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings."
                reshareApplicationEventHandlerService.auditEntry(request,
                        request.state,
                        request.state,
                        message + acceptResult?.problems,
                        null)
            }
        } catch (Exception e) {
            reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, "Host LMS integration: NCIP AcceptItem call failed for temporary item barcode: ${itemBarcode}. Review configuration and try again or deconfigure host LMS integration in settings. " + e.message, null)
        }
        request.save(flush:true, failOnError:true)

        actionResultDetails.auditMessage = auditMessage

        return(actionResultDetails)
    }
}