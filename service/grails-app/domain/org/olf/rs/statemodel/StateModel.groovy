package org.olf.rs.statemodel

import grails.gorm.MultiTenant;

/**
 * Defines a state model that defines the flow for a request
 * @author Chas
 *
 */
class StateModel implements MultiTenant<StateModel> {

    public static final String MODEL_REQUESTER = 'PatronRequest';
    public static final String MODEL_DIGITAL_RETURNABLE_REQUESTER = 'DigitalReturnableRequester';
    public static final String MODEL_RESPONDER = 'Responder';
    public static final String MODEL_CDL_RESPONDER = 'CDLResponder';

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

    /** The query to find the terminal states for a state model */
    private static final String VISIBLE_TERMINAL_STATES_QUERY = """
from Status as s
where s in (select sms.state
            from StateModel as sm
                inner join sm.states as sms
            where sm.shortcode = :stateModelCode and
                  sms.isTerminal = true and
                  sms.state.visible = true)
order by s.terminalSequence
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

    /** Action to perform when we automatically mark the pick list as printed */
    ActionEvent pickSlipPrintedAction;

    static hasMany = [
        /** The states that are applicable for this model and their specific settings */
        states : StateModelStatus,

        /** The state models it inherits transitions from */
        inheritedStateModels : StateModelInheritsFrom,

        /** The available actions that are applicable to this state model */
        availableActions : AvailableAction,

        /** The transitions we do want to inherit from */
        doNotInheritTransitions : StateModelDoNotInheritTransition
    ];

    static mappedBy = [
         inheritedStateModels : 'stateModel',
         doNotInheritTransitions : 'stateModel'
    ];

    static constraints = {
                    shortcode (nullable: false, blank: false)
                         name (nullable: true, blank: false)
                 initialState (nullable: true)
                  staleAction (nullable: true)
                overdueStatus (nullable: true)
        pickSlipPrintedAction (nullable: true)
    }

    static mapping = {
                             id column: 'sm_id', generator: 'uuid2', length:36
                        version column: 'sm_version'
                      shortcode column: 'sm_shortcode'
                           name column: 'sm_name'
                   initialState column: 'sm_initial_state'
                    staleAction column: 'sm_stale_action'
                  overdueStatus column: 'sm_overdue_status'
          pickSlipPrintedAction column: 'sm_pick_slip_printed_action'
                         states cascade: 'all-delete-orphan'
           inheritedStateModels cascade: 'all-delete-orphan', sort: 'priority' // Guarantees the set is sorted by priority
               availableActions cascade: 'all-delete-orphan'
        doNotInheritTransitions cascade: 'all-delete-orphan'
    }

    static public StateModel ensure(
        String code,
        String name = null,
        String initialStateCode = null,
        String staleActionCode = null,
        String overdueStatusCode = null,
        String pickSlipPrintedAction = null,
        List states = null,
        List inheritedStateModels = null,
        List doNotInheritTransitions = null
    ) {
        // Does this state model already exist
        StateModel stateModel = lookup(code);
        if (stateModel == null) {
            // Dosn't exist so create a new one
            stateModel = new StateModel(shortcode: code);
        }

        // Now update all the fields in case they have changed
        stateModel.name = name;
        stateModel.initialState = Status.lookup(initialStateCode);
        stateModel.staleAction = ActionEvent.lookup(staleActionCode);
        stateModel.overdueStatus = Status.lookup(overdueStatusCode);
        stateModel.pickSlipPrintedAction = ActionEvent.lookup(pickSlipPrintedAction);
        stateModel.ensureStates(states);
        stateModel.ensureInheritedStateModels(inheritedStateModels);
        stateModel.ensureDoNotInheritTransitions(doNotInheritTransitions);

        // Save the state model, if any changes have been made
        stateModel.save(flush: true, failOnError: true);

        // Return the state model to the caller
        return(stateModel);
    }

    private void ensureInheritedStateModels(List suppliedInheritedStateModels) {
        // We create ourselves a working list as we want to modify as we process it
        List working = ((suppliedInheritedStateModels == null) ? [ ] : suppliedInheritedStateModels.collect());

        // Go through removing the items that no longer need to be there
        if ((inheritedStateModels != null) && (inheritedStateModels.size() > 0)) {
            // Process all the current records
            inheritedStateModels.collect().each { inheritedStateModel ->
                // now look to see if it is in the working list
                Map foundInheritedStateModel = working.find { workingInheritFrom -> workingInheritFrom.stateModel.equals(inheritedStateModel.inheritedStateModel.shortcode) };
                if (foundInheritedStateModel == null) {
                    // no longer required so remove from the database
                    // Failed miserably to get removeFrom to work so just deleting the record directly, need to remove it from the set first
                    inheritedStateModels.remove(inheritedStateModel);
                    inheritedStateModel.delete();
                    //removeFromInheritedStateModels(inheritedStateModel);
                } else {
                    // Remove it from the working inheritsFrom as it is already in the database
                    int indexToRemove = working.findIndexOf{ workingInheritFrom -> foundInheritedStateModel.stateModel.equals(workingInheritFrom.stateModel) };
                    working.remove(indexToRemove);

                    // Just update the additional fields, but we need to do it to the already saved record
                    inheritedStateModels.find { realInheritFrom ->
                        if (realInheritFrom.inheritedStateModel.shortcode.equals(inheritedStateModel.inheritedStateModel.shortcode)) {
                            realInheritFrom.priority = foundInheritedStateModel.priority;
                            return(true);
                        }
                        return(false);
                    };
                }
            }
        }

        // if There is anything left in working then they need adding to the database
        working.each{ workingInheritFrom ->
            // Create a new StateModelState record
            StateModelInheritsFrom stateModelInheritsFrom = new StateModelInheritsFrom();
            stateModelInheritsFrom.stateModel = this;
            stateModelInheritsFrom.inheritedStateModel = lookup(workingInheritFrom.stateModel);
            if (stateModelInheritsFrom.inheritedStateModel != null) {
                // We have a state model, now set the additional fields
                stateModelInheritsFrom.priority = workingInheritFrom.priority;

                // Now add the state as its not already there
                addToInheritedStateModels(stateModelInheritsFrom);
            }
        }
    }

    private void ensureDoNotInheritTransitions(List suppliedDoNotInheritTransitions) {
        // We create ourselves a working list as we want to modify as we process it
        List working = ((suppliedDoNotInheritTransitions == null) ? [ ] : suppliedDoNotInheritTransitions.collect());

        // Go through removing the items that no longer need to be there
        if ((doNotInheritTransitions != null) && (doNotInheritTransitions.size() > 0)) {
            // Process all the current records
            doNotInheritTransitions.collect().each { StateModelDoNotInheritTransition doNotInheritTransition ->
                // now look to see if it is in the working list
                Map foundDoNotInheritTransition = working.find { workingDoNotInheritTransition ->
                    return(((workingDoNotInheritTransition.actionEvent == null &&
                             doNotInheritTransition.actionEvent == null) ||
                            (workingDoNotInheritTransition.actionEvent != null &&
                             workingDoNotInheritTransition.actionEvent.equals(doNotInheritTransition.actionEvent?.code))) &&
                           ((workingDoNotInheritTransition.state == null &&
                             doNotInheritTransition.state == null) ||
                            (workingDoNotInheritTransition.state != null &&
                             workingDoNotInheritTransition.state.equals(doNotInheritTransition.state?.code))));
                };
                if (foundDoNotInheritTransition == null) {
                    // no longer required so remove from the database
                    // Failed miserably to get removeFrom to work so just deleting the record directly, need to remove it from the set first
                    doNotInheritTransitions.remove(doNotInheritTransition);
                    doNotInheritTransition.delete();
                } else {
                    // Remove it from the working inheritsFrom as it is already in the database
                    int indexToRemove = working.findIndexOf{ workingDoNotInheritTransition ->
                        return(((workingDoNotInheritTransition.actionEvent == null &&
                                 foundDoNotInheritTransition.actionEvent == null) ||
                                (workingDoNotInheritTransition.actionEvent != null &&
                                 workingDoNotInheritTransition.actionEvent.equals(foundDoNotInheritTransition.actionEvent))) &&
                               ((workingDoNotInheritTransition.state == null &&
                                 foundDoNotInheritTransition.state == null) ||
                                (workingDoNotInheritTransition.state != null &&
                                 workingDoNotInheritTransition.state.equals(foundDoNotInheritTransition.state))));
                    };
                    working.remove(indexToRemove);
                }
            }
        }

        // if There is anything left in working then they need adding to the database
        working.each{ workingDoNotInheritTransition ->
            // Create a new StateModelDoNotInheritTransition record
            StateModelDoNotInheritTransition stateModelDoNotInheritTransition = new StateModelDoNotInheritTransition();
            stateModelDoNotInheritTransition.stateModel = this;
            stateModelDoNotInheritTransition.actionEvent = ActionEvent.lookup(workingDoNotInheritTransition.actionEvent);
            stateModelDoNotInheritTransition.state = Status.lookup(workingDoNotInheritTransition.state);

            // Now add it to the state model
            addToDoNotInheritTransitions(stateModelDoNotInheritTransition);
        }
    }

    private void ensureStates(List suppliedStates) {
        // We create ourselves a working list as we want to modify as we process it
        List workingStates = ((suppliedStates == null) ? [ ] : suppliedStates.collect());

        // Go through removing the items that no longer need to be there
        if ((states != null) && (states.size() > 0)) {
            // Process all the current records
            states.collect().each { state ->
                // now look to see if it is in the working list
                Map foundState = workingStates.find { workingState -> workingState.status.equals(state.state.code) };
                if (foundState == null) {
                    // no longer required so remove from the database
                    // Failed miserably to get removeFrom to work so just deleting the record directly, need to remove it from the set first
                    states.remove(state);
                    state.delete();
                    //removeFromStates(state);
                } else {
                    // Remove it from the working states as it is already in the database
                    int indexToRemove = workingStates.findIndexOf{ workingState -> foundState.status.equals(workingState.status) };
                    workingStates.remove(indexToRemove);

                    // Just update the additional fields, but we need to do it to the already saved record
                    states.find { realState ->
                        if (realState.state.code.equals(state.state.code)) {
                            updateState(realState, foundState.canTriggerStaleRequest, foundState.canTriggerOverdueRequest, foundState.isTerminal, foundState.triggerPullSlipEmail);
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
                updateState(state, workingState.canTriggerStaleRequest, workingState.canTriggerOverdueRequest, workingState.isTerminal, workingState.triggerPullSlipEmail);

                // Now add the state as its not already there
                addToStates(state);
            }
        }
    }

    private void updateState(
        StateModelStatus state,
        Boolean canTriggerStaleRequest,
        Boolean canTriggerOverdueRequest,
        Boolean isTerminal,
        Boolean triggerPullSlipEmail
    ) {
        // DO NOT move this method to the StateModelStatus domain as it will not work for updates and I have no idea why it dosn't spent far to long trying to make it work
        // Update the fields on the state record
        state.canTriggerStaleRequest = canTriggerStaleRequest == null ? false : canTriggerStaleRequest;
        state.canTriggerOverdueRequest = canTriggerOverdueRequest == null ? false : canTriggerOverdueRequest;
        state.isTerminal = isTerminal == null ? false : isTerminal;
        state.triggerPullSlipEmail = (triggerPullSlipEmail == null) ? false : triggerPullSlipEmail;
        // We do not need to perform a save here as it is implicitlt done by the parent
    }

    static public StateModel lookup(String code) {
        return(StateModel.findByShortcode(code));
    }

    public static List<Status> getActiveStates(String modelCode) {
        return(getStatesByStage(modelCode, [ StatusStage.ACTIVE, StatusStage.ACTIVE_PENDING_CONDITIONAL_ANSWER, StatusStage.ACTIVE_SHIPPED ]));
    }

    public static List<Status> getCompletedStates(String modelCode) {
        return(getStatesByStage(modelCode, [ StatusStage.COMPLETED ]));
    }

    public static List<Status> getStatesByStage(String modelCode, List<StatusStage> stages) {
        return(queryStates(modelCode, STATES_FOR_STAGES_QUERY, stages));
    }

    public static List<Status> getNonTerminalStates(String modelCode) {
        return(queryStates(modelCode, NON_TERMINAL_STATES_QUERY));
    }

    public static List<Status> getAllStates(String modelCode) {
        return(queryStates(modelCode, ALL_STATES_QUERY));
    }

    public static List<Status> getVisibleTerminalStates(String modelCode) {
        return(queryStates(modelCode, VISIBLE_TERMINAL_STATES_QUERY));
    }

    private static List<Status> queryStates(String modelCode, String query, List<StatusStage> stages = null) {
        List<Status> result = null;

        if (modelCode != null) {
            // Build up the parameter map, don't add stages if it is null, as that means the query is not expecting it
            Map parameters =  [ stateModelCode : modelCode ];
            if (stages != null) {
                parameters.put('stages', stages);
            }

            // Find all the states that match this query for the state model
            result = Status.findAll(query, parameters);
        }

        // Return an empty array instead of null
        if (result == null) {
            result = new ArrayList<Status>();
        }

        return(result);
    }
}
