package org.olf.rs.timers;

import org.olf.rs.NetworkStatus;
import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolMessageBuildingService;
import org.olf.rs.ProtocolMessageService;
import org.olf.rs.ProtocolResultStatus;
import org.olf.rs.iso18626.ReasonForMessage;
import org.olf.rs.logging.IIso18626LogDetails;
import org.olf.rs.logging.ProtocolAuditService;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Checks to see if There are any requests we need to validate that were received and if they were not, mark it as a resend
 *
 * @author Chas
 *
 */
public class TimerRequestNetworkTimeoutService extends AbstractTimerRequestNetworkService {

    ProtocolAuditService protocolAuditService;
    ProtocolMessageBuildingService protocolMessageBuildingService;
    ProtocolMessageService protocolMessageService;

    TimerRequestNetworkTimeoutService() {
        super(NetworkStatus.Timeout);
    }

    @Override
    public void performNetworkTask(PatronRequest request, Map retryEventData) {
        // We first need to look to see if they have received the request,
        // for this we need to send different message depending on whether they are the requester or not
        // We take the header from the original message
        Map message = [ header: retryEventData.header ];
        String note = protocolMessageBuildingService.buildLastSequence(request);
        Map symbols;
        if (request.isRequester) {
            // Set the message type
            message.messageType = 'REQUESTING_AGENCY_MESSAGE';

            // Need to populate action and note of the message
            message.action = EventISO18626IncomingAbstractService.ACTION_STATUS_REQUEST
            message.note = note

            // Fetch the symbols
            symbols = reshareActionService.requestingAgencyMessageSymbol(request);
        } else {
            // Set the message type
            message.messageType = 'SUPPLYING_AGENCY_MESSAGE';

            // Need to populate messageInfo of the message
            // As the supplier we send it as a status change message
            message.messageInfo = [
                reasonForMessage: ReasonForMessage.MESSAGE_REASON_STATUS_CHANGE,
                note: note
            ];

            // Fetch the symbols
            symbols = reshareActionService.supplyingAgencyMessageSymbol(request);
        }

        // Attempt to send the message, this is just a quick check to see if they receieved the previous message, before we attempt to resend it
        IIso18626LogDetails iso18626LogDetails = protocolAuditService.getIso18626LogDetails();
        Map sendResult = protocolMessageService.sendProtocolMessage(symbols.senderSymbol, symbols.receivingSymbol, message, iso18626LogDetails);
        protocolAuditService.save(request, iso18626LogDetails);

        // Was it successfully sent
        switch (sendResult.status) {
            case ProtocolResultStatus.Sent:
                // Set the network status depending on whether they have already received it or not
                if ((sendResult.response.messageStatus == EventISO18626IncomingAbstractService.STATUS_OK) &&
                    (sendResult.response?.errorData != null)) {
                    // Set the network status to sent
                    reshareActionService.setNetworkStatus(request, NetworkStatus.Sent, null, false);
                } else {
                    // Set it to retry as it means they did not actually receive it or we are talking to someone who dosn't know about our hack, otherwise the error data would be set
                    reshareActionService.setNetworkStatus(request, NetworkStatus.Retry, retryEventData, true);
                }
                break;

            default:
                // We mark the network status as being a retry for everything else
                reshareActionService.setNetworkStatus(request, NetworkStatus.Retry, retryEventData, true);
                break;
        }
	}
}
