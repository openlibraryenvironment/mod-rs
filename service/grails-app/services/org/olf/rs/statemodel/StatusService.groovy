package org.olf.rs.statemodel;

/**
 * Houses all the functionality to do with the status
 * @author Chas
 *
 */
public class StatusService {

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
                        actionEventResult = availableAction.resultList.lookupResult(successful, qualifier);
                    }

                    // Did we find a result
                    if (actionEventResult == null) {
                        // We didn't find one on the availableAction, so look at the actionEvent
                        if (actionEvent.resultList != null) {
                            // We have a second bite of the cherry
                            actionEventResult = actionEvent.resultList.findResult(successful, qualifier);
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
}
