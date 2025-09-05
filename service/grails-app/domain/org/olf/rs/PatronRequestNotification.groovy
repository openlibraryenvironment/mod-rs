package org.olf.rs

import java.time.Instant;

import org.olf.okapi.modules.directory.Symbol;

import grails.gorm.MultiTenant

class PatronRequestNotification implements MultiTenant<PatronRequestNotification> {

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
  String senderSymbol

  // This will hold a String containing all the information to be displayed to the user
  String messageContent

  String messageStatus


  static constraints = {
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronRequest (nullable: true)
    attachedAction( nullable: true)
    actionStatus( nullable: true)
    actionData( nullable: true)
    timestamp (nullable: true)
    seen (nullable: true)
    isSender (nullable: true)
    messageSender (nullable: true)
    messageReceiver (nullable: true)
    senderSymbol (nullable: true)
    messageStatus (nullable: true)
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
    senderSymbol column : 'prn_sender_symbol'
    messageContent column : 'prn_message_content'
    messageStatus column : 'prn_message_status'
    patronRequest column : 'prn_patron_request_fk'
  }
}