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

  // This map maps events to handlers - it is essentially an indirection mecahnism that will eventually allow
  // RE:Share users to add custom event handlers and override the system defaults. For now, we provide static
  // implementations of the different indications.
  private static Map<String,Closure> handlers = [
    'NewPatronRequest_ind':{ service, eventData ->
      service.handleNewPatronRequestIndication(eventData);
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
    def req = PatronRequest.get(eventData.payload.id)
    if ( ( req != null ) && ( req.state?.code == 'IDLE' ) ) {
      log.debug("Got request ${req}");
      log.debug(" -> Request is currently IDLE - transition to VALIDATED");
      req.state = Status.lookup('PatronRequest', 'VALIDATED');
      req.save(flush:true, failOnError:true)
    }
    else {
      log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
    }
  }
}
