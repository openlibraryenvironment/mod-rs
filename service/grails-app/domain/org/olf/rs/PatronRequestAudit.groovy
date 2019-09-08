package org.olf.rs

import grails.gorm.MultiTenant;
import org.olf.rs.statemodel.Status;
import java.time.LocalDateTime

class PatronRequestAudit implements MultiTenant<PatronRequestAudit> {

  // internal ID of the audit record
  String id

  /** The request this audit record belongs to */
  static belongsTo = [patronRequest : PatronRequest]

  /** The date time the action was processed */
  Date dateCreated;

  /** The status the action was in, when we started processing */
  Status fromStatus;

  /** The status we ended up in after performing the action */
  Status toStatus;

  /** How long it took to process this action */
  Long duration;

  /** Human readable message */
  String message;

  /** JSON Payload */
  String auditData;

  static constraints = {
    dateCreated   (nullable : true) // Because this isn't set until after validation!
    duration      (nullable : true)
    fromStatus    (nullable : true)
    patronRequest (nullable : false)
    toStatus      (nullable : true)
    message       (nullable : true)
    auditData     (nullable : true)
  }

  static mapping = {
    id            column : 'pra_id', generator: 'uuid2', length:36
    version       column : 'pra_version'
    dateCreated   column : 'pra_date_created'
    duration      column : 'pra_duration'
    fromStatus    column : 'pra_from_status_fk'
    patronRequest column : 'pra_patron_request_fk'
    toStatus      column : "pra_to_status_fk"
    message       column : "pra_message", type: 'text'
    auditData     column : "pra_audit_data", type: 'text'
  }
}
