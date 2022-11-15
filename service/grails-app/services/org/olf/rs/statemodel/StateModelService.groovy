package org.olf.rs.statemodel;

import org.grails.web.json.JSONArray;
import org.olf.rs.ReferenceDataService;
import org.olf.rs.referenceData.RefdataValueData;

import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * Service class for the StateModel
 */
public class StateModelService {

    ReferenceDataService referenceDataService;

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
        result.overdueStatus = stateModel.overdueStatus?.code;

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

    /**
     * Imports the supplied list of stati
     * @param stati The states to import
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importStati(JSONArray stati) {

        List messages = [ ];
        int okCount = 0;
        int errorCount = 0;

        // Have we been supplied any stati
        if (stati) {
            // We have been supplied some so, loop through them
            stati.each { status ->
                if (status.code) {
                    StatusStage stage = convertStatusStage(status.stage);
                    if (stage) {
                        try {
                            // We have been supplied a valid stage so try and save the status
                            if (Status.ensure(
                                status.code,
                                stage,
                                status.presSeq,
                                status.visible,
                                status.needsAttention,
                                status.terminal,
                                status.terminalSequence) == null) {
                                // Failed to create / update
                                messages.add("Failed to create / update status with code: \"" + status.code + "\"");
                                errorCount++;
                            } else {
                                // Created / Updated without errors
                                okCount++;
                            }
                        } catch (Exception e) {
                            String message = "Exception thrown adding status with code: \"" + status.code + "\"";
                            messages.add(message + ", Exception: " + e.message);
                            errorCount++;
                            log.error(message, e);
                        }
                    } else {
                        // Invalid stage specified for status
                        messages.add("An invalid stage was specified for status with code: \"" + status.code + "\"");
                        errorCount++;
                    }
                } else {
                    // No code specified for status
                    messages.add("No No code specified for status");
                    errorCount++;
                }
            }
        } else {
            // No status supplied, so just give a warning message
            messages.add("No status supplied");
        }

        // Give a summary of what we managed to do
        messages.add("Number of states imported: " + okCount.toString() + ", errors: " + errorCount.toString());

        // Return the messages
        return(messages);
    }

    /**
     * Converts a textual stage into the enum StatusStage
     * @param stage The textual stage
     * @return The StatusStage that the passed in text maps onto
     */
    private StatusStage convertStatusStage(String stage) {
        StatusStage statusStage = null;
        if (stage) {
            try {
                statusStage = StatusStage.valueOf(stage.toUpperCase());
            } catch (Exception e) {
                // We let the error be taken into account outside of this method, by the fact we return null
            }
        }
        return(statusStage);
    }

