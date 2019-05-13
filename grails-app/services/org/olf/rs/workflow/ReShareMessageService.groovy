package org.olf.rs.workflow

import grails.gorm.multitenancy.Tenants;
import grails.util.Holders;
import groovy.util.logging.Slf4j;
import org.olf.rs.PatronRequest;
import org.olf.rs.RabbitService;
import org.olf.rs.rabbit.Queue;

// PreInsertEvent etc
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.SaveOrUpdateEvent

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent

import javax.annotation.PostConstruct;
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener
import org.springframework.context.ApplicationEvent

import org.grails.orm.hibernate.AbstractHibernateDatastore

/**
 *  This service adds messages to the reshare and deals with them appropriately when one has been received
 * 
 * @author Chas
 *
 */
@Slf4j 
class ReShareMessageService implements ApplicationListener {

  /** The properties we put on the message queue to identify the request that needs to be processed */
  static private final String ACTION_CODE = "action";
  static private final String REQUEST_ID  = "requestId";
  static private final String TENANT_ID   = "tenantId";

  /** The cache of action codes against the class that performs that action */
  private Map<String, AbstractAction> actionCache = [ : ];

  /** The service that we use to put the message on the queue - Injected by framework */
  RabbitService rabbitService;

  @PostConstruct
  public void init() {
    log.debug("ReShareMessageService::init()");
  }

  /** Registers an action with the implementing class
   * 
   * @param actionCode The code that represents the action to be performed
   * @param className The name of the class that implements the action, we automatically append "Service" to the name of the class
   */
  public void registerClass(String actionCode, String className) {

    log.debug("ReShareMessageService::registerClass(${actionCode},${className}");

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
    static public String getTenantId(AbstractPersistenceEvent event = null) {
    String tenantId = null;
    try {
      if (event == null) {
        tenantId = Tenants.currentId();
      } else {
        tenantId = Tenants.currentId(event.source);
      }
    } catch (Exception e) {
    }
    return(tenantId);
  }

  /**
   * This is called after the request has been saved to see if it needs adding to the queue
   * 
   * @param patronRequest
   */
  public void checkAddToQueue(PatronRequest patronRequest, AbstractPersistenceEvent event = null) {

    log.debug("checkAddToQueue(...) ${patronRequest.id}");

    // must not be waiting for a protocol action to happen
    if (!patronRequest.awaitingProtocolResponse) {
      // Must have a pending action
      if (patronRequest.pendingAction) {
        // We need the tenant id in order to add it to the queue
        String tenantId = getTenantId(event);
    
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

    log.debug("ReShareMessageService::processAnIncomingMessage(${messageDetails})");

    String actionCode = messageDetails[ACTION_CODE];
    String requestId = messageDetails[REQUEST_ID];
    String tenantId = messageDetails[TENANT_ID];

    if (actionCode && requestId && tenantId) {
      try {
        Tenants.withId(tenantId) {
          // Fetch the patron request we need to process
          int retries = 0;

          PatronRequest patronRequest = PatronRequest.get(requestId);
          while ( ( patronRequest == null ) && ( retries++ < 5 ) )  {
            Thread.sleep(1000);
            log.debug("Retry find request ${requestId}");
            patronRequest = PatronRequest.get(requestId);
          }

          if (patronRequest) {
            log.debug("Got request ${requestId}");

            // If the pending action is not the same as in the message then we need to abandon
            if (patronRequest.pendingAction && actionCode.equals(patronRequest.pendingAction.id)) {
              // Ensure the action cache is populated
              // TODO: I believe this is redundant and is just occurring because of how we are testing, needs confirming
              if (!actionCache) {
                Action.CreateDefault(this);
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
      log.error("The ReShare queue message details does not contain the expected details (action, request id and tenant id), contents: " + ((messageDetails == null) ? "<null>" : messageDetails.toString()));
    }
  }

  /** Adds a request to the queue
   * 
   * @param tenantId The tenant the request belongs to
   * @param actionCode The action that is to be performed
   * @param requestId The request id that the action is to be performed upon
   */
  public void queue(String tenantId, String actionCode, String requestId) {
    log.debug("queue(tenant:${tenantId}, action:${actionCode}, requestId:${requestId})");
    Map<String, String> messageBody = [ : ];
    messageBody[ACTION_CODE] = actionCode;
    messageBody[REQUEST_ID] = requestId;
    messageBody[TENANT_ID] = tenantId;
    rabbitService.Send(Queue.RESHARE_ACTION, requestId + "_" + System.currentTimeMillis(), messageBody);
  }

  void afterInsert(PostInsertEvent event) {
    // log.debug("afterInsert ${event} ${event?.entityObject?.class?.name}");
    if ( event.entityObject instanceof PatronRequest ) {
      // Stuff to do after insert of a patron request which need access
      // to the spring boot infrastructure
      // log.debug("afterInsert PatronRequest id: ${event.entityObject.id}");
      PatronRequest pr = (PatronRequest) event.entityObject;
      checkAddToQueue(pr, event);
    }
  }

  void afterUpdate(PostUpdateEvent event) { 
    // log.debug("afterUpdate ${event} ${event?.entityObject?.class?.name}");
    if ( event.entityObject instanceof PatronRequest ) {
      // Stuff to do after update of a patron request which need access
      // to the spring boot infrastructure
      // log.debug("afterUpdate PatronRequest id: ${event.entityObject.id}");
      PatronRequest pr = (PatronRequest) event.entityObject;
      checkAddToQueue(pr, event);
    }
  }

  void beforeInsert(PreInsertEvent event) {
    // log.debug("beforeInsert ${event} ${event?.entityObject?.class?.name}");
    if ( event.entityObject instanceof PatronRequest ) {
      // Stuff to do before insert of a patron request which need access
      // to the spring boot infrastructure
      // log.debug("beforeInsert of PatronRequest");
    }
  }

  void onSaveOrUpdate(SaveOrUpdateEvent event) {
    // log.debug("onSaveOrUpdate ${event} ${event?.entityObject?.class?.name}");
    if ( event.entityObject instanceof PatronRequest ) {
      AbstractHibernateDatastore ds = (AbstractHibernateDatastore) event.source
      PatronRequest pr = (PatronRequest) event.entityObject;
      if ( event?.entityObject?.id ) {
        log.debug("onSaveOrUpdate of PatronRequest :: ${event?.entityObject?.id}");
        checkAddToQueue(pr, event);
      }
    }
  }

  public void onApplicationEvent(org.springframework.context.ApplicationEvent event){
    // log.debug("--> ${event?.class.name} ${event}");
    if ( event instanceof AbstractPersistenceEvent ) {
      if ( event instanceof PostUpdateEvent ) {
        afterUpdate(event);
      }
      else if ( event instanceof PreInsertEvent ) {
        beforeInsert(event);
      }
      else if ( event instanceof PostInsertEvent ) {
        afterInsert(event);
      }
      else if ( event instanceof SaveOrUpdateEvent ) {
        // On save the record will not have an ID, but it appears that a subsequent event gets triggered
        // once the id has been allocated
        onSaveOrUpdate(event);
      }
      else {
        // log.debug("No special handling for appliaction event of class ${event}");
      }
    }
    else {
      // log.debug("Event is not a persistence event: ${event}");
    }
  }
}
