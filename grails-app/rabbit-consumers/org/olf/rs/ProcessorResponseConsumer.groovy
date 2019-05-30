package org.olf.rs

import com.budjb.rabbitmq.consumer.MessageContext;
import org.olf.rs.workflow.ReShareMessageService;
import org.olf.rs.PatronRequest;
import grails.gorm.multitenancy.Tenants;

/** 
 * ProcessorResponseConsumer - Listen out for ProcessorResponse notifications
 * 
 * @author Ian
 *
 */
class ProcessorResponseConsumer {

  def housekeepingService

  /**
   * Consumer configuration.
   *
   * Defining the consumer in application.yml as below did not seem to work. definint it in the static rabbitConfig did, so thats what we do
   * Here is the config that did not work, as a signpost for others
   *
   * # ProcessorResponseConsumer configuration moved to the consumer itself. Defining the exchange/topic/queue binding
   * # in this way did not work as expected. Does work as expected when specified in grails-app/rabbit-consumers/ProcessorResponseConsumer
   * # Leaving this here as a signpost for why we did it this way.
   * # ProcessorResponseConsumer:
   * #   exchange: RSExchange
   * #   binding: ProcessorResponse.#
   * #   queue: ProcessorResponse
   * #   match: any
   *
   */
  static rabbitConfig = [
    "exchange": "RSExchange",
    "binding": "ProcessorResponse.#"
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
    log.debug("**ProcessorResponse** ${body} Context:${context}");

    // This is an indication that a protocol message was either sent or not, we need to move the patron request status
    // along accordingly.
  }

}
