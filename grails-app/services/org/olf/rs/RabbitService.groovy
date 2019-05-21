package org.olf.rs

import java.time.OffsetDateTime;
import com.budjb.rabbitmq.RabbitContext;
import com.budjb.rabbitmq.RunningState;
import com.budjb.rabbitmq.publisher.RabbitMessagePublisher;

public class RabbitService {

	RabbitMessagePublisher rabbitMessagePublisher;
	RabbitContext rabbitContext;
	boolean rabbitInitialised = false;
	
	public boolean Send(String queue, String messageId, Object message, String responseQueue = null) {
		boolean successful = true;
		if (Running()) {
			try {
				// Note: use rpc if you want to wait for a response
				rabbitMessagePublisher.send {
					routingKey = queue
					replyTo = responseQueue
					correlationId = messageId
					deliveryMode = 2 // persistent
					messageId = messageId
					timestamp = OffsetDateTime.now()
					body = message;

				// Note: Other properties of use that we might use are type - not sure if that does anyhting significant or is just an app definition of the body
				}
			} catch (Exception e) {
				log.error("Exception thrown while puting a message on the ReShare rabbit queue", e);
				successful = false;
			}
		} else {
				successful = false;
		}
		return(successful);
	}

  public boolean sendToExchange(String ex, String rk, String messageId, Object message, String responseQueue = null) {
    log.debug("RabbitService::sendToExchange(${ex}, ${rk}, ${messageId}, ...)");
    boolean successful = true;
    if (Running()) {
      try {
        // Note: use rpc if you want to wait for a response
        rabbitMessagePublisher.send {
          exchange = ex
          routingKey = rk
          deliveryMode = 2 // persistent
          messageId = messageId
          body = message;
        }
        log.debug("rabbitMessagePublisher.send completed");
      } catch (Exception e) {
        log.error("Exception thrown while puting a message on the ReShare rabbit queue", e);
        successful = false;
      }
    } else {
        successful = false;
    }

    log.debug("RabbitService::Send returns ${successful}");
    return(successful);
  }


	public boolean Running(boolean startIfNot = true) {
		boolean rabbitRunning = true;
		if (!rabbitInitialised || (rabbitContext.getRunningState() != RunningState.RUNNING)) {
			if (startIfNot) {
				try {
					// Load the configuration and attempt to start
					rabbitContext.load();
					rabbitContext.start();
					rabbitInitialised = true;
				} catch (Exception e) {
					log.error("Failed to start rabbit: ", e);
					rabbitRunning = false;
				}
			} else {
				rabbitRunning = false;
			}
		}
		return(rabbitRunning);
	}
}
