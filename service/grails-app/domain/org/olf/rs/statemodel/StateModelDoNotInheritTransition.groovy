package org.olf.rs.statemodel

import grails.gorm.MultiTenant;

/**
 * Specifies which states / actions / events should not be inherited to the state model
 *
 * @author Chas
 *
 */
class StateModelDoNotInheritTransition implements MultiTenant<StateModelDoNotInheritTransition> {

    /** The id for the record */
    String id;

    /** The state model we do not want to inherit the combination of sate / action / event to */
    StateModel stateModel;

    /** The state that is not to be inherited in combination with the ActionEvent, if null  */
    Status state;

    /** The action / event not to be inherited in combination with the status, if null then all action / events */
    ActionEvent actionEvent;

    static belongsTo = [ stateModel: StateModel ];

    static constraints = {
         stateModel (nullable: false)
              state (nullable: true)
        actionEvent (nullable: true)
    }

    static mapping = {
                 id column : 'smdnit_id', generator: 'uuid2', length:36
            version column : 'smdnit_version'
         stateModel column : 'smdnit_state_model'
              state column : 'smdnit_state'
        actionEvent column : 'smdnit_action_event'
    }
}
