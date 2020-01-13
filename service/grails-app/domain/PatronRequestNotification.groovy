package org.olf.rs

import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import org.olf.okapi.modules.directory.Symbol;
import java.time.LocalDate;

class PatronRequestNotification implements MultiTenant<PatronRequest> {
  
  // Internal id of the message
  String id

  static belongsTo = [patronRequest : PatronRequest]

  // Default date metadata maintained by the db
  Date dateCreated

  LocalDate timestamp

  Boolean seen

  // The sender/receiver variables must be nullable, since we could have a system automated timeout notifcation etc
  Symbol messageSender
  Symbol messageReceiver

  // This will hold a String containing all the information to be displayed to the user
  String messageContent


  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronRequest (nullable: false)
    timestamp (nullable: false, blank: false)
    seen (nullable: false, blank: false)
    messageSender (nullable: true, blank: false)
    messageReceiver (nullable: true, blank: false)
  }

  static mapping = {
    id column : 'prn_id', generator: 'uuid2', length:36
    dateCreated column : 'prn_date_created'
    timestamp column : 'prn_timestamp'
    seen column : 'prn_seen'
    messageSender column : 'prn_message_sender_fk'
    messageReceiver column : 'prn_message_receiver_fk'
    messageContent column : 'prn_message_content'
  }
}