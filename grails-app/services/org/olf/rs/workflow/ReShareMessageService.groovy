package org.olf.rs.workflow

import grails.gorm.multitenancy.Tenants;
import grails.util.Holders;
import groovy.util.logging.Slf4j;
import org.olf.rs.PatronRequest;
import org.olf.rs.RabbitService;
import org.olf.rs.rabbit.Queue;

// For @Subscriber
import grails.events.annotation.*

// PreInsertEvent etc
import org.grails.datastore.mapping.engine.event.*;


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
  private Map<String, AbstractAction> actionCache = [ : ];

  /** The service that we use to put the message on the queue - Injected by framework */
  private RabbitService rabbitService;

  /** Registers an action with the implementing class
   * 
   * @param actionCode The code that represents the action to be performed
   * @param className The name of the class that implements the action, we automatically append "Service" to the name of the class
   */
  public void registerClass(String actionCode, String className) {
    // Nothing to do if we have not been passed a code and class name
    if (className && actionCode) {
      // Have we already registered this action
      AbstractAction actionClass = actionCache[actionCode];
      
      // If we havn't, we need to instantiate an instance for this action
      if (actionClass == null) {
        // We will prefix the classname with action and postfix it with Service
        String realClassName = "action" + className + "Service";

        try {
          // Now setup the link between the action and the class that will do the work
          actionClass = Holders.grailsApplication.mainContext.getBean(realClassName);
          actionCache[actionCode] = actionClass;
        } catch (Exception e) {
          log.error("Unable to locate action bean: " + realClassName);
        }
      }
    }
  }

  /**
   * Determines the current tenant id
   * 
   * @return The tenant id there is one otherwise null
   */
  static public String getTenantId() {
    String tenantId = null;
    try {
      tenantId = Tenants.currentId();
    } catch (Exception e) {
    }
    return(tenantId);
  }

  /**
   * This is called after the request has been saved to see if it needs adding to the queue
   * 
   * @param patronRequest
   */
  public void checkAddToQueue(PatronRequest patronRequest) {
    // must not be waiting for a protocol action to happen
    if (!patronRequest.awaitingProtocolResponse) {
      // Must have a pending action
      if (patronRequest.pendingAction) {
        // We need the tenant id in order to add it to the queue
        String tenantId = getTenantId();
    
        // If we do not have a tenant id, then we cannot queue it
        if (tenantId) {
          // We can add it to the queue
          queue(tenantId, patronRequest.pendingAction.id, patronRequest.id);
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
            // If the pending action is not the same as in the message then we need to abandon
            if (patronRequest.pendingAction && actionCode.equals(patronRequest.pendingAction.id)) {
              // Ensure the action cache is populated
              // TODO: I believe this is redundant and is just occurring because of how we are testing, needs confirming
              if (!actionCache) {
                Action.CreateDefault();
              }

              // Get hold of the class that is going to deal with this action
              AbstractAction actionClass = actionCache[actionCode];
              if (actionClass) {
                // Perform the action against this request
                actionClass.execute(patronRequest);
              } else {
                log.error("No class defined for action: " + actionCode + ", unable to process request id" + requestId);
                // TODO: Do we add an audit record against the request ?
              }
            } else {
              log.error("Message action does not match that on the request for request " + requestId + "for tenant " + tenantId + ", message action " + actionCode + ", pending action " + (patronRequest.pendingAction ? patronRequest.pendingAction.id : "<null>"));
            }
          } else {
            log.error("Failed to find patron request " + requestId + "for tenant " + tenantId);
          }
        }
      } catch (Exception e) {
        log.error("Exception thrown while trying to process message for Tenanr: " + tenantId + " for request " + requestId + " and action " + actionCode, e);
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

  @Subscriber 
  void afterInsert(PostInsertEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      // Stuff to do after insert of a patron request which need access
      // to the spring boot infrastructure
      log.debug("afterInsert PatronRequest id: ${event.entityObject.id}");
      PatronRequest pr = (PatronRequest) event.entityObject;
      checkAddToQueue(pr);
    }
  }

  @Subscriber 
  void afterUpdate(PostUpdateEvent event) { 
    if ( event.entityObject instanceof PatronRequest ) {
      // Stuff to do after update of a patron request which need access
      // to the spring boot infrastructure
      log.debug("afterUpdate PatronRequest id: ${event.entityObject.id}");
      PatronRequest pr = (PatronRequest) event.entityObject;
      checkAddToQueue(pr);
    }
  }

  @Subscriber
  void beforeInsert(PreInsertEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      // Stuff to do before insert of a patron request which need access
      // to the spring boot infrastructure
      log.debug("beforeInsert of PatronRequest");
    }
  }
}
