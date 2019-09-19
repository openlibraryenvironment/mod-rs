package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import java.time.LocalDateTime
import grails.gorm.MultiTenant


class HostLMSLocation implements MultiTenant<HostLMSLocation> {

  String id
  String code

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

  static constraints = {
    code (nullable: false)
    icalRrule (nullable: true)
    lastCompleted (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }

  static mapping = {
    table 'host_lms_location'
               id column : 'hll_id', generator: 'uuid2', length:36
          version column : 'hll_version'
             code column : 'hll_code'
        icalRrule column : 'hll_ical_rrule'
    lastCompleted column : 'hll_last_completed'
      dateCreated column : 'hll_date_created'
      lastUpdated column : 'hll_last_updated'
  }

  public String toString() {
    return "HostLMSLocation: ${code}".toString()
  }
}

