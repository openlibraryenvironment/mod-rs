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
    Boolean canTriggerStaleRequest;

    /** Can this state trigger overdue functionality for a request */
    Boolean canTriggerOverdueRequest;

    /** Is this state considered a terminal state */
    Boolean isTerminal;

    static constraints = {
                      stateModel (nullable: false)
                           state (nullable: false)
          canTriggerStaleRequest (nullable: true)
        canTriggerOverdueRequest (nullable: true)
                      isTerminal (nullable: true)
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

    public void ensure(
        Boolean canTriggerStaleRequest,
        Boolean canTriggerOverdueRequest,
        Boolean isTerminal
    ) {
        this.canTriggerStaleRequest = canTriggerStaleRequest;
        this.canTriggerOverdueRequest = canTriggerOverdueRequest;
        this.isTerminal = isTerminal;

        // Save the record
//        save(flush: false, failOnError: true);
    }
}
