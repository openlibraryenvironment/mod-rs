package org.olf.rs

import grails.gorm.multitenancy.Tenants
import java.util.UUID
/**
 * Allow callers to request that a protocol message be sent to a remote (Or local) service. Callers
 * provide the requesting and responding symbol and the content of the message, this service works out
 * the most appropriate method/protocol. Initially this will always be loopback.
 *
 */
class ProtocolMessageService {
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  EventPublicationService eventPublicationService

  GlobalConfigService globalConfigService
  /**
   * @param eventData : A map structured as followed 
   *   event: {
   *     envelope:{
   *       sender:{
   *         symbol:''
   *       }
   *       recipient:{
   *         symbol:''
   *       }
   *       messageType:''
   *       messageBody:{
   *       }
   *   }
   *
   * @return a map containing properties including any confirmationId the underlying protocol implementation provides us
   *
   */
  public Map sendProtocolMessage(String peer_symbol, Map eventData) {
    def responseConfirmed = messageConfirmation(eventData, "request")
    log.debug("sendProtocolMessage called for ${peer_symbol},${eventData}");
    //Make this create a new request in the responder's system
    String confirmation = null;

    assert eventData != null
    assert eventData.messageType != null;
    assert peer_symbol != null;

    // The first thing to do is to look in the internal SharedConfig to see if the recipient is a
    // tenant in this system. If so, we can simply call handleIncomingMessage
    def tenant = globalConfigService.getTenantForSymbol(peer_symbol)
    log.debug("The tenant for that symbol is: ${tenant}")
    
    if (tenant != null) {
      // The lender we wish to ask for a copy is a tenant in the same system so set the required tenant
      // and then 
      log.debug("ProtocolMessageService::sendProtocolMessage(${peer_symbol},...) identified peer as a tenant in this system - loopback");
      eventData.tenant = tenant.toLowerCase()+'_mod_rs'
      eventData.event = mapToEvent(eventData.messageType)
      handleIncomingMessage(eventData)
    } else {
      log.error("Tenant does not exist in the system. TODO: call real messaging here")
    }
    
    return [
      confirmationId:confirmation
    ]
  }

  private String mapToEvent(String messageType) {
    String result = null;

    switch ( messageType ) {
      case 'REQUEST':
        result = 'MESSAGE_REQUEST_ind'
        break;
    }

    assert result != null;

    return result;
  }

  /**
   * @param eventData Symmetrical with the section above. See para on sendProtocolMessage - Map should have exactly the same shape
   * Normally called because a message was received on the wire HOWEVER can be called in a loopback scenario where the peer instition
   * is another tenant in the same re:share installation.
   * eventData should contain a tenantId
   * @return a confirmationId
   */
  public Map handleIncomingMessage(Map eventData) {
    // Recipient must be a tenant in the SharedConfig
    log.debug("handleIncomingMessage called. (eventData.messageType:${eventData.messageType})")
    
    // Now we issue a protcolMessageIndication event so that any handlers written for the protocol message can be 
    // called - this method should not do any work beyond understanding what event needs to be dispatched for the 
    // particular message coming in.
    if (eventData.tenant != null) {
      switch ( eventData.messageType ) {
        case 'REQUEST' :
          String topic = "${eventData.tenant}_PatronRequestEvents".toString()
          String key = UUID.randomUUID().toString();
          log.debug("publishEvent(${topic},${key},...");
          eventPublicationService.publishAsJSON(topic, key, eventData)
          break;
        default:
          log.warn("Unhandled message tyoe in eventData : ${eventData}");
          break;
      }
    }
    else {
      log.warn("NO tenant in incoming protocol message - don't know how to route it");
    }
    
    
    
    return [
      confirmationId: UUID.randomUUID().toString()
    ]
  }

  public messageConfirmation(eventData, messageType) {
    //TODO make this able to return a confirmation message if request/supplying agency message/requesting agency message are successful,
    //and returning error messages if not
  }
}
