package org.olf.rs.statemodel;

import grails.gorm.MultiTenant;

/**
 * This class represents the actions and events that can occur in the system.
 * The difference between an action and an event is that an action occurs immediately where am event is queued,
 * so the result of an event is not returned to the caller
 */

class ActionEvent implements MultiTenant<ActionEvent> {

    /** The id of the record */
    String id;

    /** The code this action / event is known by */
    String code;

    /** A description of what this action / event does */
    String description;

    /** Is this an action or event */
    Boolean isAction;

    /** The default set of results to use for this action / event */
    ActionEventResultList resultList;

    static constraints = {
               code (nullable: false, blank:false, unique: true)
        description (nullable: false, blank:false)
           isAction (nullable: false)
         resultList (nullable: true)
    }

    static mapping = {
                 id column: 'ae_id', generator: 'uuid2', length: 36
            version column: 'ae_version'
               code column: 'ae_code', length: 64
        description column: 'ae_description'
           isAction column: 'ae_is_action'
         resultList column: 'ae_result_list'
    }

    public static ActionEvent ensure(String code, String description, boolean isAction, String resultListCode) {
        // Lookup to see if the code exists
        ActionEvent actionEvent = findByCode(code);

        // Did we find it
        if (actionEvent == null) {
            // No we did not, so create a new one
            actionEvent = new ActionEvent (
                code: code
            );
        }

        // Just update the other fields as something may have changed
        actionEvent.description = description;
        actionEvent.isAction = isAction;
        actionEvent.resultList = ActionEventResultList.lookup(resultListCode);

        // and save it
        actionEvent.save(flush:true, failOnError:true);

        // Return the actionEvent to the caller
        return(actionEvent);
    }

    public static ActionEvent lookup(String code) {
        ActionEvent actionEvent = null;
        if (code != null) {
            actionEvent = findByCode(code);
        }
        return(actionEvent);
    }
}
