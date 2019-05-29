package org.olf.rs

import com.budjb.rabbitmq.consumer.MessageContext;
import org.olf.rs.workflow.ReShareMessageService;
import org.olf.rs.PatronRequest;
import grails.gorm.multitenancy.Tenants;

/** ResourceSharingMessageService consumer - Will likely need to be merged with other classes in this directory
 * but kept separate for now
 * 
 * @author Ian
 *
 */
class RSMSConsumer {

  def housekeepingService

  /**
   * Consumer configuration.
   */
  static rabbitConfig = [
        "exchange": "RSExchange",
        "binding": "RSInboundMessage.#"
    ]

  /**
   * Handle an incoming RabbitMQ message.
   *
   * @param body    The converted body of the incoming message.
   * @param context Properties of the incoming message.
   *        Context has body,channel,consumerTag,envelope,properties
   * @return
   */
  def handleMessage(Map body, MessageContext context) {

    log.debug("Incoming message from ResourceSharingMessageService Body:${body} Context:${context}");

    
    if ( body ) {
      log.debug("Message is bound for ${context.envelope.routingKey}");

      // Strip off "RSInboundMessage."
      // String symbol = context.envelope.routingKey.substring(17);
      String symbol = body.header.toSymbol;

      // See if we have a tenant registered for that symbol
      String tenant = housekeepingService.findTenantForSymbol(symbol);

      log.debug("Result of resolve tenant for ${symbol} : ${tenant}");
      // We've got a tenant, so now we need to issue an event to that tennant - this might involve
      // creating a request, or updating the state of an existing request.

      if ( tenant ) {
        // Resource sharing message services arranges for us to have some extra properties in the body:
        // participantInfo:[sender:[institution_symbol:RESHARE:ACMAIN], 
        //   sender_event:ILLreq, 
        //   recipient:[institution_symbol:RESHSARE:DIKUA], 
        //   recipient_event:ILL]
  
        Tenants.withId(tenant+'_mod_rs') { // Tenants.withId needs a schema name
          try {
            // Element under message determines message type - request for a new request
            if ( body.message.request ) {
              log.debug("Create new patron request");
              def request = body.message.request;
              def new_pr = new PatronRequest(title: request.bibliographicInfo?.title,
                                             subtitle: request.bibliographicInfo?.subtitle,
                                             author: request.bibliographicInfo?.author,
                                             patronReference: request.patronInfo?.patronId,
                                             isRequester:false)
              def responder_role_id = new_pr.save(flush:true, failOnError:true);
              log.debug("Responder id will be ${responder_role_id}");
            }
            else {
              log.warn("Unhandled ILL event: ${body}");
            }
          }
          catch ( Exception e ) {
            log.error("Problem saving incoming protocol message",e);
          }
        }
      }
      else {
        log.debug("Unable to resolve tenant for symbol ${symbol}");
      }
    }
    else {
      log.debug("Body is null");
    }

    log.debug("handleMessage completed OK");
    // There is nothing to return
    return(null);
  }

  def routeToTenant(body) {
  }
}
