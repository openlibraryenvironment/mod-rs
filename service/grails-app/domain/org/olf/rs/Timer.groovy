package org.olf.rs

import grails.gorm.MultiTenant;

/**
 * System timers - used to schedule recurring tasks
 */
class Timer implements MultiTenant<Timer> {

  String id
  String description;

  // A simple code this timer is known by
  String code;

  // When to execute, see https://icalendar.org/iCalendar-RFC-5545/3-8-5-3-recurrence-rule.html
  String rrule;

  // When this job was last executed (Note: used to be the time it was to be next executed)
  Long lastExecution;

  // When this job will be next executed
  Long nextExecution;

  // The task to be executed, if the task is not explicitly known it will look for a service with a name prefixed with timer and postfixed with Service and uppercase the first letter
  String taskCode;

  // Any configuration that is specific to this task that needs to be passed to the proessor
  String taskConfig;

  // Is this timer enabled or not
  Boolean enabled;

  // Do we execute this timer at the start of the day
  boolean executeAtDayStart;

  static constraints = {
                 code (nullable : true, blank:false)
          description (nullable : true, blank: false)
                rrule (nullable : true, blank: false)
        lastExecution (nullable : true)
        nextExecution (nullable : true)
             taskCode (nullable : false, blank: false)
           taskConfig (nullable : true, blank: false)
              enabled (nullable : true)
  }

  static mapping = {
    id                     column : 'tr_id', generator: 'uuid2', length:36
    version                column : 'tr_version'
    code                   column : 'tr_code'
    description            column : 'tr_description'
    rrule                  column : 'tr_rrule'
    lastExecution          column : 'tr_last_exec'
    nextExecution          column : 'tr_next_exec'
    taskCode               column : 'tr_task_code'
    taskConfig             column : 'tr_task_config', type: 'text'
    enabled                column : 'tr_enabled'
    executeAtDayStart      column : 'tr_execute_at_day_start', defaultValue : "false"
  }


  	public static Timer ensure(
        String code,
        String description,
        String rrule,
        String taskCode,
        String config = null,
        boolean enabled = true,
        boolean executeAtDayStart = false
    ) {
		Timer timer = Timer.findByCode(code);
		if (timer == null) {
			timer = new Timer(
				code: code
			);
		}

        // Update the various properties of the timer
        timer.description = description;
        timer.rrule = rrule;
        timer.taskCode = taskCode;
        timer.taskConfig = config;
        timer.enabled = enabled;
        timer.executeAtDayStart = executeAtDayStart;

		// Save the timer
		timer.save(flush:true, failOnError:true);

		// Caller may want to do something with it
		return(timer);
	}
}
