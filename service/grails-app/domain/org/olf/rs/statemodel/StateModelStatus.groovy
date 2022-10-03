package org.olf.rs.statemodel

import grails.gorm.MultiTenant;

/**
 * Links the state model to a status, with specific settings for that state model
 *
 * @author Chas
 *
 */
class StateModelStatus implements MultiTenant<StateModelStatus> {

    String id;

    /** The state model this state is associated with */
    StateModel stateModel;

    /** The state associated with the state model */
    Status state;

    /** Can this state trigger stale request functionality for a request */
    boolean canTriggerStaleRequest;

    /** Can this state trigger overdue functionality for a request */
    boolean canTriggerOverdueRequest;

    /** Is this state considered a terminal state */
    boolean isTerminal;

    static belongsTo = [ stateModel: StateModel ];

    static constraints = {
                      stateModel (nullable: false)
                           state (nullable: false)
          canTriggerStaleRequest (nullable: false)
        canTriggerOverdueRequest (nullable: false)
                      isTerminal (nullable: false)
    }

    static mapping = {
                              id column : 'sms_id', generator: 'uuid2', length:36
                         version column : 'sms_version'
                      stateModel column : 'sms_state_model'
                           state column : 'sms_state'
          canTriggerStaleRequest column : 'sms_can_trigger_stale_request'
        canTriggerOverdueRequest column : 'sms_can_trigger_overdue_request'
                      isTerminal column : 'sms_is_terminal'
    }
}