    /**
     * Imports the supplied list of action event results
     * @param actionEventResults The action event results to import
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importActionEventResults(JSONArray actionEventResults) {

        List messages = [ ];
        int okCount = 0;
        int errorCount = 0;

        // Have we been supplied any action / event results
        if (actionEventResults) {
            // We have been supplied some so, loop through them
            actionEventResults.each { actionEventResult ->
                if (actionEventResult.code) {
                    try {
                        // Lookup the restore state
                        RefdataValue saveRestoreState = referenceDataService.lookup(RefdataValueData.VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, actionEventResult.saveRestoreState);

                        // Now create / update this result
                        if (ActionEventResult.ensure(
                            actionEventResult.code,
                            actionEventResult.description,
                            actionEventResult.result,
                            Status.lookup(actionEventResult.status),
                            actionEventResult.qualifier,
                            saveRestoreState,
                            Status.lookup(actionEventResult.overrideSaveStatus),
                            Status.lookup(actionEventResult.fromStatus),
                            actionEventResult.nextActionEvent) == null) {
                            // Failed to create / update
                            messages.add("Failed to create / update action / event result with code: \"" + actionEventResult.code + "\"");
                            errorCount++;
                        } else {
                            // Created / Updated without errors
                            okCount++;
                        }
                    } catch (Exception e) {
                        String message = "Exception thrown adding action / event result with code: \"" + actionEventResult.code + "\"";
                        messages.add(message + ", Exception: " + e.message);
                        errorCount++;
                        log.error(message, e);
                    }
                } else {
                    // No code specified for the action /event result
                    messages.add("No code specified for action /event result");
                    errorCount++;
                }
            }
        } else {
            // No action / event result supplied, so just give a message
            messages.add("No action / event result supplied");
        }

        // Give a summary of what we managed to do
        messages.add("Number of action / event results imported: " + okCount.toString() + ", errors: " + errorCount.toString());

        // Return the messages
        return(messages);
    }

    /**
     * Imports the supplied list of action event result lists
     * @param actionEventResultLists The action event result lists to import
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importActionEventResultLists(JSONArray actionEventResultLists) {

        List messages = [ ];
        int okCount = 0;
        int errorCount = 0;

        // Have we been supplied any action / event result lists
        if (actionEventResultLists) {
            // We have been supplied some so, loop through them
            actionEventResultLists.each { actionEventResultList ->
                if (actionEventResultList.code) {
                    try {
                        List<ActionEventResult> resultItems = new ArrayList<ActionEventResult>();

                        // Have we been supplied with any action event results
                        if (actionEventResultList.actionEventResults) {
                            actionEventResultList.actionEventResults.each { actionEventResult ->
                                ActionEventResult resultItem = ActionEventResult.lookup(actionEventResult.code);
                                if (resultItem) {
                                    // IT exists, so add itto the result list
                                    resultItems.add(resultItem);
                                } else {
                                    messages.add("Unable to find action event result \"" + actionEventResult.code + "\" foe action event result list with code: \"" + actionEventResultList.code + "\"");
                                }
                            }
                        } else {
                            // Not an error, just an empty list
                            messages.add("An empty list has been supplied for action / event result list with code: \"" + actionEventResultList.code + "\"");
                        }

                        // Now create / update this result
                        if (ActionEventResultList.ensure(
                            actionEventResultList.code,
                            actionEventResultList.description,
                            resultItems) == null) {
                            // Failed to create / update
                            messages.add("Failed to create / update action / event result list with code: \"" + actionEventResultList.code + "\"");
                            errorCount++;
                        } else {
                            // Created / Updated without errors
                            okCount++;
                        }
                    } catch (Exception e) {
                        String message = "Exception thrown adding action / event result list with code: \"" + actionEventResultList.code + "\"";
                        messages.add(message + ", Exception: " + e.message);
                        errorCount++;
                        log.error(message, e);
                    }
                } else {
                    // No code specified for the action /event result list
                    messages.add("No code specified for action /event result list");
                    errorCount++;
                }
            }
        } else {
            // No action / event result list supplied, so just give a message
            messages.add("No action / event result list supplied");
        }

        // Give a summary of what we managed to do
        messages.add("Number of action / event result lists imported: " + okCount.toString() + ", errors: " + errorCount.toString());

        // Return the messages
        return(messages);
    }

    /**
     * Imports the supplied list of actions
     * @param actionEvents The action events to import
     * @param isAction true if we adding actions, false if we are adding events
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importActionEvents(JSONArray actionEvents, boolean isAction) {

        String actionEventType = isAction ? "action" : "event";
        List messages = [ ];
        int okCount = 0;
        int errorCount = 0;

        // Have we been supplied any actions / events
        if (actionEvents) {
            // We have been supplied some so, loop through them
            actionEvents.each { actionEvent ->
                if (actionEvent.code) {
                    try {
                        // Now create / update this action / event
                        if (ActionEvent.ensure(
                            actionEvent.code,
                            actionEvent.description,
                            isAction,
                            actionEvent. serviceClass,
                            actionEvent.resultList,
                            convertUndoStatus(actionEvent.undoStatus),
                            actionEvent.responderServiceClass) == null) {
                            // Failed to create / update
                            messages.add("Failed to create / update " + actionEventType + " with code: \"" + actionEvent.code + "\"");
                            errorCount++;
                        } else {
                            // Created / Updated without errors
                            okCount++;
                        }
                    } catch (Exception e) {
                        String message = "Exception thrown adding " + actionEventType + " with code: \"" + actionEvent.code + "\"";
                        messages.add(message + ", Exception: " + e.message);
                        errorCount++;
                        log.error(message, e);
                    }
                } else {
                    // No code specified for the action / event
                    messages.add("No code specified for " + actionEventType);
                    errorCount++;
                }
            }
        } else {
            // No actions supplied, so just give a message
            messages.add("No " + actionEventType + "s supplied");
        }

        // Give a summary of what we managed to do
        messages.add("Number of " + actionEventType + "s imported: " + okCount.toString() + ", errors: " + errorCount.toString());

        // Return the messages
        return(messages);
    }

    /**
     * Converts a textual undo status into the enum UndoStatus
     * @param status The textual undo status
     * @return The UndoStatus that the passed in text maps onto
     */
    private UndoStatus convertUndoStatus(String status) {
        UndoStatus undoStatus = null;
        if (status) {
            try {
                undoStatus = UndoStatus.valueOf(status.toUpperCase());
            } catch (Exception e) {
                // We let the error be taken into account outside of this method, by the fact we return null
            }
        }
        return(undoStatus);
    }

