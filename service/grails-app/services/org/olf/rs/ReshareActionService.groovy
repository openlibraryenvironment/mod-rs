package org.olf.rs;


import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
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

  ProtocolMessageService protocolMessageService

  public boolean checkInToReshare(PatronRequest pr, Map actionParams) {
    log.debug("checkInToReshare(${pr})");
    boolean result = false;
    Status s = Status.lookup('Responder', 'RES_CHECKED_IN_TO_RESHARE');
    if ( s && pr.state.code=='RES_AWAIT_PICKING') {
      pr.state = s;
      pr.selectedItemBarcode = actionParams?.itemBarcode;
      pr.save(flush:true, failOnError:true);
      result = true;
    }
    else {
      log.warn("Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr.state.code})");
    }

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

    if (pr.isRequester == true) {
      String message_sender_symbol = pr.requestingInstitutionSymbol;
      Long rotaPosition = pr.rotaPosition;
      String peer_symbol = pr.rota[rotaPosition];

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
        requestingAgencyRequestId:pr.id,
        supplyingAgencyRequestId:pr.peerRequestIdentifier,
      ]

      eventData.activeSection = [action:"Notification", note:actionParams.note]

    } else {
      String message_sender_symbol = pr.supplyingInstitutionSymbol;
      String peer_symbol = pr.requestingInstitutionSymbol;

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
        requestingAgencyRequestId:pr.id,
        supplyingAgencyRequestId:pr.peerRequestIdentifier,
      ]

      eventData.messageInfo = [reasonForMessage:"Notification", note:actionParams.note]

    }

    def send_result = protocolMessageService.sendProtocolMessage(message_sender_symbol, peer_symbol, eventData);

    if ( send_result=='SENT') {
      result = true;
    }
    else {
      log.warn("Unable to send protocol message");
    }
    return result;
  }

}
