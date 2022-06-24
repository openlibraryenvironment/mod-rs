package org.olf.rs.statemodel;

import grails.gorm.MultiTenant;

/**
 * This class represents the actions and events that can occur in the system.
 * The difference between an action and an event is that an action occurs immediately where am event is queued,
 * so the result of an event is not returned to the caller
 */

class ActionEvent implements MultiTenant<ActionEvent> {

    // Query to find all the events that have a result list that changes the state
    private static final String EVENTS_CHANGE_STATUS_QUERY = 'from ActionEvent ae where isAction = false and exists (from ae.resultList.results r where r.status is not null)';

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

    /** can this action / event be undone */
    UndoStatus undoStatus;

    static constraints = {
               code (nullable: false, blank:false, unique: true)
        description (nullable: false, blank:false)
           isAction (nullable: false)
         resultList (nullable: true)
         undoStatus (nullable: true)
    }

    static mapping = {
                 id column: 'ae_id', generator: 'uuid2', length: 36
            version column: 'ae_version'
               code column: 'ae_code', length: 64
        description column: 'ae_description'
           isAction column: 'ae_is_action'
         resultList column: 'ae_result_list'
         undoStatus column: 'ae_undo_status', length: 20
    }

    public static ActionEvent ensure(
        String code,
        String description,
        boolean isAction,
        String resultListCode,
        UndoStatus undoStatus = UndoStatus.NO
    ) {
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
        actionEvent.undoStatus = undoStatus;

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

    public static List<ActionEvent> getEventsThatChangeStatus() {
        return(findAll(EVENTS_CHANGE_STATUS_QUERY));
    }
}
