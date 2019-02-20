package org.olf.rs.workflow

import grails.gorm.multitenancy.Tenants;
import grails.util.Holders;
import groovy.util.logging.Slf4j;
import org.olf.rs.PatronRequest;
import org.olf.rs.RabbitService;
import org.olf.rs.rabbit.Queue;

/**
 *  This service adds messages to the reshare and deals with them appropriately when one has been received
 * 
 * @author Chas
 *
 */
@Slf4j 
class ReShareMessageService {

	/** The properties we put on the message queue to identify the request that needs to be processed */
	static private final String ACTION_CODE = "action";
	static private final String REQUEST_ID  = "requestId";
	static private final String TENANT_ID   = "tenantId";
	
	/** The cache of action codes against the class that performs that action */
	private static Map<String, AbstractAction> actionCache = [ : ];

	/** The service that we use to put the message on the queue */
	private RabbitService rabbitService;
	
	/** Registers an action with the implementing class
	 * 
	 * @param actionCode The code that represents the action to be performed
	 * @param className The name of the class that implements the action, we automatically append "Service" to the name of the class
	 */
	static public void registerClass(String actionCode, String className) {
		// Nothing to do if we have not been passed a code and class name
		if (className && actionCode) {
			// Have we already registered this action
			AbstractAction actionClass = actionCache[actionCode];
			
			// If we havn't, we need to instantiate an instance for this action
			if (actionClass == null) {
				// Now setup the link between the action and the class that will do the work
				try {
					actionClass = Holders.grailsApplication.mainContext.getBean(className + "Service");
					actionCache[actionCode] = actionClass;
				} catch (Exception e) {
					log.error("Unable to locate action bean: " + className);
				}
			}
		}
	}

	/**
	 *  Performs the designated action
	 * 
	 * @param messageDetails Defines the details that we need to act upon
	 */
	public void processAnIncomingMessage(Map messageDetails) {

		String actionCode = messageDetails[ACTION_CODE];
		String requestId = messageDetails[REQUEST_ID];
		String tenantId = messageDetails[TENANT_ID];
		if (actionCode && requestId && tenantId) {
			try {
				Tenants.withId(tenantId) {
					// Fetch the patron request we need to process
					PatronRequest patronRequest = PatronRequest.get(requestId);
					if (patronRequest) {
						// Get hold of the class that is going to deal with this action
						AbstractAction actionClass = actionCache[actionCode];
						if (actionClass) {
							// Perform the action against this request
							actionClass.execute(patronRequest);
						} else {
							log.errorEnabled("No class defined for action: " + actionCode + ", unable to process request id" + requestId);
							// TODO: Do we add an audit record against the request ?
						}
					} else {
						log.error("Failed to find patron request " + requestId + "for tenant " + tenantId);
					}
				}
			} catch (Exception e) {
				log.errorEnabled("Exception thrown while trying to process message for Tenanr: " + tenantId + " for request " + requestId + " and action " + actionCode, e);
			}
		} else {
			log.error("The ReShard queue message details does not contain the expected details (action, request id and tenant id), contents: " + ((messageDetails == null) ? "<null>" : messageDetails.toString()));
		}

	}

	/** Adds a request to the queue
	 * 
	 * @param tenantId The tenant the request belongs to
	 * @param actionCode The action that is to be performed
	 * @param requestId The request id that the action is to be performed upon
	 */
	public void queue(String tenantId, String actionCode, String requestId) {
		Map<String, String> messageBody = [ : ];
		messageBody[ACTION_CODE] = actionCode;
		messageBody[REQUEST_ID] = requestId;
		messageBody[TENANT_ID] = tenantId;
		rabbitService.Send(Queue.RESHARE_ACTION, requestId + "_" + System.currentTimeMillis(), messageBody);
	}
}
