package org.olf.rs

import com.budjb.rabbitmq.consumer.MessageContext;
import org.olf.rs.workflow.ReShareMessageService;

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
      String symbol = context.envelope.routingKey.substring(17);

      // See if we have a tenant registered for that symbol
      String tenant = housekeepingService.findTenantForSymbol(symbol);


      log.debug("Result of resolve tenant for ${symbol} : ${tenant}");
    }
    else {
      log.debug("Body is null");
    }

    // There is nothing to return
    return(null);
  }

  def routeToTenant(body) {
  }
}
