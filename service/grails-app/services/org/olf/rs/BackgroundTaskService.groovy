package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import org.olf.rs.HostLMSLocation 
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AvailableAction
import org.olf.okapi.modules.directory.Symbol;
import org.olf.okapi.modules.directory.DirectoryEntry;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.k_int.okapi.OkapiClient
import groovy.json.JsonSlurper
import org.olf.rs.EmailService
import java.util.UUID;
import groovy.text.GStringTemplateEngine;


/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def grailsApplication
  def reshareActionService
  def groovyPageRenderer

  static boolean running = false;
  OkapiClient okapiClient
  EmailService emailService

  private static config_test_count = 0;
  private static String PULL_SLIP_QUERY='''
Select pr 
from PatronRequest as pr
where ( pr.pickShelvingLocation like :loccode or pr.pickLocation.code like :loccode )
and pr.state.code='RES_NEW_AWAIT_PULL_SLIP'
'''

  private static String PULL_SLIP_SUMMARY = '''
    Select count(pr.id), pr.pickLocation.code
    from PatronRequest as pr
    where pr.state.code='RES_NEW_AWAIT_PULL_SLIP'
    group by pr.pickLocation.code
'''

  private static String EMAIL_TEMPLATE='''
<h1>Example email template</h1>
<p>
$numRequests waiting to be printed at $location
Click <a href="http://some.host">To view in the reshare app</a>
</p>

<ul>
  <% sumamry.each { s -> %>
    <li>There are ${s[0]} pending pull slips at locaiton ${s[1]}</li>
  <% } %>
</ul>
'''

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

        // Generate and log patron requests at a pick location we don't know about
        reportMissingPickLocations()
  
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

        long current_systime = System.currentTimeMillis();

        // Dump all timers whilst we look into timer execution
        Timer.list().each { ti ->
          def remaining_min = ((ti.lastExecution?:0)-current_systime)/60000
          log.debug("Declared timer: ${ti.id}, ${ti.lastExecution}, ${ti.enabled}, ${ti.rrule}, ${ti.taskConfig} remaining=${remaining_min}");
        }

        Timer.executeQuery('select t from Timer as t where ( ( t.lastExecution is null ) OR ( t.lastExecution < :now ) ) and t.enabled=:en', 
                           [now:current_systime, en: true]).each { timer ->
          try {
            log.debug("** Timer task ${timer.id} firing....");
            runTimer(timer);

            String rule_to_parse = timer.rrule.startsWith('RRULE:') ? timer.rrule.substring(6) : timer.rrule;

            // Caclulate the next due date
            RecurrenceRule rule = new RecurrenceRule(rule_to_parse);
            // DateTime start = DateTime.now()
            DateTime start = new DateTime(current_systime)
            RecurrenceRuleIterator rrule_iterator = rule.iterator(start);
            def nextInstance = null;

            // Cycle forward to the next occurrence after this moment
            int loopcount = 0;
            while ( ( ( nextInstance == null ) || ( nextInstance.getTimestamp() < current_systime ) ) && 
                    ( loopcount++ < 10 ) ) {
              nextInstance = rrule_iterator.nextDateTime();
              log.debug("Test Next calendar instance : ${nextInstance} (remaining=${nextInstance.getTimestamp()-System.currentTimeMillis()})");
            }
            log.debug("Calculated next event for ${timer.id}/${timer.taskCode}/${timer.rrule} as ${nextInstance}");
            log.debug(" -> selected as timestamp ${nextInstance.getTimestamp()}");
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

      def pull_slip_overall_summary = PatronRequest.executeQuery(PULL_SLIP_SUMMARY);

      log.debug("pull slip summary: ${pull_slip_overall_summary}");

      // DirectoryEntry de = DirectoryEntry.get(location);
      HostLMSLocation psloc = HostLMSLocation.get(location);

      if ( ( psloc != null ) && ( psloc.code != null ) ) {

        log.debug("Resolved directory entry ${location} - lmsLocationCode is ${psloc.code} name: ${psloc.name}");

        List<PatronRequest> pending_ps_printing = PatronRequest.executeQuery(PULL_SLIP_QUERY,[loccode:psloc.code]);
  
        if ( ( pending_ps_printing != null ) &&
             ( pending_ps_printing.size() > 0 ) ) {
  
          log.debug("${pending_ps_printing.size()} pending pull slip printing for location ${location}");
      
          // 'from':'admin@reshare.org',
          def engine = new groovy.text.GStringTemplateEngine()
          def email_template = engine.createTemplate(EMAIL_TEMPLATE).make([ numRequests:pending_ps_printing.size(), 
                                                                            location: location,
                                                                            summary: pull_slip_overall_summary])
          String body_text = email_template.toString()
  
          Map email_params = [
                'notificationId':'1',
                            'to':'ianibbo@gmail.com',
                        'header':"Reshare location ${location} has ${pending_ps_printing.size()} requests that need pull slip printing",
                          'body':body_text
          ]
  
          Map email_result = emailService.sendEmail(email_params);
        }
        else {
          log.debug("No pending pull slips for ${location}");
        }
      }
      else {
        if ( de == null ) {
          log.warn("Failed to resolve directory entry id ${location}");
        }
        else {
          log.warn("Directory entry for location ${location} does not have a host LMS location code");
        }
      }

      log.debug("Dumping patron requests awaiting pull slip printing for validation");
      PatronRequest.executeQuery('select pr from PatronRequest as pr where pr.state.code=:ps', [ps:'RES_NEW_AWAIT_PULL_SLIP']).each {
        log.debug("${it.id} ${it.title} ${it.pickShelvingLocation} ${it.pickLocation?.code}");
      }
    }
    catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private void reportMissingPickLocations() {
    log.debug("reportMissingPickLocations()");
    // ToDo: Implement a function that lists all pr.pickShelvingLocation values that do not have a corresponding directoryEntry.hostLMSCode and email an admin
  }
}
