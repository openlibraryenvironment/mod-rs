package org.olf.rs

import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.Status;
import org.springframework.web.context.request.RequestContextHolder;

import grails.gorm.MultiTenant;

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

    /** The audit number of this record within the request */
    Integer auditNo;

    /** The action or event that was performed */
    ActionEvent actionEvent;

    /** The rota position this record applied to */
    Long rotaPosition;

    /** Whether we have performed an undo operation on this action / event */
    Boolean undoPerformed;

    /** The sequence the message was sent with, if it was sent to the other side of the transaction */
    Integer messageSequenceNo;

    static constraints = {
        dateCreated   (nullable : true) // Because this isn't set until after validation!
        duration      (nullable : true)
        fromStatus    (nullable : true)
        patronRequest (nullable : false)
        toStatus      (nullable : true)
        message       (nullable : true)
        auditData     (nullable : true)
        user          (nullable : true)

        // The fields added for the undo functionality
        auditNo           (nullable : true)
        actionEvent       (nullable : true)
        rotaPosition      (nullable : true)
        undoPerformed     (nullable : true)
        messageSequenceNo (nullable : true)
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

        // The fields added for the undo functionality
        auditNo           column : 'pra_audit_no'
        actionEvent       column : 'pra_action_event'
        rotaPosition      column : 'pra_rota_position'
        undoPerformed     column : 'pra_undo_performed'
        messageSequenceNo column : 'pra_message_sequence_no'
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
