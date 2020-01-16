package org.olf.rs;

import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestRota
import org.olf.rs.PatronRequestNotification
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StateModel
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import java.time.LocalDateTime;

/**
 * Handle user events.
 *
 * wheras ReshareApplicationEventHandlerService is about detecting and handling system generated events - incoming protocol messages etc
 * this class is the home for user triggered activities - checking an item into reshare, marking the pull slip as printed etc.
 */
public class ReshareActionService {

  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  ProtocolMessageService protocolMessageService
  HostLMSService hostLMSService

  public boolean checkInToReshare(PatronRequest pr, Map actionParams) {
    log.debug("checkInToReshare(${pr})");
    boolean result = false;

    if ( actionParams?.itemBarcode != null ) {
      if ( pr.state.code=='RES_AWAIT_PICKING' || pr.state.code=='RES_AWAIT_PROXY_BORROWER') {
        // auditEntry(pr, pr.state, s, 'Checked in', null);
        // See if we can identify a borrower proxy for the requesting location
        String borrower_proxy_barcode = null;

        if ( borrower_proxy_barcode != null ) {
          // Attempt HostLMSService checkout
          if ( hostLMSService.checkoutItem(actionParams?.itemBarcode, borrower_proxy_barcode) ) {
            Status s = Status.lookup('Responder', 'RES_CHECKED_IN_TO_RESHARE');
            auditEntry(pr, pr.state, s, 'Checked In', null);
            pr.state = s;
            pr.selectedItemBarcode = actionParams?.itemBarcode;
            pr.save(flush:true, failOnError:true);
          }
          else {
            Status s = Status.lookup('Responder', 'RES_AWAIT_LMS_CHECKOUT');
            auditEntry(pr, pr.state, s, 'Check In Failed - Manual checkout needed', null);
            pr.state = s;
            pr.selectedItemBarcode = actionParams?.itemBarcode;
            pr.save(flush:true, failOnError:true);
          }
        }
        else {
          Status s = Status.lookup('Responder', 'RES_AWAIT_PROXY_BORROWER');
          auditEntry(pr, pr.state, s, 'Unable to check-in. No Proxy borrower account for requesting location. Please set and re-check-in', null);
          pr.selectedItemBarcode = actionParams?.itemBarcode;
          pr.state = s;
          pr.save(flush:true, failOnError:true);
        }

        result = true;
      }
      else {
        log.warn("Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr.state.code})");
      }
    }

    return result;
  }

  public boolean supplierCannotSupply(PatronRequest pr, Map actionParams) {
    boolean result = false;
    log.debug("supplierCannotSupply(${pr})");
    return result;
  }

  public boolean notiftyPullSlipPrinted(PatronRequest pr) {
    log.debug("notiftyPullSlipPrinted(${pr})");
    boolean result = false;
    Status s = Status.lookup('Responder', 'RES_AWAIT_PICKING');
    if ( s && pr.state.code=='RES_NEW_AWAIT_PULL_SLIP') {
      pr.state = s;
      pr.save(flush:true, failOnError:true);
      result = true;
    }
    else {
      log.warn("Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr.state.code})");
      result.code=-1; // Wrong state
      result.message="Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr?.state?.code})"
    }

    return result;
  }

  public boolean notifySupplierShip(PatronRequest pr) {
    log.debug("notifySupplierShip(${pr})");
    boolean result = false;
    Status s = Status.lookup('Responder', 'RES_ITEM_SHIPPED');
    if ( s && pr.state.code=='RES_AWAIT_SHIP') {
      pr.state = s;
      pr.save(flush:true, failOnError:true);
      result = true;
    }
    else {
      log.warn("Unable to locate RES_AWAIT_SHIP OR request not currently RES_AWAIT_SHIP(${pr.state.code})");
    }

    return result;
  }

