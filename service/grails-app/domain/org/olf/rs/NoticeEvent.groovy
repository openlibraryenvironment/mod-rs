package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.MultiTenant

class NoticeEvent implements MultiTenant<NoticeEvent> {

  long id;
  PatronRequest patronRequest;
  RefdataValue trigger;
  boolean sent;
  Date dateCreated;
  String jsonData;

  static constraints = {
    patronRequest (nullable: true)
    trigger (nullable: false)
    dateCreated (nullable: true, bindable: false)
    jsonData (nullable: true);
  }

  static mapping = {
    id            column: 'ne_id'
    patronRequest column: 'ne_patron_request_fk'
    jsonData      column: "ne_json_data", type: 'text'
    trigger       column: 'ne_trigger_fk'
    sent          column: 'ne_sent', defaultValue: false
    dateCreated   column: 'ne_date_created'
    version       column: 'ne_version'
  }
}
