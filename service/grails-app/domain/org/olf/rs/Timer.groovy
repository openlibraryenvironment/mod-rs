package org.olf.rs

import grails.gorm.MultiTenant;

/**
 * System timers - used to schedule recurring tasks
 */
class Timer implements MultiTenant<Timer> {

  String id
  String description
  String rrule
  Long lastExecution
  String taskCode
  String taskConfig
  
  static constraints = {
      description (nullable : true,  blank: false)
            rrule (nullable : false, blank: false)
    lastExecution (nullable : false, blank: false)
         taskCode (nullable : false, blank: false)
       taskConfig (nullable : false, blank: false)
  }

  static mapping = {
    id                     column : 'tr_id', generator: 'uuid2', length:36
    version                column : 'tr_version'
    description            column : 'tr_description'
    rrule                  column : 'tr_rrule'
    lastExecution          column : 'tr_last_exec'
    taskCode               column : 'tr_task_code'
    taskConfig             column : 'tr_task_config', type: 'text'
  }

}
