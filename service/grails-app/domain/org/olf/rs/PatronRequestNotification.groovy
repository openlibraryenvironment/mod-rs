package org.olf.rs

import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import org.olf.okapi.modules.directory.Symbol;
import java.time.Instant;

class PatronRequestNotification implements MultiTenant<PatronRequest> {
  
  // Internal id of the message
  String id

  static belongsTo = [patronRequest : PatronRequest]

  // Default date metadata maintained by the db
  Date dateCreated
  Date lastUpdated

  Instant timestamp

  // The 'seen/unseen' bool is now referred to as 'read/unread' in the front end
  Boolean seen

  Boolean isSender


  // attachedAction pertains to the action ISO18626 action or reasonForMessage, ie "RequestResponse" from Supplying Agency or "Received" from Requesting Agency
  String attachedAction

  // The below two fields will *probably* only be used for notifications sent from supplying agencies...
  // actionStatus will contain information about what state changes might happen as a result of this message, ie: "WillSupply", "Unfilled" or "Conditional"
  String actionStatus
  // actionData will contain any further information, such as "reasonUnfilled" or "loanConditions"
  String actionData


  // The sender/receiver variables must be nullable, since we could have a system automated timeout notifcation etc
  Symbol messageSender
  Symbol messageReceiver

  // This will hold a String containing all the information to be displayed to the user
  String messageContent


  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronRequest (nullable: true)
    attachedAction( nullable: true)
    actionStatus( nullable: true)
    actionData( nullable: true)
    timestamp (nullable: true, blank: false)
    seen (nullable: true, blank: false)
    isSender (nullable: true, blank: false)
    messageSender (nullable: true, blank: false)
    messageReceiver (nullable: true, blank: false)
  }

  static mapping = {
    id column : 'prn_id', generator: 'uuid2', length:36
    version column : 'prn_version'
    dateCreated column : 'prn_date_created'
    lastUpdated column : 'prn_last_updated'
    timestamp column : 'prn_timestamp'
    seen column : 'prn_seen'
    isSender column : 'prn_is_sender'
    attachedAction column: 'prn_attached_action'
    actionStatus column: 'prn_action_status'
    actionData column: 'prn_action_data'
    messageSender column : 'prn_message_sender_fk'
    messageReceiver column : 'prn_message_receiver_fk'
    messageContent column : 'prn_message_content'
    patronRequest column : 'prn_patron_request_fk'
  }
}