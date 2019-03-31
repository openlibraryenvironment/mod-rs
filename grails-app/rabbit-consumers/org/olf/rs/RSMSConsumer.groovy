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
   * @return
   */
  def handleMessage(def body, MessageContext context) {
    log.debug("Incoming message from ResourceSharingMessageService ${body} ${context}");



    // There is nothing to return
    return(null);
  }
}
