package org.olf.rs.statemodel;

/**
 * Service class for the StateModel
 */
public class StateModelService {

    /**
     * Returns the supplied state model for export purposes
     * @param stateModel The state model that needs to be built
     * @return The full state model
     */
    public Map exportStateModel(StateModel stateModel) {
        Map result = [ : ];

        // The basic for the state model
        result.code = stateModel.shortcode;
        result.name = stateModel.name;
        result.initialState = stateModel.initialState?.code;
        result.staleAction = stateModel.staleAction?.code;
        result.overdueDtatus = stateModel.overdueStatus?.code;

        // Now lets run through all the states
        List states = [];
        result.stati = states;
        if (stateModel.states) {
            stateModel.states.each { status ->
                Map state = [ : ];
                states.add(state);

                // The basics for this status
                state.state = status.state.code;
                state.canTriggerStaleRequest = status.canTriggerStaleRequest;
                state.canTriggerOverdueRequest = status.canTriggerOverdueRequest;
                state.isTerminal = status.isTerminal;
                state.triggerPullSlipEmail = status.triggerPullSlipEmail;

                // Now deal with the available actions for this state
                List availableActions = [ ];
                state.availableActions = availableActions;

                def statusAvailableActions = stateModel.availableActions.findAll { availableAction ->
                    availableAction.fromState == status.state
                };

                // Did we find any available actions
                if (statusAvailableActions) {
                    statusAvailableActions.each { availableAction ->
                        Map availableActionResult = [ : ];
                        availableActions.add(availableActionResult);

                        // Basics for the available action
                        availableActionResult.actionEvent = availableAction.actionEvent?.code;
                        availableActionResult.triggerType = availableAction.triggerType;
                        availableActionResult.actionType = availableAction.actionType;
                        availableActionResult.actionBody = availableAction.actionBody;
                        availableActionResult.resultList = availableAction.resultList?.code;
                    }
                }
            }
        }

        // return the result to the caller
        return(result);
    }

    /**
     * Returns the full list of actions or events for export purposes
     * @param wantActions if true only returns the actions otherwise it only return events
     * @return The full list of actions or events
     */
    public List exportActionEvents(Boolean wantActions) {
        List result = [ ];

        ActionEvent.findAll().each { actionEvent ->
            // Is this a record we are interested in
            if (actionEvent.isAction == wantActions) {
                Map actionEventResult = [ : ];
                result.add(actionEventResult);

                // The basic for the action / event
                actionEventResult.code = actionEvent.code;
                actionEventResult.description = actionEvent.description;
                actionEventResult.undoStatus = actionEvent.undoStatus?.toString();
                actionEventResult.resultList = actionEvent.resultList?.code;
                actionEventResult.serviceClass = actionEvent.serviceClass;
                actionEventResult.responderServiceClass = actionEvent.responderServiceClass;
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Creates a list of all the stati for export purposes
     * @return The full list of stati
     */
    public List exportStati() {
        List result = [ ];

        Status.findAll().each { status ->
            Map statusResult = [ : ];
            result.add(statusResult);

            // The basic for the status
            statusResult.code = status.code;
            statusResult.presSeq = status.presSeq;
            statusResult.visible = status.visible;
            statusResult.needsAttention = status.needsAttention;
            statusResult.terminal = status.terminal;
            statusResult.stage = status.stage?.toString();
            statusResult.terminalSequence = status.terminalSequence;
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Creates a list of all the action event results for export purposes
     * @return The full list of action event results
     */
    public List exportActionEventResults() {
        List result = [ ];

        ActionEventResult.findAll().each { actionEventResult ->
            Map actionEventResultResult = [ : ];
            result.add(actionEventResultResult);

            // The basic for the action event result
            actionEventResultResult.code = actionEventResult.code;
            actionEventResultResult.description = actionEventResult.description;
            actionEventResultResult.result = actionEventResult.result;
            actionEventResultResult.qualifier = actionEventResult.qualifier;
            actionEventResultResult.status = actionEventResult.status?.code;
            actionEventResultResult.saveRestoreState = actionEventResult.saveRestoreState?.value;
            actionEventResultResult.overrideSaveStatus = actionEventResult.overrideSaveStatus?.code;
            actionEventResultResult.fromStatus = actionEventResult.fromStatus?.code;
            actionEventResultResult.nextActionEvent = actionEventResult.nextActionEvent?.code;
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Creates a list of all the action event result lists for export purposes
     * @return The full list of action event result lists
     */
    public List exportActionEventResultLists() {
        List result = [ ];

        ActionEventResultList.findAll().each { actionEventResultList ->
            Map actionEventResultListResult = [ : ];
            result.add(actionEventResultListResult);

            // The basic for the action event result list
            actionEventResultListResult.code = actionEventResultList.code;
            actionEventResultListResult.description = actionEventResultList.description;

            // The results that make up this list
            if (actionEventResultList.results) {
                List actionEventResults = [ ];
                actionEventResultListResult.actionEventResults = actionEventResults;

                // Now loop through all the results
                actionEventResultList.results.each { actionEventResult ->
                    Map actionEventResultResult = [ : ];
                    actionEventResults.add(actionEventResultResult);
                    actionEventResultResult.code = actionEventResult.code;
                }
            }
        }

        // Return the result to the caller
        return(result);
    }
}
