package org.olf.rs

import grails.gorm.MultiTenant;
import org.olf.rs.statemodel.Status;
import org.olf.okapi.modules.directory.Symbol

class PatronRequestRota implements MultiTenant<PatronRequestRota> {

  // internal ID of the audit record
  String id

  // These 2 dates are maintained by the framework for us
  Date dateCreated
  Date lastUpdated
  
  /** The request this audit record belongs to */
  static belongsTo = [patronRequest : PatronRequest]

  /** The position in the rota */
  Long rotaPosition;

  /** The directory entry that represents this rota entry */ 
  String directoryId;

  /** The system identifier for the found item */
  String systemIdentifier;

  /** The shelfmark also known as call number, this is where it lives on the shelf */
  String shelfmark;

  /** Availability as returned */
  String availability;

  /** The normalised availability - this is what we have interpreted the availability field as */
  NormalisedAvailability normalisedAvailability;

  /** The date we have determined the item is available from */
  Date availableFrom;

  /** The status the protocol thinks we are at with this messsage,
   *  the meaning we vary from protocol to protocol, but it is assumed it will map onto an enum 
   */
  Long protocolStatus;

  /**
   * The load balancer will assign a score for ranking
   */
  Long loadBalancingScore

  /**
   * For the load balance to explain it's reasons
   */
  String loadBalancingReason

  /**
   * a workflow state for this rota entry
   */
  Status state

  Symbol peerSymbol

  String note

  String instanceIdentifier
  String copyIdentifier

  static constraints = {
    availability           (nullable : true,  blank: false)
    availableFrom          (nullable : true)
    dateCreated            (nullable : true)
    directoryId            (nullable : false, blank : false, maxSize : 36)
    lastUpdated            (nullable : true)
    normalisedAvailability (nullable : true)
    patronRequest          (nullable : false)
    protocolStatus         (nullable : true)
    rotaPosition           (nullable : false) // unique['patronRequest'] if you wanted to - but I don't know why you would.
    shelfmark              (nullable : true,  blank: false)
    systemIdentifier       (nullable : true,  blank: false)
    state                  (nullable : true)
    peerSymbol             (nullable : true)
    instanceIdentifier     (nullable : true)
    copyIdentifier         (nullable : true)
    note                   (nullable : true)
    loadBalancingScore     (nullable : true)
    loadBalancingReason    (nullable : true)
  }

  static mapping = {
    id                     column : 'prr_id', generator: 'uuid2', length:36
    version                column : 'prr_version'
    availability           column : 'prr_availability'
    availableFrom          column : 'prr_available_from'
    dateCreated            column : 'prr_date_created'
    directoryId            column : 'prr_directory_id_fk' // Note: It is a foreign key into mod directory
    lastUpdated            column : 'prr_last_updated'
    normalisedAvailability column : "prr_normalised_availability"
    patronRequest          column : "prr_patron_request_fk"
    protocolStatus         column : "prr_protocol_status"
    rotaPosition           column : "prr_rota_position"
    shelfmark              column : "prr_shelfmark"
    systemIdentifier       column : "prr_system_identifier"
    state                  column : "prr_state_fk"
    peerSymbol             column : "prr_peer_symbol_fk"
    instanceIdentifier     column : "prr_instance_identifier"
    copyIdentifier         column : "prr_copy_identifier"
    note                   column : "prr_note"
    loadBalancingScore     column : 'prr_lb_score'
    loadBalancingReason    column : 'prr_lb_reason', type: 'text'
  }
}
