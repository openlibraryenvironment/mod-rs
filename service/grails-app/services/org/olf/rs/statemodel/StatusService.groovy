package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
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

    /** The vslue of the ref data value record for if we need to save the status*/
    private String saveValue = null;

    /** The value of the ref data value record for if we need to restore the status */
    private String restoreValue = null;

    /**
     * Returns a list of transitions for the given model and action
     * @param model The model that we want the tansitions for
     * @param action The action that we want the transitions for
     * @return All the transitions for this model and action combination
     */
    public List<Transition> possibleActionTransitionsForModel(StateModel model, ActionEvent action) {
        List<Transition> transitions = new ArrayList<Transition>();

        // If we had a valid action and model that is a good start
        if ((action != null) && (model != null)) {
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
                    transitions.add(new Transition (
                        fromStatus : availableAction.fromState,
                        actionEvent : action,
                        qualifier : null,
                        toStatus : availableAction.fromState
                    ));
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
                                    transitions.add(new Transition (
                                        fromStatus : result.status,
                                        actionEvent : action,
                                        qualifier : ((result.qualifier == null) ? "" : (result.qualifier  + "-")) + "saved" ,
                                        toStatus : ((result.overrideSaveStatus == null) ? status : result.overrideSaveStatus)
                                    ));
                                }
                            } else {
                                // We have a valid transition
                                transitions.add(new Transition (
                                    fromStatus : availableAction.fromState,
                                    actionEvent : action,
                                    qualifier : result.qualifier,
                                    toStatus : ((result.status == null) ? availableAction.fromState : result.status)
                                ));
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

            // TODO: how do we deal with events ... With any luck events do not trigger a save ...
            // We will deal with that when events are added to the diagram
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
    public ActionEventResult findResult(StateModel model, Status fromStatus, String actionCode, boolean successful, String qualifier, boolean availableActionMustExist) {
        // We return null if we could not find a status
        ActionEventResult actionEventResult = null;

        // Must have a from status and action code, qualifier is optional
        if ((fromStatus != null) && (actionCode != null)) {
            // Get hold of the ActionEvent
            ActionEvent actionEvent = ActionEvent.lookup(actionCode);
            if (actionEvent != null) {
                boolean carryOn = true;
                if (availableActionMustExist) {
                    // Get hold of the AvailableAction
                    AvailableAction availableAction = AvailableAction.findByFromStateAndActionEventAndModel(fromStatus, actionEvent, model);
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
                        if (actionEvent.resultList != null) {
                            // We have a second bite of the cherry
                            actionEventResult = actionEvent.resultList.lookupResult(successful, qualifier, fromStatus);
                        }

                        // If we still didn;t find a result log an error
                        if (actionEventResult == null) {
                            log.error('Looking up the to status, but unable to find an ActionEventResult for Status: ' + fromStatus.code +
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
     * Looks up the database to see what the new status should be for the request
     * @param request The request that we want to set the status for
     * @param action The action that has been performed
     * @param qualifier The qualifier for looking up the status
     * @param successful Were we successful performing the action
     * @param availableActionMustExist If trye it means an AvailableAction record must exist
     * @return The determined status
     */
    public Status lookupStatus(PatronRequest request, String action, String qualifier, boolean successful, boolean availableActionMustExist) {
        // We default to staying at the current status
        Status newStatus = request.state;

        // Now lets see if we can find the ActionEventResult record
        StateModel stateModel = (request.stateModel == null) ? getStateModel(request.isRequester) : request.stateModel;
        ActionEventResult actionEventResult = findResult(stateModel, request.state, action, successful, qualifier, availableActionMustExist);

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
                        newStatus = Status.lookup(newStatusCode);

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
                    newStatus = actionEventResult.status;
                }
            } else {
                String error = 'The status is null on the found actionEventResult, so status is staying the same for, From Status: ' + request.state.code +
                               ', Action: ' + action +
                               ', Qualifer: ' + ((qualifier == null) ? '' : qualifier);
                log.warn(error);
            }
        } else {
            String error = 'No actionEventResult found, so status is staying the same for, From Status: ' + request.state.code +
                           ', Action: ' + action +
                           ', Qualifer: ' + ((qualifier == null) ? '' : qualifier);
            log.warn(error);
        }

        // Return the new status to the caller
        return(newStatus);
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
     * Returns the state model currently in use (this will need to be extended to take into account the institution once we have determined where the institution is coming from)
     * @param isRequester Are we playing the role of requester
     * @return The state model that is being used
     */
    public StateModel getStateModel(boolean isRequester) {
        // Generate the settings key
        String settingsKey = isRequester ? SettingsData.SETTING_STATE_MODEL_REQUESTER : SettingsData.SETTING_STATE_MODEL_RESPONDER;
        String stateModelCode = settingsService.getSettingValue(settingsKey);

        // Lookup the state model
        StateModel stateModel = StateModel.lookup(stateModelCode);
        if (stateModel == null) {
            // Log the fact that we cannot find the state model and return a default
            log.error("unable to find state model with code " + stateModelCode + " in StatusService.getStateModel(" + boolean.toString() + ")");
            stateModel = StateModel.lookup(isRequester ? StateModel.MODEL_REQUESTER : StateModel.MODEL_RESPONDER);
        }

        // Return the found state model
        return(stateModel);
    }
}
