package org.olf.rs.statemodel

import org.apache.commons.lang3.builder.HashCodeBuilder;

import grails.gorm.MultiTenant;

/**
 * Links the state model to state models that it inherits transitions from
 *
 * @author Chas
 *
 */
class StateModelInheritsFrom implements Serializable, MultiTenant<StateModelInheritsFrom> {

    /** The state model that is inheriting the transitions */
    StateModel stateModel;

    /** The state model we are inheriting the transitions from */
    StateModel inheritedStateModel;

    /** The priority of this state model */
    int priority;

    static belongsTo = [ stateModel: StateModel ];

    static constraints = {
                 stateModel (nullable: false)
        inheritedStateModel (nullable: false)
    }

    static mapping = {
                         id composite : [ 'stateModel', 'inheritedStateModel' ]
                    version column : 'smif_version'
                 stateModel column : 'smif_state_model'
        inheritedStateModel column : 'smif_inherited_state_model'
                   priority column : 'smif_priority', defaultValue: 99
    }

    public boolean equals(other) {
        // If the object is not of the correct type then it can't be equal
        if (!(other instanceof StateModelInheritsFrom)) {
            return(false);
        }

        // So if the state and state model are the same
        return((other.stateModel.id == stateModel.id) && (other.inheritedStateModel.id == inheritedStateModel.id));
    }

    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(stateModel.id);
        builder.append(inheritedStateModel.id);
        return(builder.toHashCode());
    }
}
