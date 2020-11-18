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
import groovy.json.JsonSlurper
import org.olf.rs.EmailService
import java.util.UUID;


/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def grailsApplication
  def reshareActionService
  static boolean running = false;
  OkapiClient okapiClient
  EmailService emailService

  private static config_test_count = 0;

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
        log.debug("Checking timers ready for execution");

    
        // Dump all timers whilst we look into timer execution
        Timer.list().each { ti ->
          def remaining_min = ((ti.lastExecution?:0)-System.currentTimeMillis())/60000
          log.debug("Declared timer: ${ti.id}, ${ti.lastExecution}, ${ti.enabled}, ${ti.rrule}, ${ti.taskConfig} remaining=${remaining_min}");
        }

        Timer.executeQuery('select t from Timer as t where ( ( t.lastExecution is null ) OR ( t.lastExecution < :now ) ) and t.enabled=:en', 
                           [now:System.currentTimeMillis(), en: true]).each { timer ->
          try {
            log.debug("** Timer task ${timer.id} firing....");
            runTimer(timer);

            String rule_to_parse = timer.rrule.startsWith('RRULE:') ? timer.rrule.substring(6) : timer.rrule;

            // Caclulate the next due date
            RecurrenceRule rule = new RecurrenceRule(rule_to_parse);
            DateTime start = DateTime.now()
            RecurrenceRuleIterator rrule_iterator = rule.iterator(start);
            def nextInstance = rrule_iterator.nextDateTime();
            log.debug("Calculated next event for ${timer.id}/${timer.taskCode}/${timer.rrule} as ${nextInstance}");
            log.debug(" -> as timestamp ${nextInstance.getTimestamp()} == due in ${nextInstance.getTimestamp()-System.currentTimeMillis()}");
            timer.lastExecution = nextInstance.getTimestamp();
            timer.save(flush:true, failOnError:true)
          }
          catch ( Exception e ) {
            log.error("Unexpected error processing timer tasks ${e.message} - rule is \"${timer.rrule}\"");
          }
          finally {
            log.debug("Completed scheduled task checking");
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
    try {
      switch ( t.taskCode ) {
        case 'PrintPullSlips':
          log.debug("Fire pull slip timer task. Config is ${t.taskConfig} enabled=${t.enabled}")
          JsonSlurper jsonSlurper = new JsonSlurper()
          Map task_config = jsonSlurper.parseText(t.taskConfig)
          checkPullSlips(task_config)
          break;
        default:
          log.debug("Unhandled timer task code ${t.taskCode}");
          break;
      }
    }
    catch ( Exception e ) {
      log.error("ERROR running timer",e)
    }
  }

  // Use mod-configuration to retrieve the approproate setting
  private String getSetting(String setting) {
    String result = null;
    try {
      def setting_result = okapiClient.getSync("/configurations/entries", [query:'code='+setting])
      log.debug("Got setting result ${setting_result}");
    }   
    catch ( Exception e ) {
      e.printStackTrace()
    }

    return result;
  }

  private void checkPullSlips(Map timer_cfg) {
    log.debug("checkPullSlips(${timer_cfg})");
    timer_cfg.locations.each { loc ->
      log.debug("Check pull slips for ${loc}");
      checkPullSlipsFor(loc);
    }
  }

  private void checkPullSlipsFor(String location) {
    log.debug("checkPullSlipsFor(${location})");
    try {
      // 'from':'admin@reshare.org',
      Map email_params = [
            'notificationId':UUID.randomUUID().toString(),
                        'to':'ianibbo@gmail.com',
                    'header':'Test email from reshare',
                      'body':'''Some test'''
      ]

      Map email_result = emailService.sendEmail(email_params);
    }
    catch ( Exception e ) {
      e.printStackTrace();
    }
  }
}
