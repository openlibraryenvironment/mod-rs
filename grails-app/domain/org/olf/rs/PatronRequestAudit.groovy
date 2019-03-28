package org.olf.rs

import grails.gorm.MultiTenant;
import org.olf.rs.workflow.Action;
import org.olf.rs.workflow.Status;

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class PatronRequestAudit implements MultiTenant<PatronRequestAudit> {

  // internal ID of the audit record
  long id

  /** The request this audit record belongs to */
  static belongsTo = [patronRequest : PatronRequest]

  /** The date time the action was processed */
  Date dateCreated;

  /** The status the action was in, when we started processing */
  Status fromStatus;

  /** The action that was performed */ 
  Action action;

  /** The status we ended up in after performing the action */
  Status toStatus;

  /** How long it took to process this action */
  Long duration;

  static constraints = {
    action        (nullable : false)
    dateCreated   (nullable : false)
    duration      (nullable : false)
    fromStatus    (nullable : false)
    patronRequest (nullable : false)
    toStatus      (nullable : false)
  }

  static mapping = {
    id            column : 'pra_id'
    version       column : 'pra_version'
    action        column : 'pra_action_fk'
    dateCreated   column : 'pra_date_created'
    duration      column : 'pra_duration'
    fromStatus    column : 'pra_from_status_fk'
    patronRequest column : 'pra_patron_request_fk'
    toStatus      column : "pra_to_status_fk"
  }
}
