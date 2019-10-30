package org.olf.rs;


import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StateModel
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import java.time.LocalDateTime;

/**
 * Handle user events.
 *
 * wheras ReshareApplicationEventHandlerService is about detecting and handling system generated events - incoming protocol messages etc
 * this class is the home for user triggered activities - checking an item into reshare, marking the pull slip as printed etc.
 */
public class ReshareActionService {

  public boolean checkInToReshare(PatronRequest pr) {
    log.debug("checkInToReshare(${pr})");
    boolean result = false;
    Status s = Status.lookup('Responder', 'RES_CHECKED_IN_TO_RESHARE');
    if ( s && pr.state.value=='RES_AWAIT_PICKING') {
      pr.state = s;
      pr.save(flush:true, failOnError:true);
      result = true;
    }
    else {
      log.warn("Unable to locate RES_CHECKED_IN_TO_RESHARE OR request not currently RES_AWAIT_PICKING(${pr.state.value})");
    }

    return result;
  }

  public boolean notiftyPullSlipPrinted(PatronRequest pr) {
    return true;
  }

}
