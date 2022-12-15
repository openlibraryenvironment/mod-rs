package org.olf.rs.statemodel

import org.apache.commons.lang3.builder.HashCodeBuilder;

import grails.gorm.MultiTenant;

/**
 * Links the state model to a status, with specific settings for that state model
 *
 * @author Chas
 *
 */
class StateModelStatus implements Serializable, MultiTenant<StateModelStatus> {

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

    /** Does this state trigger pull slip emails */
    boolean triggerPullSlipEmail;

    static belongsTo = [ stateModel: StateModel ];

    static constraints = {
                      stateModel (nullable: false)
                           state (nullable: false)
          canTriggerStaleRequest (nullable: false)
        canTriggerOverdueRequest (nullable: false)
                      isTerminal (nullable: false)
            triggerPullSlipEmail (nullable: false)
    }

    static mapping = {
                              id composite : [ 'stateModel', 'state' ]
                         version column : 'sms_version'
                      stateModel column : 'sms_state_model'
                           state column : 'sms_state'
          canTriggerStaleRequest column : 'sms_can_trigger_stale_request'
        canTriggerOverdueRequest column : 'sms_can_trigger_overdue_request'
                      isTerminal column : 'sms_is_terminal'
            triggerPullSlipEmail column : 'sms_trigger_pull_slip_email', defaultValue : "false"
    }

    public boolean equals(other) {
        // If the object is not of the correct type then it can't be equal
        if (!(other instanceof StateModelStatus)) {
            return(false);
        }

        // So if the state and state model are the same
        return((other.stateModel.id == stateModel.id) && (other.state.id == state.id));
    }

    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(stateModel.id);
        builder.append(state.id);
        return(builder.toHashCode());
    }
}
