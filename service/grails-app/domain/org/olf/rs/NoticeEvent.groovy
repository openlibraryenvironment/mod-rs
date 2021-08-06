package org.olf.rs

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue

class NoticeEvent implements MultiTenant<NoticeEvent> {

  long id
  PatronRequest patronRequest
  RefdataValue trigger
  boolean sent
  Date dateCreated

  static constraints = {
    patronRequest (nullable: false)
    trigger (nullable: false)
    dateCreated (nullable: true, bindable: false)
  }

  static mapping = {
    id column: 'ne_id'
    patronRequest column: 'ne_patron_request_fk'
    trigger column: 'ne_trigger_fk'
    sent column: 'ne_sent', defaultValue: false
    dateCreated column: 'ne_date_created'
    version column: 'ne_version'
  }
}
