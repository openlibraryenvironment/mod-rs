package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReferenceDataService;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.referenceData.RefdataValueData;

import com.k_int.web.toolkit.refdata.RefdataValue;

/**
 * Houses all the functionality to do with the status
 * @author Chas
 *
 */
public class StatusService {

    ReferenceDataService referenceDataService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;

    /** The vslue of the ref data value record for if we need to save the status*/
    private String saveValue = null;

    /** The vslue of the ref data value record for if we need to restore the status */
    private String restoreValue = null;

    /**
     * Looks to find a result record from the parameters that have been passed in
     * @param fromStatus The status we want to transition from
     * @param actionCode The action code that has been performed
     * @param successful Was the execution of the action successful
     * @param qualifier A qualifier for looking up the result
     * @return An ActionEventResult that informs us what to do on completion of the ActionEvent
     */
    public ActionEventResult findResult(Status fromStatus, String actionCode, boolean successful, String qualifier) {
        // We return null if we could not find a status
        ActionEventResult actionEventResult = null;

        // Must have a from status and action code, qualifier is optional
        if ((fromStatus != null) && (actionCode != null)) {
            // Get hold of the ActionEvent
            ActionEvent actionEvent = ActionEvent.lookup(actionCode);
            if (actionEvent != null) {
                // Get hold of the AvailableAction
                AvailableAction availableAction = AvailableAction.findByFromStateAndActionEventAndModel(fromStatus, actionEvent, fromStatus.owner);
                if (availableAction != null) {
                    // Now do we have a resultList on the availableAction
                    if (availableAction.resultList != null) {
                        actionEventResult = availableAction.resultList.lookupResult(successful, qualifier, fromStatus);
                    }

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
                } else {
                    log.error('Looking up the to status, but unable to find an AvailableAction for Model: ' + fromStatus.owner.shortcode +
                                 ', fromStatus: ' + fromStatus.code +
                                 ', action: ' + actionCode);
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
     * @return The determined status
     */
    public Status lookupStatus(PatronRequest request, String action, String qualifier, boolean successful) {
        // We default to staying at the current status
        Status newStatus = request.state;

        // Now lets see if we can find the ActionEventResult record
        ActionEventResult actionEventResult = findResult(request.state, action, successful, qualifier);

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
                        newStatus = reshareApplicationEventHandlerService.lookupStatus(request.state.owner.shortcode, newStatusCode);

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
}
