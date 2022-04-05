package org.olf.rs.statemodel;

import com.k_int.web.toolkit.refdata.RefdataValue;

import grails.gorm.MultiTenant;

/**
 * This class represents what will happen on completion of an action / event
 */

class ActionEventResult implements MultiTenant<ActionEventResult> {

    /** The id of the record */
    String id;

    /** The code we have given to this result */
    String code;

    /** A description of what this action / event represents */
    String description;

    /** The result of of the event / action, success = true, error  = failed */
    Boolean result;

    /** A qualifier for the result that may have been supplied */
    String qualifier;

    /** The status the request should change to, given the result and qualifier, if no status specified then it stays in the same state */
    Status status;

    /** Do we need to save or restore the status */
    RefdataValue saveRestoreState;

    /** Is there a follow on action or event to be executed, will be passed the same parameters passed to this action, so could have been modified ... */
    ActionEvent nextAactionEvent;

    static constraints = {
                    code (nullable: false, blank:false, unique: true)
             description (nullable: false, blank:false)
                  result (nullable: false)
               qualifier (nullable: true)
                  status (nullable: true)
        saveRestoreState (nullable: true)
        nextAactionEvent (nullable: true)
    }

    static mapping = {
                      id column : 'aer_id', generator: 'uuid2', length: 36
                 version column : 'aer_version'
                    code column : 'aer_code', length: 64
             description column : 'aer_description'
                  result column : 'aer_result'
               qualifier column : 'aer_qualifier', length: 64
                  status column : 'aer_status'
        saveRestoreState column : 'aer_save_restore_state'
        nextAactionEvent column : 'aer_next_action_event'
    }

    public static ActionEventResult ensure(String code, String description, boolean result, Status status = null, String qualifier = null, String saveRestoreState = null, ActionEvent nextAactionEvent = null) {
        // Lookup to see if the code exists
        ActionEventResult actionEventResult = findByCode(code);

        // Did we find it
        if (actionEventResult == null) {
            // No we did not, so create a new one
            actionEventResult = new ActionEventResult (
                code: code,
                description: description,
                result: result,
                qualifier: qualifier,
                status: status,
                saveRestoreState: saveRestoreState,
                nextAactionEvent: nextAactionEvent
            );

            // and save it
            actionEventResult.save(flush:true, failOnError:true);
        }

        // Return the result to the caller
        return(actionEventResult);
    }
}


