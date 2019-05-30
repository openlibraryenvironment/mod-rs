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
   */
  static rabbitConfig = [
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
  }

}
