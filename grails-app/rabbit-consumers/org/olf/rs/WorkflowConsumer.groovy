package org.olf.rs

import com.budjb.rabbitmq.consumer.MessageContext

class WorkflowConsumer {
	/**
	 * Consumer configuration.
	 */
	static rabbitConfig = [
//		connection: 'main',
//	    consumers : 1,
//    	transacted: true,
//	    retry     : true,
		queue     : "ReShare"
	]

	/**
	 * Handle an incoming RabbitMQ message.
	 *
	 * @param body    The converted body of the incoming message.
	 * @param context Properties of the incoming message.
	 * @return
	 */
	def handleMessage(def body, MessageContext context) {
		println body
		return "Have consumed it!"
	}
}
