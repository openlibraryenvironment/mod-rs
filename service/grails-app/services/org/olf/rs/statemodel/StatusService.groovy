package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest
import org.olf.rs.ProtocolReferenceDataValue;
import org.olf.rs.ReferenceDataService;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.SettingsService;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.rs.referenceData.SettingsData;

import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * Houses all the functionality to do with the status
 * @author Chas
 *
 */
public class StatusService {

    ReferenceDataService referenceDataService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    SettingsService settingsService;

    /** The value of the ref data value record for if we need to save the status*/
    private String saveValue = null;

    /** The value of the ref data value record for if we need to restore the status */
    private String restoreValue = null;

    /**
     * Returns a list of transitions for the given model and action
     * @param model The model that we want the tansitions for
     * @param action The action that we want the transitions for
     * @param traverseHierarchy Do we traverse the inheritance hierarchy or not
     * @return All the transitions for this model and action combination
     */
    public List<Transition> possibleActionTransitionsForModel(StateModel model, ActionEvent action, boolean traverseHierarchy = true) {
        List<Transition> transitions = [ ];

        // If we had a valid action and model that is a good start
        if ((action != null) && (model != null)) {
            // Get hold of the transitions for this model
            transitionsForModel(transitions, model, action, traverseHierarchy);
        }

        // Return the transitions to the caller
        return(transitions);
    }

    /**
     * Adds the list of transitions available to this model to the supplied transition list if it dosn't already exist
     * @param transitions The list of transitions that needs to be added
     * @param model The model that we want the tansitions for
     * @param action The action that we want the transitions for
     * @param traverseHierarchy Do we traverse the inheritance hierarchy or not
     */
    private void transitionsForModel(List<Transition> transitions, StateModel model, ActionEvent action, boolean traverseHierarchy) {

        // Loop through all the available actions for the supplied model and action
        AvailableAction.findAllByActionEventAndModel(action, model).each { availableAction ->
            // Lets find the result list we are dealing with
            ActionEventResultList resultList = availableAction.resultList;

            // If we do not have a resultList fall back to the one on the action event record
            if (resultList == null) {
                resultList = action.resultList;
            }

            // If we still do not have one or there are no results, just add a transition record that stays at the same state
            if ((resultList == null) || !resultList.results) {
                addTransitionIfNotExist(
                    transitions,
                    availableAction.fromState,
                    action,
                    null,
                    availableAction.fromState
                );
            } else {
                // // We have a result list, so we need to process them all
                resultList.results.each { result ->
                    // Is this result valid for this from status
                    if ((result.fromStatus == null) || result.fromStatus.id.equals(availableAction.fromState.id)) {
                        // if we are restoring we need to be a lot cleverer
                        if (isRestoreRequired(result.saveRestoreState)) {
                            // ensure the saveValue is populated
                            if (saveValue == null) {
                                // Appears not t be doing anything, but is actually populating saveValue, will always return false
                                isSaveRequired(result.saveRestoreState);
                            }

                            // Find all the states that we can potentially return to
                            findAllStatesThatSaveOnStatus(result.status).forEach() { status ->
                                // Create a transition for them
                                addTransitionIfNotExist(
                                    transitions,
                                    result.status,
                                    action,
                                    ((result.qualifier == null) ? "" : (result.qualifier  + "-")) + "saved",
                                    ((result.overrideSaveStatus == null) ? status : result.overrideSaveStatus)
                                );
                            }
                        } else {
                            // We have a valid transition
                            addTransitionIfNotExist(
                                transitions,
                                availableAction.fromState,
                                action,
                                result.qualifier,
                                ((result.status == null) ? availableAction.fromState : result.status)
                            );
                        }
                    }
                }
            }
        }

        // Now do we want to traverse the hierarchy
        if (traverseHierarchy && model.inheritedStateModels) {
            // These should already be sorted by priority
            model.inheritedStateModels.each { inheritedStateModel ->
                // This is where we ger recursive
                transitionsForModel(transitions, inheritedStateModel.inheritedStateModel, action, traverseHierarchy);
            }
        }
    }

