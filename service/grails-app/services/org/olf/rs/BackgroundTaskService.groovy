package org.olf.rs;

import java.lang.management.ManagementFactory;
import java.text.NumberFormat;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.olf.rs.logging.ContextLogging;
import org.olf.rs.timers.AbstractTimer;

import grails.util.Holders;

/**
 * This handles the background tasks, these are triggered by the folio 2 minute timer
 *
 */
public class BackgroundTaskService {

    def grailsApplication;

    LockService lockService;
    OkapiSettingsService okapiSettingsService;

    // Holds the services that we have discovered that perform tasks for the timers
    private static Map serviceTimers = [ : ];

    def performReshareTasks(String tenant) {

        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();

        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long jvmUpTime = ManagementFactory.getRuntimeMXBean().getUptime();

        ContextLogging.setValue(ContextLogging.FIELD_MEMORY_FREE, format.format(freeMemory / 1024));
        ContextLogging.setValue(ContextLogging.FIELD_MEMORY_ALLOCATED, format.format(allocatedMemory / 1024));
        ContextLogging.setValue(ContextLogging.FIELD_MEMORY_MAX, format.format(maxMemory / 1024));
        ContextLogging.setValue(ContextLogging.FIELD_MEMORY_TOTAL_FREE, format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024));
        ContextLogging.setValue(ContextLogging.FIELD_JVM_UPTIME, format.format(jvmUpTime));
        log.debug(ContextLogging.MESSAGE_ENTERING + " performReshareTasks");

        // Only want these in the context logging once
        ContextLogging.remove(ContextLogging.FIELD_MEMORY_FREE);
        ContextLogging.remove(ContextLogging.FIELD_MEMORY_ALLOCATED);
        ContextLogging.remove(ContextLogging.FIELD_MEMORY_MAX);
        ContextLogging.remove(ContextLogging.FIELD_MEMORY_TOTAL_FREE);
        ContextLogging.remove(ContextLogging.FIELD_JVM_UPTIME);

        // We need a transaction in order to obtain the lock
        PatronRequest.withTransaction {
            // We do not want to do any processing if we are already performing the background processing
            // We have a distributed lock for when there are multiple mod-rs processes running
            if (!lockService.performWorkIfLockObtained(tenant, LockIdentifier.BACKGROUND_TASKS, 0) {
                doBackgroundTasks(tenant);
            }) {
                // Failed to obtain the lock
                log.info("Skiping background tasks as unable to obtain lock");
            }
        }

        log.debug(ContextLogging.MESSAGE_EXITING + " performReshareTasks");
    }

    private void doBackgroundTasks(String tenant) {

        // Everything should now be in a timer
        try {
            // Process any timers for sending pull slip notification emails
            // Refactor - lastExcecution now contains the next scheduled execution or 0
            // log.debug("Checking timers ready for execution");

            long current_systime = System.currentTimeMillis();

            int maxDelay = 5000
            int delay = Math.abs(new Random().nextInt() % maxDelay) + 1
            log.debug("Delaying ${delay} milliseconds prior to running background tasks");
            sleep(delay);

            log.debug("Checking timers");
            Timer[] timers = Timer.executeQuery('select t from Timer as t where ( ( t.nextExecution is null ) OR ( t.nextExecution < :now ) ) and t.enabled=:en',
                               [now:current_systime, en: true]);
            if ((timers != null) && (timers.size()> 0)) {
                timers.each { timer ->
                    try {
                        ContextLogging.setValue(ContextLogging.FIELD_ID, timer.id);
                        ContextLogging.setValue(ContextLogging.FIELD_TIMER, timer.taskCode);
                        ContextLogging.setValue(ContextLogging.FIELD_JSON, timer.taskConfig);
                        log.debug("** Timer task firing....");

                        TimeZone tz;
                        try {
                            Map tenant_locale = okapiSettingsService.getLocaleSettings();
                            log.debug("Got system locale settings : ${tenant_locale}");
                            String localeTimeZone = tenant_locale?.timezone;
                            if (localeTimeZone == null) {
                                // Time zone not set, so use UTC
                                tz = TimeZone.getTimeZone('UTC');
                            } else {
                                tz = TimeZone.getTimeZone(localeTimeZone);
                            }
                        } catch ( Exception e ) {
                            log.debug("Failure getting locale to determine timezone, processing timer in UTC:", e);
                            tz = TimeZone.getTimeZone('UTC');
                        }

                        // The date we start processing this in the local time zone
                        timer.lastExecution = new DateTime(tz, System.currentTimeMillis()).getTimestamp();

                        if ( ( timer.nextExecution == 0 ) || ( timer.nextExecution == null ) ) {
                            // First time we have seen this timer - we don't know when it is next due - so work that out
                            // as though we just run the timer.
                        } else {
                            runTimer(timer, tenant)
                        };

                        // The timer has completed its work
                        log.debug("** Timer task completed");

                        String rule_to_parse = timer.rrule.startsWith('RRULE:') ? timer.rrule.substring(6) : timer.rrule;

                        // Calculate the next due date
                        RecurrenceRule rule = new RecurrenceRule(rule_to_parse);
                        // DateTime start = DateTime.now()
                        // DateTime start = new DateTime(current_systime)
                        // DateTime start = new DateTime(TimeZone.getTimeZone("UTC"), current_systime)

                        DateTime start = new DateTime(tz, current_systime);
                        // If we are to be executed at the beginning of the day, then clear the time element
                        if (timer.executeAtDayStart) {
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
                    } catch ( Exception e ) {
                        log.error("Unexpected error processing timer tasks ${e.message} - rule is \"${timer.rrule}\"");
                    } finally {
                        log.debug("Completed scheduled task checking");
                    }
                }
            }
        } catch ( Exception e ) {
            log.error("Exception running background tasks",e);
        } finally {
            log.debug("BackgroundTaskService::performReshareTasks exiting");
        }
    }

  	private runTimer(Timer t, String tenant) {
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
    							timerBean.performTask(tenant, t.taskConfig);
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
