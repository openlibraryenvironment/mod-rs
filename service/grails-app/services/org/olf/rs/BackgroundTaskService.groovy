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
import com.k_int.web.toolkit.settings.AppSetting
import java.util.TimeZone;
import java.text.NumberFormat;
import org.olf.templating.*
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class BackgroundTaskService {

  def grailsApplication
  def reshareActionService
  def groovyPageRenderer
  def templatingService

  OkapiClient okapiClient
  EmailService emailService
  PatronNoticeService patronNoticeService
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  OkapiSettingsService okapiSettingsService


  private static config_test_count = 0;
  private static String PULL_SLIP_QUERY='''
Select pr 
from PatronRequest as pr
where ( pr.pickLocation.id in ( :loccodes ) )
and pr.state.code='RES_NEW_AWAIT_PULL_SLIP'
'''

  private static String PULL_SLIP_SUMMARY = '''
    Select count(pr.id), pr.pickLocation.code
    from PatronRequest as pr
    where pr.state.code='RES_NEW_AWAIT_PULL_SLIP'
    group by pr.pickLocation.code
'''

  def performReshareTasks(String tenant) {
    log.debug("performReshareTasks(${tenant}) as at ${new Date()}");

    Runtime runtime = Runtime.getRuntime();

    NumberFormat format = NumberFormat.getInstance();

    StringBuilder sb = new StringBuilder();
    long maxMemory = runtime.maxMemory();
    long allocatedMemory = runtime.totalMemory();
    long freeMemory = runtime.freeMemory();
    long jvmUpTime = ManagementFactory.getRuntimeMXBean().getUptime();

    log.info("free memory: " + format.format(freeMemory / 1024));
    log.info("allocated memory: " + format.format(allocatedMemory / 1024));
    log.info("max memory: " + format.format(maxMemory / 1024));
    log.info("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
    log.info("JVM uptime: " + format.format(jvmUpTime));

    if ( grailsApplication.config?.reshare?.patronNoticesEnabled == true ) {
      patronNoticeService.processQueue(tenant)
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
        
        //Find any supplier-side PatronRequests that have become overdue
        log.debug("Checking for overdue PatronRequests");
        Date currentDate = new Date();
        def criteria = PatronRequest.createCriteria();
        def results = criteria.list {
          lt("parsedDueDateRS", currentDate) //current date is later than due date
          state {
            eq("code","RES_ITEM_SHIPPED") //only marked items as overdue once shipped
          }
          ne("isRequester", true) //request is not request-side (we want supply-side)
        }
        results.each { patronRequest ->
          log.debug("Found PatronRequest ${patronRequest.id} with state ${patronRequest.state?.code}");
          def previousState = patronRequest.state;
          def overdueState = reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_OVERDUE');
          if(overdueState == null) {
            log.error("Unable to lookup state with reshareApplicationEventHandlerService.lookupStatus('Responder', 'RES_OVERDUE')");            
          } else {
            patronRequest.state = overdueState;
            reshareApplicationEventHandlerService.auditEntry(patronRequest, previousState, overdueState, "Request is Overdue", null);
            log.debug("PatronRequest ${patronRequest.id} is overdue -- currently ${currentDate} and due on ${patronRequest.parsedDueDateRS}");
            patronRequest.save(flush:true, failOnError:true);
          }
        }

        // Process any timers for sending pull slip notification emails
        // Refactor - lastExcecution now contains the next scheduled execution or 0
        // log.debug("Checking timers ready for execution");

        long current_systime = System.currentTimeMillis();

        // Dump all timers whilst we look into timer execution
        Timer.list().each { ti ->
          def remaining_min = ((ti.lastExecution?:0)-current_systime)/60000
          log.debug("Declared timer: ${ti.id}, ${ti.lastExecution}, ${ti.enabled}, ${ti.rrule}, ${ti.taskConfig} remaining=${remaining_min}min");
        }

        Timer.executeQuery('select t from Timer as t where ( ( t.lastExecution is null ) OR ( t.lastExecution < :now ) ) and t.enabled=:en', 
                           [now:current_systime, en: true]).each { timer ->
          try {
            log.debug("** Timer task ${timer.id} firing....");

            if ( ( timer.lastExecution == 0 ) || ( timer.lastExecution == null ) ) {
              // First time we have seen this timer - we don't know when it is next due - so work that out
              // as tho we just run the timer.
            }
            else {
              runTimer(timer)
            };

            String rule_to_parse = timer.rrule.startsWith('RRULE:') ? timer.rrule.substring(6) : timer.rrule;

            // Caclulate the next due date
            RecurrenceRule rule = new RecurrenceRule(rule_to_parse);
            // DateTime start = DateTime.now()
            // DateTime start = new DateTime(current_systime)
            // DateTime start = new DateTime(TimeZone.getTimeZone("UTC"), current_systime)

            TimeZone tz;
            try {
              JsonSlurper jsonSlurper = new JsonSlurper();
              def tenant_locale = jsonSlurper.parseText(okapiSettingsService.getSetting('localeSettings').value);
              log.debug("Got system locale settings : ${tenant_locale}");
              tz = TimeZone.getTimeZone(tenant_locale?.timezone);
            }
            catch ( Exception e ) {
              log.debug("Failure getting locale to determine timezone, processing timer in UTC:", e);
              tz = TimeZone.getTimeZone('UTC');
            }

            DateTime start = new DateTime(tz, current_systime);
            RecurrenceRuleIterator rrule_iterator = rule.iterator(start);
            def nextInstance = null;

            // Cycle forward to the next occurrence after this moment
            int loopcount = 0;
            while ( ( ( nextInstance == null ) || ( nextInstance.getTimestamp() < current_systime ) ) && 
                    ( loopcount++ < 10 ) ) {
              nextInstance = rrule_iterator.nextDateTime();
            }
            log.debug("Calculated next event for ${timer.id}/${timer.taskCode}/${timer.rrule} as ${nextInstance} (remaining=${nextInstance.getTimestamp()-System.currentTimeMillis()})");
            log.debug(" -> selected as timestamp ${nextInstance.getTimestamp()}");
            timer.lastExecution = nextInstance.getTimestamp();
            timer.save(flush:true, failOnError:true)
          }
          catch ( Exception e ) {
            log.error("Unexpected error processing timer tasks ${e.message} - rule is \"${timer.rrule}\"");
          }
          finally {
            // log.debug("Completed scheduled task checking");
          }
        }
        
      }
    }
    catch ( Exception e ) {
      log.error("Exception running background tasks",e);
    }
    finally {
      // log.debug("BackgroundTaskService::performReshareTasks exiting");
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

  private void checkPullSlips(Map timer_cfg) {
    log.debug("checkPullSlips(${timer_cfg})");
    checkPullSlipsFor(timer_cfg.locations,
                      timer_cfg.confirmNoPendingRequests?:true, 
                      timer_cfg.emailAddresses);
  }

  private void checkPullSlipsFor(ArrayList loccodes, 
                                 boolean confirm_no_pending_slips,
                                 ArrayList emailAddresses) {

    // See /configurations/entries?query=code==FOLIO_HOST
    // See /configurations/entries?query=code==localeSettings
    log.debug("checkPullSlipsFor(${loccodes},${confirm_no_pending_slips},${emailAddresses})");

    try {
      AppSetting pull_slip_template_setting = AppSetting.findByKey('pull_slip_template_id')
      TemplateContainer tc = TemplateContainer.read(pull_slip_template_setting?.value)

      if (tc != null) {
        def pull_slip_overall_summary = PatronRequest.executeQuery(PULL_SLIP_SUMMARY);
        log.debug("pull slip summary: ${pull_slip_overall_summary}");

        List<HostLMSLocation> pslocs = HostLMSLocation.executeQuery('select h from HostLMSLocation as h where h.id in ( :loccodes )',[loccodes:loccodes])

        if ( ( pslocs.size() > 0 ) && ( emailAddresses != null ) ) {
          log.debug("Resolved locations ${pslocs} - send to ${emailAddresses}");

          List<PatronRequest> pending_ps_printing = PatronRequest.executeQuery(PULL_SLIP_QUERY,[loccodes:loccodes]);
    
          if ( pending_ps_printing != null ) {

            if ( ( pending_ps_printing.size() > 0 ) || confirm_no_pending_slips ) {
              log.debug("${pending_ps_printing.size()} pending pull slip printing for locations ${pslocs}");
              
              String locationsText = pslocs.inject('', { String output, HostLMSLocation loc ->
                output + (output != '' ? ', ' : '') + (loc.name ?: loc.code)
              })

              String locFilters = loccodes.collect{"location.${it}"}.join('%2C')

              // 'from':'admin@reshare.org',
              def tmplResult = templatingService.performTemplate(
                tc,
                [
                  locations: locationsText,
                  pendingRequests: pending_ps_printing,
                  numRequests:pending_ps_printing.size(),
                  summary: pull_slip_overall_summary,
                  reshareURL: "${okapiSettingsService.getSetting('FOLIO_HOST')?.value}/supply/requests?filters=${locFilters}%2Cstate.RES_NEW_AWAIT_PULL_SLIP&sort=-dateCreated"
                ],
                "en"
              );

              emailAddresses.each { to ->
                Map email_params = [
                  notificationId: '1',
                  to: to,
                  header: tmplResult.result.header,
                  body: tmplResult.result.body,
                  outputFormat: "text/html"
                ]

                Map email_result = emailService.sendEmail(email_params);
              }
            }
          }
          else {
            log.debug("No pending pull slips for ${loccodes}");
          }
        }
        else {
          log.warn("Problem resolving locations or email addresses");
        }
      } else {
        log.error("Cannot find a pull slip template")
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