    /**
     * Adds a transition to the transitions list if it does not already exist
     * @param transitions The transitions list that the transition will added to
     * @param fromStatus The status that we will be moving from
     * @param actionEvent The action / event that is occurring
     * @param qualifier The qualifier returned by the processing of the action / event (may be null)
     * @param toStatus The status that the combination of fromStatus / actionEvent and qualifier it will transition to
     */
    private void addTransitionIfNotExist(
        List<Transition> transitions,
        Status fromStatus,
        ActionEvent actionEvent,
        String qualifier,
        Status toStatus)
    {
        // Must have fromStatus, actionEvent and toStatus, otherwise we do not add it
        if (fromStatus && actionEvent && toStatus) {
            // only add it, if it dosn't already exist
            if (transitions.find { transition ->
                // For the qualifier we have to take into account null values
                boolean qualifierMatches =
                    (qualifier == null ? (transition.qualifier == null ? true : false) :
                        (transition.qualifier == null ? false : qualifier.equals(transition.qualifier)));
                return(qualifierMatches &&
                       fromStatus.id.equals(transition.fromStatus.id) &&
                       actionEvent.id.equals(transition.actionEvent.id) &&
                       toStatus.id.equals(transition.toStatus.id));
            } == null) {
                // dosn't already exist. so add it
                transitions.add(new Transition (
                    fromStatus : fromStatus,
                    actionEvent : actionEvent,
                    qualifier : qualifier,
                    toStatus : toStatus
                ));
            }
        }
    }

