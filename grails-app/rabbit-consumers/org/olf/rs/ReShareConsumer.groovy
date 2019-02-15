package org.olf.rs

import com.budjb.rabbitmq.consumer.MessageContext

class ReShareConsumer {
	/**
	 * Consumer configuration.
	 */
	static rabbitConfig = [
	]

	/**
	 * Handle an incoming JSON RabbitMQ message.
	 *
	 * @param body    The JSON as a map
	 * @param context Properties of the incoming message.
	 * @return
	 */
    def handleMessage(Map body, MessageContext context) {
		println "Map body: " + body.toString();
		return '{"field2":"Have swallowed it"}'
    }
	
	/**
	 * Handle an incoming RabbitMQ message.
	 *
	 * @param body    The converted body of the incoming message.
	 * @param context Properties of the incoming message.
	 * @return
	 */
	def handleMessage(def body, MessageContext context) {
		String contextBody = new String(context.body);
		println "body: " + body
		println "contextBody: " + contextBody
		return "Have consumed it!"
	}
}
