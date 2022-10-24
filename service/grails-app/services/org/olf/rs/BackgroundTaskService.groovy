package org.olf.rs;

import java.lang.management.ManagementFactory;
import java.text.NumberFormat;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.Freq;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.olf.rs.timers.AbstractTimer;

import grails.util.Holders;
import groovy.json.JsonSlurper;

/**
 * This handles the background tasks, these are triggered by the folio 2 minute timer
 *
 */
public class BackgroundTaskService {

  def grailsApplication

  PatronNoticeService patronNoticeService
  OkapiSettingsService okapiSettingsService

  // Holds the services that we have discovered that perform tasks for the timers
  private static Map serviceTimers = [ : ];

  def performReshareTasks() {
    log.debug("performReshareTasks() as at ${new Date()}");

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
      patronNoticeService.processQueue()
    }

    try {
        // Process any timers for sending pull slip notification emails
        // Refactor - lastExcecution now contains the next scheduled execution or 0
        // log.debug("Checking timers ready for execution");

        long current_systime = System.currentTimeMillis();

        log.debug("Checking timers");
        Timer[] timers = Timer.executeQuery('select t from Timer as t where ( ( t.nextExecution is null ) OR ( t.nextExecution < :now ) ) and t.enabled=:en',
                           [now:current_systime, en: true]);
        if ((timers != null) && (timers.size()> 0)) {
            timers.each { timer ->
              try {
                log.debug("** Timer task ${timer.id} firing....");

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

    			// The date we start processing this in the local time zone
                timer.lastExecution = new DateTime(tz, System.currentTimeMillis()).getTimestamp();

                if ( ( timer.nextExecution == 0 ) || ( timer.nextExecution == null ) ) {
                  // First time we have seen this timer - we don't know when it is next due - so work that out
                  // as though we just run the timer.
                }
                else {
                  runTimer(timer)
                };

                String rule_to_parse = timer.rrule.startsWith('RRULE:') ? timer.rrule.substring(6) : timer.rrule;

                // Calculate the next due date
                RecurrenceRule rule = new RecurrenceRule(rule_to_parse);
                // DateTime start = DateTime.now()
                // DateTime start = new DateTime(current_systime)
                // DateTime start = new DateTime(TimeZone.getTimeZone("UTC"), current_systime)

                DateTime start = new DateTime(tz, current_systime);
    			// If the frequency, is daily, monthly or yearly then we need to clear the time part
    			if (rule_to_parse.contains(Freq.DAILY.toString()) ||
    				rule_to_parse.contains(Freq.MONTHLY.toString()) ||
    				rule_to_parse.contains(Freq.WEEKLY.toString()) ||
    				rule_to_parse.contains(Freq.YEARLY.toString())) {
    				// Set it to the start of the day, otherwise we will have jobs happening during the day
    				start = start.startOfDay();
    			}

    			// Now work out what the next execution time will be
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
                timer.nextExecution = nextInstance.getTimestamp();
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
      log.debug("BackgroundTaskService::performReshareTasks exiting");
    }
  }

  	private runTimer(Timer t) {
		try {
			if (t.taskCode != null) {
				// Get hold of the bean and store it in our map, if we previously havn't been through here
				if (serviceTimers[t.taskCode] == null) {
					// We capitalize the task code and then prefix it with "timer" and postfix with "Service"
					String beanName = "timer" + t.taskCode.capitalize() + "Service";

					// Now setup the link to the service action that actually does the work
					try {
						serviceTimers[t.taskCode] = Holders.grailsApplication.mainContext.getBean(beanName);
					} catch (Exception e) {
						log.error("Unable to locate timer bean: " + beanName);
					}
				}

				// Did we find the bean
				AbstractTimer timerBean = serviceTimers[t.taskCode];
				if (timerBean == null) {
					log.debug("Unhandled timer task code ${t.taskCode}");
				} else {
                    // Start a new session and transaction for each timer, so that everything is not in the same transaction
                    // As the transactions in the different timers should not be related in any form
                    Timer.withNewSession { session ->
                        try {
                            // Start a new transaction
                            Timer.withNewTransaction {
    							// just call the performTask method, with the timers config
    							timerBean.performTask(t.taskConfig);
                            }
                        } catch(Exception e) {
                            log.error("Exception thrown by timer " + t.code, e);
                        }
                    }
				}
			}
		} catch ( Exception e ) {
			log.error("ERROR running timer",e)
		}
  	}
}
