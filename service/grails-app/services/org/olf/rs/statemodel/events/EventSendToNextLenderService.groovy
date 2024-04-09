package org.olf.rs.statemodel.events;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.HostLMSService;
import org.olf.rs.NetworkStatus;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.ProtocolMessageBuildingService;
import org.olf.rs.ProtocolMessageService;
import org.olf.rs.ProtocolType;
import org.olf.rs.ReshareActionService;
import org.olf.rs.SettingsService;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;

import com.k_int.web.toolkit.settings.AppSetting;

/**
 * This service eveent is abstract as multiple actions can lead to ending the conversation with a supplier so therefore you have multiple events where you want to move onto the next lender
 * @author Chas
 *
 */
public abstract class EventSendToNextLenderService extends AbstractEvent {

    HostLMSService hostLMSService;
    ProtocolMessageBuildingService protocolMessageBuildingService;
    ProtocolMessageService protocolMessageService;
    ReshareActionService reshareActionService;
    SettingsService settingsService;

    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    // This takes a request with the state of REQ_SUPPLIER_IDENTIFIED and changes the state to REQUEST_SENT_TO_SUPPLIER
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        log.debug("Got request (HRID Is ${request.hrid}) (Status code is ${request.state?.code})");

        // Set the network status to Idle, just in case we do not attempt to send the message, to avoid confusion
        request.networkStatus = NetworkStatus.Idle;

