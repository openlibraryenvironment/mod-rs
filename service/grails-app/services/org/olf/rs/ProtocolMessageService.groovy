package org.olf.rs

import grails.gorm.multitenancy.Tenants
import java.util.UUID
import org.olf.okapi.modules.directory.ServiceAccount
import groovy.xml.StreamingMarkupBuilder
import java.text.SimpleDateFormat
import static groovyx.net.http.HttpBuilder.configure
import static groovyx.net.http.ContentTypes.XML
import groovyx.net.http.*


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
          recipient:{
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
  public Map sendProtocolMessage(String message_sender_symbol, String peer_symbol, Map eventData) {

    Map result = [:]

    def responseConfirmed = messageConfirmation(eventData, "request")
    log.debug("sendProtocolMessage called for ${message_sender_symbol}, ${peer_symbol},${eventData}");
    //Make this create a new request in the responder's system
    String confirmation = null;

    assert eventData != null
    assert eventData.messageType != null;
    assert peer_symbol != null;

    // The first thing to do is to look in the internal SharedConfig to see if the recipient is a
    // tenant in this system. If so, we can simply call handleIncomingMessage
    def tenant = globalConfigService.getTenantForSymbol(peer_symbol)
    log.debug("The tenant for that symbol(${peer_symbol}) is: ${tenant}")

    def ill_services_for_peer = findIllServices(peer_symbol)
    log.debug("ILL Services for peer: ${ill_services_for_peer}")

    /* if (tenant != null) {
      // The lender we wish to ask for a copy is a tenant in the same system so set the required tenant
      // and then 
      log.debug("ProtocolMessageService::sendProtocolMessage(${message_sender_symbol},${peer_symbol},...) identified peer as a tenant in this system - loopback");
      eventData.tenant = tenant.toLowerCase()+'_mod_rs'
      eventData.sender = message_sender_symbol
      eventData.recipient = peer_symbol
      eventData.event = mapToEvent(eventData.messageType)
      log.debug("Direct call ${tenant} as loopback for ${eventData}");
      handleIncomingMessage(eventData)
      result.status='SENT'
    } else {
      log.error("Tenant ${peer_symbol} does not exist in the system. TODO: call real messaging here")
      // If the symbol exists in the directory and we have a protocol address, send a message,
      // otherwise, mark as failed and skip to the next rota entry.
      // update the request status - set the 
      result.status='ERROR'
    } */
    log.debug("Will send an ISO18626 message to ILL service")

    log.debug("====================================================================")
    log.debug("Event Data: ${eventData}")
    log.debug("Service: ${ill_services_for_peer.service.address}")

    Map req_data = [service:ill_services_for_peer.service.address,
                supplier: eventData.bibliographicInfo.supplyingInstitutionSymbol,
                requester: eventData.bibliographicInfo.requestingInstitutionSymbol,
                title: eventData.bibliographicInfo.title,
                requestingAgencyRequestId: eventData.header.requestingAgencyRequestId]

    log.debug("Req Data: ${req_data}")
    log.debug("Will now send ISO18626 request")
    try {
      sendISO18626Request(req_data)
      log.debug("ISO18626 request sent")
    } catch(Exception e) {
      log.debug("ISO18626 request failed to send.")
      log.error("ISO18626 stacktrace: ${e}")
    }
    log.debug("====================================================================")
    
    return result;
  }

  private String mapToEvent(String messageType) {
    String result = null;

    switch ( messageType ) {
      case 'REQUEST':
        result = 'MESSAGE_REQUEST_ind'
        break;
      case 'SUPPLYING_AGENCY_MESSAGE':
        result = 'SUPPLYING_AGENCY_MESSAGE_ind'
        break;
      default:
        log.error("Unhandled event type on incoming protocol message: ${messageType}");
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
        case 'SUPPLYING_AGENCY_MESSAGE':
          String topic = "${eventData.tenant}_PatronRequestEvents".toString()
          String key = UUID.randomUUID().toString();
          log.debug("publishEvent(${topic},${key},...");
          eventPublicationService.publishAsJSON(topic, key, eventData)
          break;
        default:
          log.warn("Unhandled message type in eventData : ${eventData}");
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

  /**
   * Return a prioroty order list of service accounts this symbol can accept
   */
  def findIllServices(String symbol) {
    String[] symbol_components = symbol.split(':');

    log.debug("symbol: ${symbol}, symbol components: ${symbol_components}");
    List<ServiceAccount> result = ServiceAccount.executeQuery('''select sa from ServiceAccount as sa
join sa.accountHolder.symbols as symbol
where symbol.symbol=:sym 
and symbol.authority.symbol=:auth
and sa.service.businessFunction.value=:ill
''', [ ill:'ill', sym:symbol_components[1], auth:symbol_components[0] ] ); 

    log.debug("Got service accounts: ${result}");

    return result;
  }

  def makeISO18626Request(Map args) {
    log.debug("Creating ISO18626 Message")
    String[] sup_info = args.supplier.split(':');
    String[] req_info = args.requester.split(':');
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    return{
      ISO18626Message( 'ill:version':'1.0',
                       'xmlns':'http://illtransactions.org/2013/iso18626',
                       'xmlns:ill': 'http://illtransactions.org/2013/iso18626',
                       'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                       'xsi:schemaLocation': 'http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_1.xsd' ) {
        request {
          header {
            supplyingAgencyId {
              agencyIdType(sup_info[0])
              agencyIdValue(sup_info[1])
            }
            requestingAgencyId {
              agencyIdType(req_info[0])
              agencyIdValue(req_info[1])
            }
            timestamp(dateFormatter.format(new Date())) // Current time
            requestingAgencyRequestId(args.requestingAgencyRequestId) 
          }
          bibliographicInfo {
            //supplierUniqueRecordId('1234')
            title(args.title)
          }
          serviceInfo {
            serviceType('Loan')
            serviceLevel('Loan')
            //needBeforeDate('2014-05-01T00:00:00.0Z')
            anyEdition('Y')
          }
        }
      }
    }
  }

  def sendISO18626Request(Map args) {
    log.debug("Sending ISO18626 Message")
    StringWriter sw = new StringWriter();
    sw << new StreamingMarkupBuilder().bind(makeISO18626Request(args))
    String message = sw.toString();
    log.debug("ISO18626 Message: ${message}")
    def iso18626_response = configure {
      request.uri = args.service
      request.contentType = XML[0]
    }.post {
      request.body = message
    }
  }
}
