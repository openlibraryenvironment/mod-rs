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

    /** When we are set to save the status, this allows us to set it to a specific status instead of the current request status */
    Status overrideSaveStatus;

    /** Do we need to save or restore the status */
    RefdataValue saveRestoreState;

    /** The status the request must currently be in */
    Status fromStatus;

    /** Is there a follow on action or event to be executed, will be passed the same parameters passed to this action, so could have been modified ... */
    ActionEvent nextActionEvent;

    static constraints = {
                      code (nullable: false, blank:false, unique: true)
               description (nullable: false, blank:false)
                    result (nullable: false)
                 qualifier (nullable: true)
                    status (nullable: true)
          saveRestoreState (nullable: true)
        overrideSaveStatus (nullable: true)
                fromStatus (nullable: true)
           nextActionEvent (nullable: true)
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
        overrideSaveStatus column : 'aer_override_save_state'
                fromStatus column : 'aer_from_state'
           nextActionEvent column : 'aer_next_action_event'
    }

    public static ActionEventResult ensure(
        String code,
        String description,
        boolean result,
        Status status = null,
        String qualifier = null,
        RefdataValue saveRestoreState = null,
        Status overrideSaveStatus = null,
        Status fromStatus = null,
        ActionEvent nextActionEvent = null
    ) {
        // Lookup to see if the code exists
        ActionEventResult actionEventResult = findByCode(code);

        // Did we find it
        if (actionEventResult == null) {
            // No we did not, so create a new one
            actionEventResult = new ActionEventResult (
                code: code
            );
        }

        // They may have changed something
        actionEventResult.description = description;
        actionEventResult.result = result;
        actionEventResult.qualifier = qualifier;
        actionEventResult.status = status;
        actionEventResult.saveRestoreState = saveRestoreState;
        actionEventResult.overrideSaveStatus = overrideSaveStatus;
        actionEventResult.fromStatus = fromStatus;
        actionEventResult.nextActionEvent = nextActionEvent;

        // and save it
        actionEventResult.save(flush:true, failOnError:true);

        // Return the result to the caller
        return(actionEventResult);
    }
}
