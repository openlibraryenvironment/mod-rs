package org.olf.rs

import com.budjb.rabbitmq.consumer.MessageContext;
import org.olf.rs.workflow.ReShareMessageService;

/** This is the class that deals the generic reshare actions, nothing protocol specific, the actions may trigger a protocol action to be performed
 * 
 * @author Chas
 *
 */
class ReShareConsumer {
	/**
	 * Consumer configuration.
	 */
	static rabbitConfig = [
	]

	/** The service that is goinf to process the incoming message */
	ReShareMessageService reShareMessageService;

	/**
	 * Handle an incoming JSON RabbitMQ message.
	 *
	 * @param body    The JSON as a map
	 * @param context Properties of the incoming message.
	 * @return
	 */
    def handleMessage(Map body, MessageContext context) {
		// Just hand it off to the service
		reShareMessageService.processAnIncomingMessage(body);

		// There is nothing to return
		return(null);
    }
	
	/**
	 * Handle an incoming RabbitMQ message.
	 *
	 * @param body    The converted body of the incoming message.
	 * @param context Properties of the incoming message.
	 * @return
	 */
	def handleMessage(def body, MessageContext context) {
		log.error("Received a message in the generic handle message for the ReShareConsumer, this should not happen: " + ((body == null) ? "Null Body" : body.toString()));

		// There is nothing to return
		return(null);
	}
}
