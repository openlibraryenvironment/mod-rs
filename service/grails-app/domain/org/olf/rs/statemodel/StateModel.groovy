package org.olf.rs.statemodel

import grails.gorm.MultiTenant;

/**
 * Defines a state model that defines the flow for a request
 * @author Chas
 *
 */
class StateModel implements MultiTenant<StateModel> {

    public static final String MODEL_REQUESTER = 'PatronRequest';
    public static final String MODEL_RESPONDER = 'Responder';

    /** The query to find the states that are for a particular stage for a state model */
    private static final String STATES_FOR_STAGES_QUERY = """
from Status as s
where s in (select sms.state
            from StateModel as sm
                inner join sm.states as sms
            where sm.shortcode = :stateModelCode and
                  sms.state.stage in (:stages))
""";

    /** The query to find the terminal states for a state model */
    private static final String NON_TERMINAL_STATES_QUERY = """
from Status as s
where s in (select sms.state
            from StateModel as sm
                inner join sm.states as sms
            where sm.shortcode = :stateModelCode and
                  sms.isTerminal = false)
""";

    /** The query to find all states for a state model */
    private static final String ALL_STATES_QUERY = """
from Status as s
where s in (select sms.state
            from StateModel as sm
                inner join sm.states as sms
            where sm.shortcode = :stateModelCode)
""";

    String id;
    String shortcode;
    String name;

    /** The initial state for this model */
    Status initialState;

    /** Action to perform when a request becomes stale */
    ActionEvent staleAction;

    /** The status to set the request to when when a request is overdue */
    Status overdueStatus;

    static hasMany = [
        // The states that are applicable for this model and their specific settings
        states : StateModelStatus
    ];

    static constraints = {
            shortcode (nullable: false, blank: false)
                 name (nullable: true, blank: false)
         initialState (nullable: true)
          staleAction (nullable: true)
        overdueStatus (nullable: true)
    }

    static mapping = {
                   id column: 'sm_id', generator: 'uuid2', length:36
              version column: 'sm_version'
            shortcode column: 'sm_shortcode'
                 name column: 'sm_name'
         initialState column: 'sm_initial_state'
          staleAction column: 'sm_stale_action'
        overdueStatus column: 'sm_overdue_status'
               states cascade: 'all-delete-orphan'
    }

    static public StateModel ensure(
        String code,
        String name = null,
        String initialStateCode = null,
        String staleActionCode = null,
        String overdueStatusCode = null,
        List states = null
    ) {
        // Does this state model already exist
        StateModel stateModel = lookup(code);
        if (stateModel == null) {
            // Dosn't exist so create a new one
            stateModel = new StateModel(shortcode: code);
        }

        // Now update all the fields in case they have changed
        stateModel.initialState = Status.lookup(initialStateCode);
        stateModel.staleAction = ActionEvent.lookup(staleActionCode);
        stateModel.overdueStatus = Status.lookup(overdueStatusCode);
        stateModel.ensureStates(states);

        // Save the state model, if any changes have been made
        stateModel.save(flush: true, failOnError: true);

        // Return the state model to the caller
        return(stateModel);
    }

    private void ensureStates(List suppliedStates) {
        // We create ourselves a working list as we want to modify as we process it
        List workingStates = ((suppliedStates == null) ? [ ] : suppliedStates.collect());

        // Go through removing the items that no longer need to be there
        if (states.size() > 0) {
            // Process all the current records
            states.collect().each { state ->
                // now look to see if it is in the working list
                Map foundState = workingStates.find { workingState -> workingState.status.equals(state.state.code) };
                if (foundState == null) {
                    // no longer required so remove from the database
                    removeFromStates(state);
                } else {
                    // Remove it from the working states as it is already in the database
                    int indexToRemove = workingStates.findIndexOf{ workingState -> foundState.status.equals(workingState.status) };
                    workingStates.remove(indexToRemove);

                    // Just update the additional fields, but we need to do it to the already saved record
                    states.find { realState ->
                        if (realState.state.code.equals(state.state.code)) {
//                            realState.canTriggerStaleRequest = foundState.canTriggerStaleRequest;
//                            realState.canTriggerOverdueRequest = foundState.canTriggerOverdueRequest;
//                            realState.isTerminal = foundState.isTerminal;
                            updateState(realState, foundState.canTriggerStaleRequest, foundState.canTriggerOverdueRequest, foundState.isTerminal);
                            return(true);
                        }
                        return(false);
                    };
                }
            }
        }

        // if There is anything left in workingStates then they need adding to the database
        workingStates.each{ workingState ->
            // Create a new StateModelState record
            StateModelStatus state = new StateModelStatus();
            state.stateModel = this;
            state.state = Status.lookup(workingState.status);
            if (state.state != null) {
                // We have a status, now set the additional fields
                updateState(state, workingState.canTriggerStaleRequest, workingState.canTriggerOverdueRequest, workingState.isTerminal);

                // Now add the state as its not already there
                addToStates(state);
            }
        }
    }

    private void updateState(
        StateModelStatus state,
        Boolean canTriggerStaleRequest,
        Boolean canTriggerOverdueRequest,
        Boolean isTerminal
    ) {
        // DOT NOT move this method to the StateModelStatus domain as it will not work for updates and I have no idea why it dosn't spent far to long trying to make it work
        // Update the fields on the state record
        state.canTriggerStaleRequest = canTriggerStaleRequest == null ? Boolean.FALSE : canTriggerStaleRequest;
        state.canTriggerOverdueRequest = canTriggerOverdueRequest == null ? Boolean.FALSE : canTriggerOverdueRequest;
        state.isTerminal = isTerminal == null ? Boolean.FALSE : isTerminal;
        // We do not need to perform a save here as it is implicitlt done by the parent
    }

    static public StateModel lookup(String code) {
        return(StateModel.findByShortcode(code));
    }

    public static List<Status> getActiveStates(String modelCode) {
        List<Status> result = null;

        if (modelCode != null) {
            // Just lookup the statuses that have the appropriate stage for this model
            result = Status.findAll(STATES_FOR_STAGES_QUERY, [ stateModelCode : modelCode, stages : [ StatusStage.ACTIVE, StatusStage.ACTIVE_SHIPPED ]]);
        }

        // Return an empty array instead of null
        if (result == null) {
            result = new ArrayList<Status>();
        }
        return(result);
    }

    public static List<Status> getNonTerminalStates(String modelCode) {
        List<Status> result = null;

        if (modelCode != null) {
            // Just lookup the statuses that have the appropriate stage for this model
            result = Status.findAll(NON_TERMINAL_STATES_QUERY, [ stateModelCode : modelCode ]);
        }

        // Return an empty array instead of null
        if (result == null) {
            result = new ArrayList<Status>();
        }

        return(result);
    }

    public static List<Status> getAllStates(String modelCode) {
        List<Status> result = null;

        if (modelCode != null) {
            // Just lookup the statuses that have the appropriate stage for this model
            result = Status.findAll(ALL_STATES_QUERY, [ stateModelCode : modelCode ]);
        }

        // Return an empty array instead of null
        if (result == null) {
            result = new ArrayList<Status>();
        }

        return(result);
    }
}