  public boolean sendMessage(PatronRequest pr, Object actionParams) {
    log.debug("actionMessage(${pr})");
    boolean result = false;
    // Sending a message does not change the state of a request


    // If the actionParams does not contain a note then this method should do nothing
    if (actionParams.isNull("note")) {
      return false;
    }


    Map eventData = [header:[]];

    String message_sender_symbol = "unassigned_message_sender_symbol";
    String peer_symbol = "unassigned_peer_symbol"

    // This is for sending a REQUESTING AGENCY message to the SUPPLYING AGENCY
    if (pr.isRequester == true) {
      message_sender_symbol = pr.requestingInstitutionSymbol;
      Long rotaPosition = pr.rotaPosition;
      
      // We check that it is sensible to send a message, ie that we have a non-empty rota and are pointing at an entry in that.
      if (pr.rota.isEmpty()) {
        log.error("sendMessage has been given an empty rota")
        return false;
      }

      if (rotaPosition == null) {
        log.error("sendMessage could not find current rota postition")
        return false;
      } else if (pr.rota.empty()) {
        log.error("sendMessage has been handed an empty rota")
        return false;
      }

      log.debug("ROTA TYPE: ${pr.rota.getClass()}")
      PatronRequestRota prr = pr.rota.find({it.rotaPosition == rotaPosition})
      log.debug("ROTA at position ${pr.rotaPosition}: ${prr}")

      peer_symbol = "${prr.peerSymbol.authority.symbol}:${prr.peerSymbol.symbol}"

      eventData.messageType = 'REQUESTING_AGENCY_MESSAGE';

      eventData.header = [
        supplyingAgencyId: [
          agencyIdType:peer_symbol.split(":")[0],
          agencyIdValue:peer_symbol.split(":")[1],
        ],
        requestingAgencyId:[
          agencyIdType:message_sender_symbol.split(":")[0],
          agencyIdValue:message_sender_symbol.split(":")[1],
        ],
        requestingAgencyRequestId:pr.hrid ?: pr.id,
        supplyingAgencyRequestId:pr.peerRequestIdentifier,
      ]

      eventData.activeSection = [action:"Notification", note:actionParams.note]

    } // This is for sending a SUPPLYING AGENCY message to the REQUESTING AGENCY
    else {
      message_sender_symbol = pr.supplyingInstitutionSymbol;
      peer_symbol = pr.requestingInstitutionSymbol;

      eventData.messageType = 'SUPPLYING_AGENCY_MESSAGE'

      eventData.header = [
        supplyingAgencyId: [
          agencyIdType:message_sender_symbol.split(":")[0],
          agencyIdValue:message_sender_symbol.split(":")[1],
        ],
        requestingAgencyId:[
          agencyIdType:peer_symbol.split(":")[0],
          agencyIdValue:peer_symbol.split(":")[1],
        ],
        requestingAgencyRequestId:pr.peerRequestIdentifier,
        supplyingAgencyRequestId:pr.id,
      ]

      eventData.messageInfo = [reasonForMessage:"Notification", note:actionParams.note]

    }

    def send_result = protocolMessageService.sendProtocolMessage(message_sender_symbol, peer_symbol, eventData);
    
    def outboundMessage = new PatronRequestNotification()
    outboundMessage.setPatronRequest(pr)
    outboundMessage.setTimestamp(LocalDateTime.now())
    outboundMessage.setMessageSender(resolveCombinedSymbol(message_sender_symbol))
    outboundMessage.setMessageReceiver(resolveCombinedSymbol(peer_symbol))
    outboundMessage.setIsSender(true)
    outboundMessage.setMessageContent(actionParams.note)

    log.debug("Outbound Message: ${outboundMessage}")
    outboundMessage.save(flush:true, failOnError:true)

    if ( send_result=='SENT') {
      result = true;
    }
    else {
      log.warn("Unable to send protocol message");
    }
    return result;
  }

public boolean changeMessageSeenState(PatronRequest pr, Object actionParams) {
    log.debug("actionMessage(${pr})");
    boolean result = false;

    if (actionParams.isNull("id")){
      return result
    }
    if (actionParams.isNull("seenStatus")){
      log.warn("No seen status was sent to changeMessageSeenState")
      return result
    }

    def id = actionParams.id
    PatronRequestNotification message = PatronRequestNotification.findById(id)
    if ( message == null ) {
      log.warn("Unable to locate PatronRequestNotification corresponding to ID or hrid in requestingAgencyRequestId \"${id}\"");
      return result
    }

    message.setSeen(actionParams.seenStatus)
    message.save(flush:true, failOnError:true)

    result = true;
    return result;
  }

  private void auditEntry(PatronRequest pr, Status from, Status to, String message, Map data) {

    String json_data = ( data != null ) ? JsonOutput.toJson(data).toString() : null;
    LocalDateTime ts = LocalDateTime.now();
    log.debug("add audit entry at ${ts}");

    pr.addToAudit( new PatronRequestAudit(
      patronRequest: pr,
      dateCreated:ts,
      fromStatus:from,
      toStatus:to,
      duration:null,
      message: message,
      auditData: json_data))
  }

  private Symbol resolveSymbol(String authorty, String symbol) {
    Symbol result = null;
    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                   [authority:authorty?.toUpperCase(), symbol:symbol?.toUpperCase()]);
    if ( symbol_list.size() == 1 ) {
      result = symbol_list.get(0);
    }

    return result;
  }

  private Symbol resolveCombinedSymbol(String combinedString) {
    Symbol result = null;
    if ( combinedString != null ) {
      String[] name_components = combinedString.split(':');
      if ( name_components.length == 2 ) {
        result = resolveSymbol(name_components[0], name_components[1]);
      }
    }
    return result;
  }

  public simpleTransition(PatronRequest pr, Map params, String state_model, String target_status, String p_message=null) {

    log.debug("request to transition ${pr} to ${target_status}");
    def new_state = reshareApplicationEventHandlerService.lookupStatus(state_model, target_status);

    if ( ( pr != null ) && ( new_state != null ) ) {
      String message = p_message ?: "Simple Transition ${pr.state?.code} to ${new_state.code}".toString()

      reshareApplicationEventHandlerService.auditEntry(pr,
                                      pr.state,
                                      new_state,
                                      message, null);
      pr.state=new_state;
      pr.save(flush:true, failOnError:true);
      log.debug("Saved new state ${new_state.code} for pr ${pr.id}");
    }
  }
}