    /**
     * Imports the supplied list of state models
     * @param stateModels The state models to import
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importStateModels(JSONArray stateModels) {

        List messages = [ ];
        int okCount = 0;
        int errorCount = 0;

        // Have we been supplied any state models
        if (stateModels) {
            // We have been supplied some so, loop through them
            stateModels.each { jsonStateModel ->
                if (jsonStateModel.code) {
                    try {
                        List states = [ ];

                        // Have any states been specified dor this model
                        if (jsonStateModel.stati) {
                            // Loop through all the states, dealing with states specific to this model
                            jsonStateModel.stati.each { status ->
                                Map stateModelStatus = [ : ];
                                states.add(stateModelStatus);
                                stateModelStatus.status = status.state;
                                stateModelStatus.canTriggerOverdueRequest = status.canTriggerOverdueRequest;
                                stateModelStatus.canTriggerStaleRequest = status.canTriggerStaleRequest;
                                stateModelStatus.isTerminal = status.isTerminal;
                                stateModelStatus.triggerPullSlipEmail = status.triggerPullSlipEmail;
                            }
                        } else {
                            messages.add("No states specified for state model with code: \"" + jsonStateModel.code + "\"");
                        }

                        // Now create / update this state model
                        StateModel stateModel = StateModel.ensure(
                            jsonStateModel.code,
                            jsonStateModel.name = null,
                            jsonStateModel.initialState,
                            jsonStateModel.staleAction,
                            jsonStateModel.overdueStatus,
                            states);
                        if (stateModel == null) {
                            // Failed to create / update
                            messages.add("Failed to create / update state model with code: \"" + jsonStateModel.code + "\"");
                            errorCount++;
                        } else {
                            // Created / Updated without errors
                            okCount++;

                            // We now need to iterate through the available actions for the states in the model
                            if (jsonStateModel.stati) {
                                jsonStateModel.stati.each { status ->
                                    // We have separate counts for the available actions
                                    int okCountAvailableAction = 0;
                                    int errorCountAvailableAction = 0;

                                    if (status.availableActions) {
                                        // Now iterate through the available actions adding them
                                        status.availableActions.each { jsonAvailableAction ->
                                            if (AvailableAction.ensure(
                                                stateModel,
                                                status.state,
                                                jsonAvailableAction.actionEvent,
                                                jsonAvailableAction.triggerType,
                                                jsonAvailableAction.resultList) == null) {
                                                // Failed to create the available action
                                                errorCountAvailableAction++;
                                                messages.add("Failed to create available action for state model with code: \"" + jsonStateModel.code + "\" and status with code \"" + status.state + "\" for action / event with code \"" + jsonAvailableAction.actionEvent + "\"");
                                            } else {
                                                okCountAvailableAction++;
                                            }
                                        }

                                        // Add a message to let them know how many available actions were create
                                        messages.add("Number of available actions for state model with code: \"" + jsonStateModel.code + "\" and status with code \"" + status.state + "\" that have been imported: " + okCountAvailableAction.toString() + ", errors: " + errorCountAvailableAction.toString());
                                    } else {
                                        // If it is terminal then we may not have any available actions
                                        if (!status.isTerminal) {
                                            messages.add("No available actions specified for state model with code: \"" + jsonStateModel.code + "\" and status with code \"" + status.state + "\"");
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        String message = "Exception thrown adding state model with code: \"" + jsonStateModel.code + "\"";
                        messages.add(message + ", Exception: " + e.message);
                        log.error(message, e);
                        errorCount++;
                    }
                } else {
                    // No code specified for the state model
                    messages.add("No code specified for state model");
                    errorCount++;
                }
            }
        } else {
            // No state models supplied, so just give a message
            messages.add("No state models supplied");
        }

        // Give a summary of what we managed to do
        messages.add("Number of state models imported: " + okCount.toString() + ", errors: " + errorCount.toString());

        // Return the messages
        return(messages);
    }
}
