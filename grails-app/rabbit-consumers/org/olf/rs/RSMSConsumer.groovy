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
      String symbol = body.participantInfo.recipient.institution_symbol;

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
        log.debug("Perform action ${body.participantInfo.recipient_event} for symbol ${symbol} who resides in tenant ${tenant}");
  
        Tenants.withId(tenant+'_mod_rs') { // Tenants.withId needs a schema name
          switch ( body.participantInfo.recipient_event ) {
            case 'ILL':
              log.debug("Create new patron request");
              def new_pr = new PatronRequest(title: body.request.item_id.title,
                                             patronReference: body.request.client_id?.client_identifier,
                                             isRequester:false).save(flush:true, failOnError:true);
              break;
            default:
              log.warn("Unhandled ILL event: ${body.participantInfo.recipient_event}");
              break;
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
