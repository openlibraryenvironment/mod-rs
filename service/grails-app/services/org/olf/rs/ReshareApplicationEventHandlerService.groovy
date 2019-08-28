package org.olf.rs;

import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.Status

/**
 * Handle application events.
 *
 * This class is all about high level reshare events - the kind of things users want to customise and modify. Application functionality
 * that might change between implementations can be added here.
 * REMEMBER: Notifications are not automatically within a tenant scope - handlers will need to resolve that.
 */
public class ReshareApplicationEventHandlerService {

  private static final int MAX_RETRIES = 10;

  // This map maps events to handlers - it is essentially an indirection mecahnism that will eventually allow
  // RE:Share users to add custom event handlers and override the system defaults. For now, we provide static
  // implementations of the different indications.
  private static Map<String,Closure> handlers = [
    'NewPatronRequest_ind':{ service, eventData ->
      service.handleNewPatronRequestIndication(eventData);
    },
    'STATUS_VALIDATED_ind': { service, eventData ->
      service.log.debug("handle VALIDATED state change");
    },
    'STATUS_SOURCING_ITEM_ind': { service, eventData ->
      service.log.debug("handle SOURCING_ITEM state change");
    }
    
  ]

  @Subscriber('PREventIndication')
  public handleApplicationEvent(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleApplicationEvent(${eventData})");
    Closure c = handlers[eventData.event]
    if ( c != null ) {
      // System has a closure registered for event, call it
      if ( eventData.tenant ) {
        Tenants.withId(eventData.tenant) {
          c.call(this, eventData);
        }
      }
    }
  }

  // Requests are created with a STATE of IDLE, this handler validates the request and sets the state to VALIDATED, or ERROR
  public void handleNewPatronRequestIndication(eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleNewPatronRequestIndication(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      if ( ( req != null ) && ( req.state?.code == 'IDLE' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently IDLE - transition to VALIDATED");
        req.state = Status.lookup('PatronRequest', 'VALIDATED');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
  }

  /**
   * Sometimes, we might receive a notification before the source transaction has committed. THats rubbish - so here we retry
   * up to 5 times.
   */
  public PatronRequest delayedGet(String pr_id) {
    log.debug("PatronRequest called")
    PatronRequest result = null;
    int retries = 0;

    try {
      while ( ( result == null ) && (retries < MAX_RETRIES) ) {
        result = PatronRequest.get(pr_id)
        if ( result == null ) {
          log.debug("Waiting to see if request has become available: Try ${retries}")
          //Thread.sleep(2000);
          Thread.sleep(900);
          retries++;
        } else {
          log.debug("Result found")
        }
      }
    }
    catch(Exception e){
      log.error("Problem", e)
    }
    return result;
  }
}
