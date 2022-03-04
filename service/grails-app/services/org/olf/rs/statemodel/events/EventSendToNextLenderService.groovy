package org.olf.rs.statemodel.events;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.ProtocolMessageBuildingService;
import org.olf.rs.ProtocolMessageService;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.settings.AppSetting;

/**
 * This service eveent is abstract as multiple actions can lead to ending the conversation with a supplier so therefore you have multiple events where you want to move onto the next lender
 * @author Chas
 *
 */
public abstract class EventSendToNextLenderService extends AbstractEvent {

    private static final String[] FROM_STATES = [
        Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED,
        Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER,
        Status.PATRON_REQUEST_UNFILLED
    ];

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_LOCAL_REVIEW,
        Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER,
        Status.PATRON_REQUEST_END_OF_ROTA
    ];

    HostLMSService hostLMSService;
    ProtocolMessageBuildingService protocolMessageBuildingService;
    ProtocolMessageService protocolMessageService;

    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    Boolean canLeadToSameState() {
        return(false);
    }

    @Override
    String[] toStates(String model) {
        return(TO_STATES);
    }

    @Override
    String[] fromStates(String model) {
        return(FROM_STATES);
    }

    @Override
    boolean supportsModel(String model) {
        // This event
        return(model == StateModel.MODEL_REQUESTER);
    }

    // This takes a request with the state of REQ_SUPPLIER_IDENTIFIED and changes the state to REQUEST_SENT_TO_SUPPLIER
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // We must have found the request, and it as to be in a state of supplier identifier or unfilled
        if ((request.state?.code == Status.PATRON_REQUEST_SUPPLIER_IDENTIFIED) ||
            (request.state?.code == Status.PATRON_REQUEST_CANCELLED_WITH_SUPPLIER) ||
            (request.state?.code == Status.PATRON_REQUEST_UNFILLED)) {
            log.debug("Got request (HRID Is ${request.hrid}) (Status code is ${request.state?.code})");

            if (request.rota.size() > 0) {
                Map requestMessageRequest  = protocolMessageBuildingService.buildRequestMessage(request);
                log.debug("Built request message request: ${requestMessageRequest }");

                boolean requestSent  = false;

                // There may be problems with entries in the lending string, so we loop through the rota
                // until we reach the end, or we find a potential lender we can talk to. The request must
                // also explicitly state a requestingInstitutionSymbol
                while ((!requestSent) &&
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
                                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_LOCAL_REVIEW);
                                eventResultDetails.auditMessage = 'Sent to local review';
                                return(eventResultDetails);  //Nothing more to do here
                            } else {
                                prr.state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_NOT_SUPPLIED);
                                prr.save(flush: true, failOnError: true);
                                log.debug('Cannot fill locally, skipping');
                                continue;
                            }
                        }

                        // Fill out the directory entry reference if it's not currently set, and try to send.
                        if ((nextResponder != null) &&
                            (s != null) &&
                            (prr.peerSymbol == null)) {
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
                            }
                            requestMessageRequest.bibliographicInfo.supplyingInstitutionSymbol = nextResponder;

                            // Probably need a lender_is_valid check here
                            Map sendResult  = protocolMessageService.sendProtocolMessage(request.requestingInstitutionSymbol, nextResponder, requestMessageRequest);
                            if (sendResult.status == 'SENT') {
                                prr.state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);
                                requestSent  = true;
                            } else {
                                prr.state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER);
                                prr.note = "Result of send : ${sendResult.status}";
                            }
                        } else {
                            log.warn("Lender at position ${request.rotaPosition} invalid, skipping");
                            prr.state = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_UNABLE_TO_CONTACT_SUPPLIER);
                            prr.note = "Send not attempted: Unable to resolve symbol for : ${nextResponder}";
                            }

                        prr.save(flush:true, failOnError:true);
                    } else {
                        log.error("Unable to find rota entry at position ${request.rotaPosition} (Size=${request.rota.size()}) ${( request.rotaPosition ?: -1 < request.rota.size() )}. Try next");
                    }
                       }

                // ToDo - there are three possible states here,not two - Send, End of Rota, Error
                // Did we send a request?
                if (requestSent) {
                    log.debug('sendToNextLender sent to next lender.....');
                    eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_REQUEST_SENT_TO_SUPPLIER);
                    eventResultDetails.auditMessage = 'Sent to next lender';
                } else {
                    // END OF ROTA
                    log.warn('sendToNextLender reached the end of the lending string.....');
                    eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA);
                    eventResultDetails.auditMessage = 'End of rota';
                }
            } else {
                log.warn('Cannot send to next lender - rota is empty');
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_END_OF_ROTA);
            }
        }

        return(eventResultDetails);
    }

    //Check to see if we can find a local copy of the item. If yes, then we go
    //ahead and transitition to local review. If not, transitition to send-to-next-lender

    private boolean checkForLocalCopy(PatronRequest pr) {
        log.debug('Checking to see if we have a local copy available');

        //Let's still go ahead and try to call the LMS Adapter to find a copy of the request
        ItemLocation location = hostLMSService.getHostLMSActions().determineBestLocation(pr);
        log.debug("Got ${location} as a result of local host lms lookup");

        return(location != null);
    }
}