    /**
     * Retrieves all the possible transitions for the supplied events for the given state model
     * @param model The state model that we want the transitions for
     * @param events The events that the transitions are required for
     * @return The transitions that are appropriate for the supplied state model and events
     */
    public List<Transition> possibleEventTransitionsForModel(StateModel model, List<ActionEvent> events) {
        List<Transition> transitions = new ArrayList<Transition>();

        // Can't continue if we do not have a model or events
        if ((events != null) && (model != null)) {
            // Obtain the list of valid status for the model
            List<Status> validStatus = StateModel.getAllStates(model.shortcode);

            // Process the events
            events.each() { event ->
                // Must have a resultList
                if (event.resultList != null) {
                    // First we need to find the status that triggers this event
                    Status status = Status.lookupStatusEvent(event);

                    // Did we find a status
                    if (status != null) {
                        // Is it a state that belongs to the state model
                        if (validStatus.find { modelState -> return(modelState.id.equals(status.id)) } != null) {
                            // Now we can process the result records
                            event.resultList.results.each() { result ->
                                // Only interested where the status has changed
                                if ((result.status != null) && !result.status.id.equals(status.id)) {
                                // if we are restoring we need to be a lot cleverer
                                    if (isRestoreRequired(result.saveRestoreState)) {
                                        // ensure the saveValue is populated
                                        if (saveValue == null) {
                                            // Appears not t be doing anything, but is actually populating saveValue, will always return false
                                            isSaveRequired(result.saveRestoreState);
                                        }

                                        // Find all the states that we can potentially return to
                                        findAllStatesThatSaveOnStatus(result.status).forEach() { toStatus ->
                                            // Create a transition for them
                                            transitions.add(new Transition (
                                                fromStatus : status,
                                                actionEvent : event,
                                                qualifier : ((result.qualifier == null) ? "" : (result.qualifier  + "-")) + "saved" ,
                                                toStatus : ((result.overrideSaveStatus == null) ? toStatus : result.overrideSaveStatus)
                                            ));
                                        }
                                    } else {
                                        // Should we take into account save / restore, does this happen during an event ...
                                        transitions.add(new Transition (
                                            fromStatus : status,
                                            actionEvent : event,
                                            qualifier : result.qualifier,
                                            toStatus : result.status
                                        ));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // We can now return the transitions
        return(transitions);
    }

    /**
     * Performs a reverse lookup to find which status could be saved when we change to a specific state
     * Note: this does not take into account events
     * @param status The status that we change to when the state is changed
     * @return The list of status that could have taken us to this state
     */
    private List<Status> findAllStatesThatSaveOnStatus(Status status) {
        List<Status>savedStatus = new ArrayList<Status>();

        // We need to work our way backwards here, starting from the result, we then go through all the lists,
        // up through the available actions and the ActionEvents and back through the AvailableActions
        // and of course not forgetting the events, so bit of a journey here to find what we are looking for
        // So first of all look for all the result lists that save the current status for this status
        List<ActionEventResultList> resultLists = ActionEventResultList.getResultsListForSaveStatus(status, saveValue);

        // Now we lookup the AvailableActions that have this result list set on it
        if (resultLists) {
            resultLists.each(){ resultList ->
                AvailableAction.findAllByResultList(resultList).each() { availableAction ->
                    // Add it to our list
                    savedStatus.add(availableAction.fromState);
                }

                // Now go via the ActionEvent
                ActionEvent.findAllByResultList(resultList).each() { actionEvent ->
                    // Now look up AvailableAction where the resultList is null for this action event
                    AvailableAction.findAllByActionEventAndResultListIsNull(actionEvent).each() { availableAction ->
                        // Add the from status to our list
                        savedStatus.add(availableAction.fromState);
                    }
                }
            }
        }

        // Return a unique list
        return(savedStatus.unique(){ stat -> stat.id });
    }

    /**
     * Looks to find a result record from the parameters that have been passed in
     * @param model The state model that the result has to be associated with
     * @param fromStatus The status we want to transition from
     * @param actionCode The action code that has been performed
     * @param successful Was the execution of the action successful
     * @param qualifier A qualifier for looking up the result
     * @param availableActionMustExist If trye it means an AvailableAction record must exist
     * @return An ActionEventResult that informs us what to do on completion of the ActionEvent
     */
    private ActionEventResult findResult(StateModel model, Status fromStatus, String actionCode, boolean successful, String qualifier, boolean availableActionMustExist) {
        // We return null if we could not find a status
        log.debug("Calling StatusService.findResult with model ${model.shortcode}, fromStatus ${fromStatus.code}, actionCode ${actionCode}" +
            " successful ${successful}, qualifier ${qualifier}, availableActionMustExist ${availableActionMustExist}");
        ActionEventResult actionEventResult = null;

        // Must have a from status and action code, qualifier is optional
        if ((fromStatus != null) && (actionCode != null)) {
            // Get hold of the ActionEvent
            ActionEvent actionEvent = ActionEvent.lookup(actionCode);
            if (actionEvent != null) {
                boolean carryOn = true;
                if (availableActionMustExist) {
                    // Get hold of the AvailableAction
                    AvailableAction availableAction = lookupAvailableAction(model, fromStatus, actionEvent);
                    log.debug("Found AvailableAction ${availableAction} with resultList ${availableAction?.resultList?.code}");
                    if (availableAction != null) {
                        // Now do we have a resultList on the availableAction
                        if (availableAction.resultList != null) {
                            actionEventResult = availableAction.resultList.lookupResult(successful, qualifier, fromStatus);
                        }
                    } else {
                        carryOn = false;
                        log.error('Looking up the to status, but unable to find an AvailableAction for Model: ' + model.shortcode +
                                     ', fromStatus: ' + fromStatus.code +
                                     ', action: ' + actionCode);
                    }
                }

                // Do we carry on or was there a problem with the AvailableAction
                if (carryOn) {
                    // Did we find a result
                    if (actionEventResult == null) {
                        // We didn't find one on the availableAction, so look at the actionEvent
                        log.debug("No actionEventResult from availableAction, checking actionEvent")
                        if (actionEvent.resultList != null) {
                            // We have a second bite of the cherry
                            log.debug("Looking up actionEventResult with result ${successful}, qualifier ${qualifier} and fromStatus ${fromStatus?.code}");
                            actionEventResult = actionEvent.resultList.lookupResult(successful, qualifier, fromStatus);
                        }

                        // If we still didn;t find a result log an error
                        if (actionEventResult == null) {
                            log.error('Looking up the to status, but unable to find an ActionEventResult for Status: ' + fromStatus?.code +
                                ', action: ' + actionCode +
                                ', successful: ' + successful +
                                ', qualifier: ' + qualifier);
                        }
                    }
                }
            } else {
                log.error('Looking up the to status, but unable to find ActionEvent for code: ' +  actionCode);
            }
        } else {
            log.error('Looking up the to status, but either fromStatus (' + fromStatus?.code + ') actionCode (' + actionCode + ') is null');
        }

        // Return the result to the caller
        return(actionEventResult);
    }

    /**
     * Looks to see if the available action exists for the state model, looking at inherited state models as well if any
     * @param stateModel The state model we want to lookup
     * @param fromStatus The status that we are coming from
     * @param actionEvent The action / event being performed
     * @return The available action record if one was found, if not null
     */
    private AvailableAction lookupAvailableAction(StateModel stateModel, Status fromStatus, ActionEvent actionEvent) {
        // Nice and simple lookup the available action
        AvailableAction availableAction = AvailableAction.findByFromStateAndActionEventAndModel(fromStatus, actionEvent, stateModel);

        // Did we find the transition
        if (availableAction == null) {
            // We did not, so look to see if we are inheriting it
            if (stateModel.inheritedStateModels) {
                // We do so go through each of the state models to see if we can find the available action
                // The set should already have been sorted by priority
                stateModel.inheritedStateModels.find { inheritedStateModel ->

                    // this is where we get recursive ...
                    availableAction = lookupAvailableAction(inheritedStateModel.inheritedStateModel, fromStatus, actionEvent);

                    // return true if we managed to find an available action
                    return(availableAction != null);
                }
            }
        }

        // Return the result to the caller
        return(availableAction);
    }

    /**
     * Looks up the database to see what the new status should be for the request
     * @param request The request that we want to set the status for
     * @param action The action that has been performed
     * @param qualifier The qualifier for looking up the status
     * @param successful Were we successful performing the action
     * @param availableActionMustExist If trye it means an AvailableAction record must exist
     * @return The determined status
     */
    public NewStatusResult lookupStatus(PatronRequest request, String action, String qualifier, boolean successful, boolean availableActionMustExist) {
        // We default to staying at the current status
        NewStatusResult newStatusResult = new NewStatusResult(request.state);

        // Now lets see if we can find the ActionEventResult record
        ActionEventResult actionEventResult = findResult(getStateModel(request), request.state, action, successful, qualifier, availableActionMustExist);

        // Did we find a result
        if (actionEventResult != null) {
            // Is the status set on it
            if (actionEventResult.status != null) {
                // Do we just need to restore the status
                if (isRestoreRequired(actionEventResult.saveRestoreState)) {
                    // So we just restore it from what was previously saved, we assume the model is the same as per the current status
                    String statusCode = actionEventResult.status.code;
                    String newStatusCode = request.previousStates.get(statusCode);
                    if (newStatusCode != null) {
                        // That is good we have a saved status
                        newStatusResult.status = Status.lookup(newStatusCode);
                        newStatusResult.updateRotaLocation = actionEventResult.updateRotaLocation;

                        // We also clear the saved value
                        request.previousStates[statusCode] = null;
                    }
                } else {
                    // Do we need to save the status
                    if (isSaveRequired(actionEventResult.saveRestoreState)) {
                        // Save the status first, need to assign to a string first, otherwise it stores null
                        String statusCode = request.state.code;
                        if (actionEventResult.overrideSaveStatus != null) {
                            // They have overridden the status we need to save
                            statusCode = actionEventResult.overrideSaveStatus.code;
                        }

                        // Now save the status they want to return to
                        request.previousStates.put(actionEventResult.status.code, statusCode);
                    }

                    // So set our new status
                    newStatusResult.status = actionEventResult.status;
                    newStatusResult.updateRotaLocation = actionEventResult.updateRotaLocation;
                }
            } else {
                String error = "The status is null on the found actionEventResult '${actionEventResult?.code}', so status is staying the same, From Status: " + request.state.code +
                               ', Action: ' + action +
                               ', Qualifier: ' + ((qualifier == null) ? '' : qualifier);
                log.warn(error);
            }
        } else {
            String error = 'No actionEventResult found, so status is staying the same for, From Status: ' + request.state.code +
                           ', Action: ' + action +
                           ', Qualifer: ' + ((qualifier == null) ? '' : qualifier);
            log.warn(error);
        }

        // Return the new status to the caller
        return(newStatusResult);
    }

    /**
     * Checks the supplied RefdataValue to see if we are restoring the status from a previous saved value
     * @param saveRestoreValue The reference value that needs to be checked to see if it means restore the previous saved value
     * @return true if we need to restore a previously saved value otherwise false
     */
    private boolean isRestoreRequired(RefdataValue saveRestoreValue) {
        boolean result  = false;

        // Have we been passed a value
        if (saveRestoreValue != null) {
            // Have we obtained the value for the restore reference value
            if (restoreValue == null) {
                // So get it
                RefdataValue refdataValue = referenceDataService.lookup(RefdataValueData.VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE);
                if (refdataValue != null) {
                    restoreValue = refdataValue.value;
                }
            }

            // We should have it now
            if (restoreValue != null) {
                // Now compare the values, if they match then they want to restore a previous status
                result = restoreValue.equals(saveRestoreValue.value);
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Checks the supplied RefdataValue to see if we need to save the current status before it is set with the new status
     * @param saveRestoreValue The reference value that needs to be checked to see if it means save the current value
     * @return true if we need to save the current status before we overwrite it otherwise false
     */
    private boolean isSaveRequired(RefdataValue saveRestoreValue) {
        boolean result  = false;

        // Have we been passed a value
        if (saveRestoreValue != null) {
            // Have we obtained the value for the save reference value
            if (saveValue == null) {
                // So get it
                RefdataValue refdataValue = referenceDataService.lookup(RefdataValueData.VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, RefdataValueData.ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE);
                if (refdataValue != null) {
                    saveValue = refdataValue.value;
                }
            }

            // We should have it now
            if (saveValue != null) {
                // Now compare the values, if they match then they want to save the existing status
                result = saveValue.equals(saveRestoreValue.value);
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Determines the state model for a patron request
     * @param request The request we want the state model for
     * @return The state model that is applicable for that request
     */
    public StateModel getStateModel(PatronRequest request) {
        String settingsKey
        String stateModelCode = null
        if (request.isRequester != null) {
            Boolean isRequester = request.isRequester;

            if (request.serviceType?.value == 'copy') {
                settingsKey = isRequester
                        ? SettingsData.SETTING_STATE_MODEL_REQUESTER_NON_RETURNABLE
                        : SettingsData.SETTING_STATE_MODEL_RESPONDER_NON_RETURNABLE
            } else {
                if (request.deliveryMethod?.value == 'url') {
                    settingsKey = isRequester
                            ? SettingsData.SETTING_STATE_MODEL_REQUESTER_DIGITAL_RETURNABLE
                            : SettingsData.SETTING_STATE_MODEL_RESPONDER_CDL
                } else {
                    settingsKey = isRequester
                            ? SettingsData.SETTING_STATE_MODEL_REQUESTER_RETURNABLE
                            : SettingsData.SETTING_STATE_MODEL_RESPONDER_RETURNABLE
                }
            }

            if (settingsKey) {
                stateModelCode = settingsService.getSettingValue(settingsKey);
            }
        }

        // Lookup the state model
        StateModel stateModel = StateModel.lookup(stateModelCode);
        if (stateModel == null) {
            // Log the fact that we cannot find the state model and return a default
            log.error("unable to find state model with code " + stateModelCode + " in StatusService.getStateModel(" + boolean.toString() + ")");
            stateModel = StateModel.lookup(request.isRequester ? StateModel.MODEL_REQUESTER : StateModel.MODEL_RESPONDER);
        }

        // Return the found state model
        return(stateModel);
    }
}
