package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import org.olf.rs.HostLMSLocation 
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AvailableAction
import org.olf.okapi.modules.directory.Symbol;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.k_int.okapi.OkapiClient


/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def grailsApplication
  def reshareActionService
  static boolean running = false;
  OkapiClient okapiClient

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
        PatronRequest.executeQuery('select pr.id, aa from PatronRequest as pr, AvailableAction as aa where pr.state = aa.fromState and aa.triggerType=:system',[system:'S']).each {  pr ->
          AvailableAction aa = (AvailableAction) pr[1] 
          log.debug("Apply system action ${pr[1]} to patron request ${pr[0]}");
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

        // Process any timers for sending pull slip notification emails
        // Refactor - lastExcecution now contains the next scheduled execution or 0
        Timer.executeQuery('select t from Timer as t where t.lastExecution < :now', [now:System.currentTimeMillis()]).each { timer ->
          try {
            log.debug("** Timer task ${timer.id} firing....");
            runTimer(timer);
 
            // Caclulate the next due date
            RecurrenceRule rule = new RecurrenceRule(timer.rrule);
            DateTime start = DateTime.now()
            RecurrenceRuleIterator rrule_iterator = rule.iterator(start);
            // Rule will schedule a task immediately, skip it
            rrule_iterator.nextDateTime();
            // And then work out when it would next fire
            def nextInstance = rrule_iterator.nextDateTime();
            log.debug("Calculated next event for ${timer.id}/${timer.taskCode}/${timer.rrule} as ${nextInstance}");
            log.debug(" -> as timestamp ${nextInstance.getTimestamp()} == due in ${nextInstance.getTimestamp()-System.currentTimeMillis()}");
            timer.lastExecution = nextInstance.getTimestamp();
            timer.save(flush:true, failOnError:true)
          }
          catch ( Exception e ) {
            log.error("Unexpected error processing timer tasks ${e.message} - rule is ${timer.rrule}");
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

  private runTimer(Timer t) {
  }

  private void checkPullSlips() {
    log.debug("checkPullSlips()");
    // HostLMSLocation.list().each { loc ->
    //   log.debug("Check pull slips fro ${loc}");
    //   checkPullSlipsFor(loc.code);
    // }

    log.debug("Checking if okapi context provides us with configuration");

    // if (okapiClient?.withTenant().providesInterface("configuration", "^2.0")) {
    //   log.debug(" -> okapi exposing configuration ^2.0 to us - we can get the email config");
      // Needs to be blocking...
      // List links = okapiClient.getSync("/erm/sas/linkedLicenses", [
      //   filters: [
      //     "remoteId==${license.id}"
      //   ]
      // ])
    // }
    // else {
    //   log.debug(" -> okapi not exposing configuration ^2.0 to us");
    // }
    
  }

  private void checkPullSlipsFor(String location) {
    log.debug("checkPullSlipsFor(${location})");
  }
}
