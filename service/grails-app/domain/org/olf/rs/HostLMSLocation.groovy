package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import java.time.LocalDateTime
import grails.gorm.MultiTenant

class HostLMSLocation implements MultiTenant<HostLMSLocation> {

  String id
  String code
  String name

  // The iCal recurrence rule (https://www.kanzaki.com/docs/ical/rrule.html) that defines how
  // often pull slip batching happens for this location.
  String icalRrule

  // The datetime the icalRule last matched a new date time
  // considering https://github.com/dmfs/lib-recur
  // and https://github.com/dlemmermann/google-rfc-2445 (see also https://www.programcreek.com/java-api-examples/index.php?api=com.google.ical.values.RRule)
  // My intention is to store the time of last run, then use an iterator on that time to work out the next (as per google example above)
  // event. If now() > that next event time, then the event needs to run.
  // note: https://javalibs.com/artifact/org.scala-saddle/google-rfc-2445
  Long lastCompleted

  Date dateCreated
  Date lastUpdated

  // < 0 - Never use
  // 0 - No preference / default
  // > 0 - Preference order
  Long supplyPreference

  static constraints = {
    code (nullable: false)
    name (nullable: true)
    icalRrule (nullable: true)
    lastCompleted (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    supplyPreference (nullable: true)
  }

  static mapping = {
    table 'host_lms_location'
                    id column : 'hll_id', generator: 'uuid2', length:36
               version column : 'hll_version'
                  code column : 'hll_code'
                  name column : 'hll_name'
             icalRrule column : 'hll_ical_rrule'
         lastCompleted column : 'hll_last_completed'
           dateCreated column : 'hll_date_created'
           lastUpdated column : 'hll_last_updated'
      supplyPreference column : 'hll_supply_preference'
  }

  public String toString() {
    return "HostLMSLocation: ${code}".toString()
  }
}

