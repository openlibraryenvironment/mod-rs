package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import org.olf.rs.HostLMSLocation 
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AvailableAction

import org.olf.okapi.modules.directory.Symbol;

/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def grailsApplication
  def reshareActionService
  static boolean running = false;

  def performReshareTasks(String tenant) {
    log.debug("performReshareTasks(${tenant})");


    // If somehow we get asked to perform the background tasks, but a thread is already running, then just return
    synchronized ( this ) {
      if ( running == true ) {
        log.debug("BackgroundTaskService::performReshareTasks already running - return");
        return;
      }
      else {
        running = true;
      }
    }

    try {
      Tenants.withId(tenant) {
        // We aren't doing this any longer / at the moment.
        checkPullSlips();
  
        // Warn of any duplicate symbols
        def duplicate_symbols = Symbol.executeQuery('select distinct s.symbol, s.authority.symbol from Symbol as s group by s.symbol, s.authority.symbol having count(*) > 1')
        duplicate_symbols.each { ds ->
          log.warn("WARNING: Duplicate symbols detected. This means the symbol ${ds} appears more than once. This shoud not happen. Incoming requests for this symbol cannot be uniquely matched to an institution");
        }
  
        // Find all patron requesrs where the current state has a System action attached that can be executed.
        PatronRequest.executeQuery('select pr.id, aa from PatronRequest as pr, AvailableAction as aa where pr.state = aa.fromState and aa.triggerType=:system',[system:'S']).each { 
          AvailableAction aa = (AvailableAction) it[1] 
          log.debug("Apply system action ${it[1]} to patron request ${it[0]}");
          switch ( aa.actionType ) {
            case 'S':
              log.debug("service action");
              break;
            case 'C':
              log.debug("closure action");
              break;
            default:
              log.debug("No action type for action ${aa}");
              break;
          }
          
        }
      }
    }
    catch ( Exception e ) {
      log.error("Exception running background tasks",e);
    }
    finally {
      running = false;
      log.debug("BackgroundTaskService::performReshareTasks exiting");
    }
  }

  private void checkPullSlips() {
    log.debug("checkPullSlips()");
    // HostLMSLocation.list().each { loc ->
    //   log.debug("Check pull slips fro ${loc}");
    //   checkPullSlipsFor(loc.code);
    // }
  }

  private void checkPullSlipsFor(String location) {
    log.debug("checkPullSlipsFor(${location})");
  }
}
