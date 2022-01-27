package org.olf.rs

import grails.gorm.MultiTenant;
import org.springframework.web.context.request.RequestContextHolder;
import org.olf.rs.statemodel.Status;

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

  /** ID of user who performed action if available **/
  String user;

  static constraints = {
    dateCreated   (nullable : true) // Because this isn't set until after validation!
    duration      (nullable : true)
    fromStatus    (nullable : true)
    patronRequest (nullable : false)
    toStatus      (nullable : true)
    message       (nullable : true)
    auditData     (nullable : true)
    user          (nullable : true)
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
    user          column : 'pra_user'
  }

  def beforeInsert() {
    try {
      def uid = RequestContextHolder.currentRequestAttributes().getHeader('X-Okapi-User-Id');
      if (uid) {
        this.user = uid;
      }
    } catch (Exception e) {}
  }
}