        if (request.rota.size() > 0) {
            boolean messageTried  = false;
            boolean lookAtNextResponder = true;

            // There may be problems with entries in the lending string, so we loop through the rota
            // until we reach the end, or we find a potential lender we can talk to. The request must
            // also explicitly state a requestingInstitutionSymbol
            while (lookAtNextResponder &&
                   (request.rota.size() > 0) &&
                   ((request.rotaPosition ?: -1) < request.rota.size()) &&
                   (request.requestingInstitutionSymbol != null)) {
                // We have rota entries left, work out the next one
                request.rotaPosition = (request.rotaPosition != null ? request.rotaPosition + 1 : 0);

                // get the responder
                PatronRequestRota prr = request.rota.find({ rotaEntry -> rotaEntry.rotaPosition == request.rotaPosition });
                if (prr != null) {
                    String nextResponder = prr.directoryId

                    log.debug("Attempt to resolve symbol \"${nextResponder}\"");
                    Symbol s = (nextResponder != null) ? reshareApplicationEventHandlerService.resolveCombinedSymbol(nextResponder) : null;
                    log.debug("Resolved nextResponder to ${s} with status ${s?.owner?.status?.value}");
                    String ownerStatus = s.owner?.status?.value;

                    if (ownerStatus == 'Managed' || ownerStatus == 'managed') {
                        log.debug('Responder is local') //, going to review state");
                        boolean doLocalReview  = true;
                        //Check to see if we're going to try to automatically check for local
                        //copies
                        String localAutoRespond = AppSetting.findByKey('auto_responder_local')?.value;
                        if (localAutoRespond?.toLowerCase()?.startsWith('on')) {
                            log.debug("Setting request.supplierUniqueRecordId to ${prr.instanceIdentifier}");
                            request.supplierUniqueRecordId = prr.instanceIdentifier;
                            boolean hasLocalCopy = checkForLocalCopy(request);
                            if (hasLocalCopy) {
                                reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, 'Local auto-responder located a local copy - requires review', null);
                            } else {
                                doLocalReview  = false;
                                reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, 'Local auto-responder did not locate a local copy - sent to next lender', null);
                            }
                        } else {
                            reshareApplicationEventHandlerService.auditEntry(request, request.state, request.state, 'Local auto-responder off - requires manual checking', null);
                        }

                        if (doLocalReview) {
                            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_LOCAL_REVIEW;
                            eventResultDetails.auditMessage = 'Sent to local review';
                            return(eventResultDetails);  //Nothing more to do here
                        } else {
                            log.debug('Cannot fill locally, skipping');
                            continue;
                        }
                    }

                    // Fill out the directory entry reference if it's not currently set, and try to send.
                    if ((nextResponder != null) &&
                        (s != null) &&
                        (prr.peerSymbol == null)) {
                        // Determine the message we will be sending, the id includes the rota position, so needs to be dtermined after the rota position has been calculated
                        Map requestMessageRequest  = protocolMessageBuildingService.buildRequestMessage(request);
                        log.debug("Built request message request: ${requestMessageRequest }");

                        if (s != null) {
                            request.resolvedSupplier = s;
                            log.debug("LOCKING: PatronRequestRota[${prr.id}] - REQUEST");
                            prr.lock();
                            log.debug("LOCKING: PatronRequestRota[${prr.id}] - OBTAINED");
                            prr.peerSymbol = s;
                            prr.save(flush:true, failOnError:true);

                            requestMessageRequest.header.supplyingAgencyId = [
                                agencyIdType : s.authority?.symbol,
                                agencyIdValue : s.symbol,
                            ];
                        } else {
                            log.warn("Cannot understand or resolve symbol ${nextResponder}");
                        }

                        if ((prr.instanceIdentifier != null) && (prr.instanceIdentifier.length() > 0)) {
                            // update requestMessageRequest.supplierUniqueRecordId to the system number specified in the rota
                            requestMessageRequest.bibliographicInfo.supplierUniqueRecordId = prr.instanceIdentifier;
                            log.debug("Setting supplierUniqueRecordId to ${prr.instanceIdentifier} from PatronRequestRota ${prr}");
                        } else {
                            log.debug("No instanceIdentifier present for PatronRequestRota ${prr}");
                        }

                        requestMessageRequest.bibliographicInfo.bibliographicRecordId.add([ bibliographicRecordIdentifierCode:'supplyingInstitutionSymbol', bibliographicRecordIdentifier: nextResponder ])

                        // No longer need to look at next responder
                        lookAtNextResponder = false;

                        // Probably need a lender_is_valid check here
                        if (!reshareActionService.sendProtocolMessage(request, request.requestingInstitutionSymbol, nextResponder, requestMessageRequest)) {
                            // Failed to send to lender
                            messageTried = true;
                            prr.note = "Result of send : ${request.networkStatus.toString()}";
                        }
                    } else {
                        log.warn("Lender at position ${request.rotaPosition} invalid, skipping");
                        prr.note = "Send not attempted: Unable to resolve symbol for : ${nextResponder}";
                    }

                    prr.save(flush:true, failOnError:true);
                } else {
                    // We have come to the end of the rota, this is handled below, it is no longer an error
                }
            }

            // Did we send a request?
            if (request.networkStatus == NetworkStatus.Sent) {
                log.debug('sendToNextLender sent to next lender.....');
                eventResultDetails.auditMessage = 'Sent to next lender';
            } else if (messageTried) {
                // We will not set the state yet, just the audit message
                eventResultDetails.auditMessage = 'Problem sending to supplier, will reattempt';
            } else {
                // END OF ROTA
                log.warn('sendToNextLender reached the end of the lending string.....');
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_END_OF_ROTA;
                eventResultDetails.auditMessage = 'End of rota';
            }
        } else {
            log.warn('Cannot send to next lender - rota is empty');
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_END_OF_ROTA;
            eventResultDetails.auditMessage = 'End of rota';
        }

        return(eventResultDetails);
    }

    //Check to see if we can find a local copy of the item. If yes, then we go
    //ahead and transitition to local review. If not, transitition to send-to-next-lender

    private boolean checkForLocalCopy(PatronRequest request) {
        log.debug('Checking to see if we have a local copy available');

        //Let's still go ahead and try to call the LMS Adapter to find a copy of the request
        ItemLocation location = hostLMSService.determineBestLocation(request, ProtocolType.Z3950_REQUESTER);
        log.debug("Got ${location} as a result of local host lms lookup");

        return(location != null);
    }
}
