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
        result.pickSlipPrintedAction = stateModel.pickSlipPrintedAction?.code();

        // Have we inherited any state models
        if (stateModel.inheritedStateModels) {
            List inheritedStateModels = [ ];
            result.inheritedStateModels = inheritedStateModels;

            // We do so export the code and priority for each one that is inherited
            stateModel.inheritedStateModels.each { inheritedStateModel ->
                Map inheritedStateModelMap = [ : ];
                inheritedStateModels.add(inheritedStateModelMap);
                inheritedStateModelMap.code = inheritedStateModel.inheritedStateModel.shortcode;
                inheritedStateModelMap.priority = inheritedStateModel.priority;
            }
        }

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
                        availableActionResult.isAvailableGroovy = availableAction.isAvailableGroovy;
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
                actionEventResult.isAvailableGroovy = actionEvent.isAvailableGroovy;
                actionEventResult.isAvailableForBulk = actionEvent.isAvailableForBulk;
                actionEventResult.showInAuditTrail = actionEvent.showInAuditTrail;
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
            actionEventResultResult.saveRestoreState = actionEventResult.saveRestoreState?.label;
            actionEventResultResult.overrideSaveStatus = actionEventResult.overrideSaveStatus?.code;
            actionEventResultResult.fromStatus = actionEventResult.fromStatus?.code;
            actionEventResultResult.updateRotaLocation = actionEventResult.updateRotaLocation;
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
     * Generic domain import routine
     * @param items The domain items to be imported
     * @param domainText The text to appear in messages that represents the domain
     * @param createUpdate The closure that performs the create / update for the domain,
     *                     it is passed the json that represents a record and the messages list,
     *                     it returns true if successful or false if an error occurred
     * @param messages A message list that we will append, default: an empty list
     * @param itemCodeProperty The property on the item that represents the code for that item, default: code
     * @return The list of messages that were accumulated
     */
    private List importDomain(
        JSONArray items,
        String domainText,
        Closure createUpdate,
        List messages = null,
        String itemCodeProperty = null) {

        // If we have not been passed an itemCodeProperty default to "code"
        String codeProperty = itemCodeProperty ? itemCodeProperty : "code";

        // Do we need to allocate a new messages list or have we been supplied one
        List localMessages = (messages == null) ? [ ] : messages;
        int okCount = 0;
        int errorCount = 0;

        // Have we been supplied any items
        if (items) {
            // We have been supplied some so, loop through them
            items.each { item ->
                if (item[codeProperty]) {
                    try {
                        // Call the closure to attempt to create the item
                        if (createUpdate(item, localMessages)) {
                            // We were successful
                            okCount++;
                        } else {
                            // Failed to create / update
                            localMessages.add("Failed to create / update " + domainText + " with " + codeProperty + ": \"" + item[codeProperty] + "\"");
                            errorCount++;
                        }
                    } catch (Exception e) {
                        String message = "Exception thrown adding " + domainText + " with " + codeProperty + ": \"" + item[codeProperty] + "\"";
                        localMessages.add(message + ", Exception: " + e.message);
                        errorCount++;
                        log.error(message, e);
                    }
                } else {
                    // No code specified for item
                    localMessages.add("No " + codeProperty + " specified for " + domainText);
                    errorCount++;
                }
            }
        } else {
            // No array of items supplied, so just give a warning message
            localMessages.add("No array of " + domainText + " supplied");
        }

        // Give a summary of what we managed to do
        localMessages.add("Number of " + domainText + " imported: " + okCount.toString() + ", errors: " + errorCount.toString());

        // Return the messages
        return(localMessages);
    }

    /**
     * Imports the supplied list of stati
     * @param stati The states to import
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importStati(JSONArray stati) {
        // The closure ensure the status exists
        Closure createUpdate = { jsonStatus, statusMessages ->
            boolean result = false;
            StatusStage stage = convertStatusStage(jsonStatus.stage);
            if (stage) {
                // We have been supplied a valid stage so try and save the status
                result = (Status.ensure(
                    jsonStatus.code,
                    stage,
                    jsonStatus.presSeq,
                    jsonStatus.visible,
                    jsonStatus.needsAttention,
                    jsonStatus.terminal,
                    jsonStatus.terminalSequence) != null);
            }
            return(result);
        };

        // Call the generic import routine to do the work and return the messages
        return(importDomain(stati,  "status", createUpdate));
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

        // The closure ensure the action event result exists
        Closure createUpdate = { jsonActionEventResult, actionEventResultMessages ->
            // Lookup the restore state
            RefdataValue saveRestoreState = referenceDataService.lookup(RefdataValueData.VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, jsonActionEventResult.saveRestoreState);

            // Now create / update this result
            return(ActionEventResult.ensure(
                jsonActionEventResult.code,
                jsonActionEventResult.description,
                jsonActionEventResult.result,
                Status.lookup(jsonActionEventResult.status),
                jsonActionEventResult.qualifier,
                saveRestoreState,
                Status.lookup(jsonActionEventResult.overrideSaveStatus),
                Status.lookup(jsonActionEventResult.fromStatus),
                jsonActionEventResult.updateRotaLocation  == null ? false : jsonActionEventResult.updateRotaLocation,
                jsonActionEventResult.nextActionEvent) != null);
        }

        // Call the generic import routine to do the work and return the messages
        return(importDomain(actionEventResults,  "action / event result", createUpdate));
    }

    /**
     * Imports the supplied list of action event result lists
     * @param actionEventResultLists The action event result lists to import
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importActionEventResultLists(JSONArray actionEventResultLists) {

        // The closure ensure the action event result list exists
        Closure createUpdate = { jsonActionEventResultList, actionEventResultListMessages ->
            List<ActionEventResult> resultItems = new ArrayList<ActionEventResult>();

            // Have we been supplied with any action event results
            if (jsonActionEventResultList.actionEventResults) {
                jsonActionEventResultList.actionEventResults.each { jsonActionEventResult ->
                    ActionEventResult resultItem = ActionEventResult.lookup(jsonActionEventResult.code);
                    if (resultItem) {
                        // IT exists, so add it to the result list
                        resultItems.add(resultItem);
                    } else {
                        messages.add("Unable to find action event result \"" + jsonActionEventResult.code + "\" fof action event result list with code: \"" + jsonActionEventResultList.code + "\"");
                    }
                }
            } else {
                // Not an error, just an empty list
                messages.add("An empty list has been supplied for action / event result list with code: \"" + jsonActionEventResultList.code + "\"");
            }

            // Now create / update this result list
            return(ActionEventResultList.ensure(
                jsonActionEventResultList.code,
                jsonActionEventResultList.description,
                resultItems) != null);
        };

        // Call the generic import routine to do the work and return the messages
        return(importDomain(actionEventResultLists,  "action / event result list", createUpdate));
    }

    /**
     * Imports the supplied list of actions
     * @param actionEvents The action events to import
     * @param isAction true if we adding actions, false if we are adding events
     * @return A list of informational / error messages that inform the user of what went on
     */
    public List importActionEvents(JSONArray actionEvents, boolean isAction) {

        // The closure ensure the action event exists
        Closure createUpdate = { jsonActionEvent, actionEventMessages ->
             // Create / update this action / event
            return(ActionEvent.ensure(
                jsonActionEvent.code,
                jsonActionEvent.description,
                isAction,
                jsonActionEvent. serviceClass,
                jsonActionEvent.resultList,
                (jsonActionEvent.isAvailableForBulk == null) ? false : jsonActionEvent.isAvailableForBulk,
                (jsonActionEvent.showInAuditTrail == null) ? true : jsonActionEvent.showInAuditTrail,
                convertUndoStatus(jsonActionEvent.undoStatus),
                jsonActionEvent.responderServiceClass,
                jsonActionEvent.isAvailableGroovy) != null);
        };

        // Call the generic import routine to do the work and return the messages
        return(importDomain(actionEvents, isAction ? "action" : "event", createUpdate));
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

        // The closure ensure the state model exists
        Closure createUpdate = { jsonStateModel, stateModelMessages ->
            boolean result = false;
            List states = [ ];
            List inheritedStateModels = [ ];

            // Check for any inherited state models
            if (jsonStateModel.inheritedStateModels) {
                jsonStateModel.inheritedStateModels.each { jsonInheritedStateModel ->
                    Map inheritedStateModel = [ : ];
                    inheritedStateModels.add(inheritedStateModel);
                    inheritedStateModel.stateModel = jsonInheritedStateModel.code;
                    inheritedStateModel.priority = jsonInheritedStateModel.priority;
                }
            }

            // Have any states been specified for this model
            if (jsonStateModel.stati) {
                // Loop through all the states, dealing with states specific to this model
                jsonStateModel.stati.each { jsonStatus ->
                    Map stateModelStatus = [ : ];
                    states.add(stateModelStatus);
                    stateModelStatus.status = jsonStatus.state;
                    stateModelStatus.canTriggerOverdueRequest = jsonStatus.canTriggerOverdueRequest;
                    stateModelStatus.canTriggerStaleRequest = jsonStatus.canTriggerStaleRequest;
                    stateModelStatus.isTerminal = jsonStatus.isTerminal;
                    stateModelStatus.triggerPullSlipEmail = jsonStatus.triggerPullSlipEmail;
                }
            } else {
                stateModelMessages.add("No states specified for state model with code: \"" + jsonStateModel.code + "\"");
            }

            // Delete all available actions for this state model first
            StateModel stateModel = StateModel.lookup(jsonStateModel.code);

            // Did we find one
            if (stateModel != null) {
                // We did so delete all the available actions as they may have changed
                if (stateModel.availableActions) {
                    // We have at least 1 available action to remove
                    stateModel.availableActions.collect().each { availableAction ->
                        // Delete the available action
                        stateModel.availableActions.remove(availableAction);
                        availableAction.delete();
                    }
                }
            }

            // Now create / update this state model
            stateModel = StateModel.ensure(
                jsonStateModel.code,
                jsonStateModel.name,
                jsonStateModel.initialState,
                jsonStateModel.staleAction,
                jsonStateModel.overdueStatus,
                jsonStateModel.pickSlipPrintedAction,
                states,
                inheritedStateModels);

            if (stateModel != null) {
                // Successfully created
                result = true;

                // We now need to iterate through the available actions for the states in the model
                if (jsonStateModel.stati) {
                    jsonStateModel.stati.each { jsonStatus ->
                        Closure createUpdateAvailableAction = { jsonAvailableAction, availableActionMessages ->
                            return(AvailableAction.ensure(
                                stateModel,
                                jsonStatus.state,
                                jsonAvailableAction.actionEvent,
                                jsonAvailableAction.triggerType,
                                jsonAvailableAction.resultList,
                                jsonAvailableAction.isAvailableGroovy) != null);
                        };

                        // Call the generic import routine to do the work
                        importDomain(
                            jsonStatus.availableActions,
                            "available action for state model with code: \"" + jsonStateModel.code + "\" and status with code \"" + jsonStatus.state + "\"",
                            createUpdateAvailableAction,
                            stateModelMessages,
                            "actionEvent");
                    }
                }
            }

            // Return the result to the caller
            return(result);
        };

        // The messages that have been generated during the import process
        List messages = [ ];

        // As we can inherit state tables we need to ensure any ones we are inheriting are already in the database before we attempt to import the state table
        JSONArray pendingStateModels;
        JSONArray stateModelsToImport;
        JSONArray stateModelsToCheck = stateModels;
        int lastStateModelsToCheckSize = -1;

        // Keep loop until we have no more state models to load or we are referenceing a state model that is not going to get loaded
        while (stateModelsToCheck && (lastStateModelsToCheckSize != stateModelsToCheck.size())) {
            // Process all the state models putting them in the appropriate list
            pendingStateModels = new JSONArray();
            stateModelsToImport = new JSONArray();
            stateModelsToCheck.each { stateModel ->
                // Are we inheriting any state models
                if (stateModel.inheritedStateModels) {
                    // Check that all the state model we are referencing already exists
                    if (stateModel.inheritedStateModels.find { inheritedStateModel ->
                        // We return true if the state model is not in the database
                        return(StateModel.lookup(inheritedStateModel.code) == null)
                    } == null) {
                        // Needs to go back in the to import list as all inherited state models exist
                        stateModelsToImport.add(stateModel);
                    } else {
                        // Needs to go back in the pending list as an inherited state model does not exist
                        pendingStateModels.add(stateModel);
                    }
                } else {
                    // Can go straight into the import list as there are no inherited state models
                    stateModelsToImport.add(stateModel);
                }
            }

            // All state models have been checked
            lastStateModelsToCheckSize = stateModelsToCheck.size();
            stateModelsToCheck = pendingStateModels;

            // Process the ones that we can
            messages += importDomain(stateModelsToImport, "state model", createUpdate);
        }

        // if We still have some state models to import then we have some inherited state models that do not exist
        stateModelsToCheck.each { stateModel ->
            // Must be inheriting state models for them to be still in the check list
            if (stateModel.inheritedStateModels) {
                // Add a message for each inherited state model that does not exist
                stateModel.inheritedStateModels.each { inheritedStateModel ->
                    if (StateModel.lookup(inheritedStateModel.code) == null) {
                        messages.add("State model " + stateModel.code + " has not been loaded as it is inheriting from state model " + inheritedStateModel.code + " which does not exist");
                    }
                }
            } else {
                // This should not happen, but for some reason it has happens
                messages.add("State model " + stateModel.code + " has not imported");
            }
        }

        // Call the generic import routine to do the work and return the messages
        return(messages);
    }
}
