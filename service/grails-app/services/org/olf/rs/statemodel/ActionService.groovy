package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestAudit;
import org.olf.rs.ReshareApplicationEventHandlerService;

/**
 * Checks the incoming action to ensure it is valid and dispatches it to the appropriate service
 */
public class ActionService {

    private static final String POSSIBLE_ACTIONS_QUERY='select distinct aa.actionCode from AvailableAction as aa where aa.fromState = :fromstate and aa.triggerType = :triggerType'

    private static final Integer ZERO = Integer.valueOf(0);

    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    StatusService statusService;

    /**
     * Checks whether an action being performed is valid
     */
    boolean isValid(boolean isRequester, Status status, String action) {
        // We default to not being valid
        boolean isValid = false;

        // Can only continue if we have been supplied the values
        if (action && status) {
            // If it is the undo action we need to perform a slightly different test
            if (Actions.ACTION_UNDO.equals(action)) {
                // We do not have the request at this stage, so we assume it is valid
                isValid = true;
            } else {
                // Get hold of the state model
                StateModel stateModel = statusService.getStateModel(isRequester);
                if (stateModel) {
                    // It is a valid state model
                    // Now is this a valid action for this state
                    isValid = (AvailableAction.countByModelAndFromStateAndActionCode(stateModel, status, action) == 1);
                }
            }
        }
        return(isValid);
    }

    /**
     * Obtains an instance of the service that will perform the requested action
     * @param actionCode The action that is to be performed
     * @param isRequester Whether it is for the requester or responder
     * @return The instance of the service that will perform the action
     */
    public AbstractAction getServiceAction(String actionCode, boolean isRequester) {
        AbstractAction actionProcessor = null;
        def service = ActionEvent.lookupService(actionCode, isRequester);
        if ((service != null) && (service instanceof AbstractAction)) {
            actionProcessor = (AbstractAction)service;
        } else {
            log.error("Unable to locate service for action: " + actionCode);
        }
        return(actionProcessor);
    }

    /**
     * Performs the desired action against the supplied request
     * @param action the action to be performed
     * @param request the request the action is to be performed against
     * @param parameters any parameters that may be required for the action
     * @return An ActionResultDetails that contains the result of performing the action
     */
    ActionResultDetails performAction(String action, PatronRequest request, Object parameters) {
        // Lookup the ActionEvent record
        ActionEvent actionEvent = ActionEvent.lookup(action);

        ActionResultDetails resultDetails = new ActionResultDetails();
        Status currentState = request.state;

        // Default a few fields
        resultDetails.result = ActionResult.SUCCESS;
        resultDetails.auditMessage = 'Executing action: ' + action;
        resultDetails.auditData = parameters;

        // Get hold of the action
        AbstractAction actionBean = getServiceAction(action, request.isRequester);
        if (actionBean == null) {
            resultDetails.result = ActionResult.ERROR;
            resultDetails.auditMessage = 'Failed to find class for action: ' + action;
        } else {
            // Just tell the class to do its stuff
            resultDetails = actionBean.performAction(request, parameters, resultDetails);
        }

        // Now lookup what we will set the status to
        Status newStatus;

        // if we have an override status then we shall use that and not calculate the status, this is primarily for the undo action where we look at the audit trail to see what we need to set the status to
        if (resultDetails.overrideStatus == null) {
            // Normal scenario of an action being performed
            newStatus = statusService.lookupStatus(request, action, resultDetails.qualifier, resultDetails.result == ActionResult.SUCCESS, true);
        } else {
            // The status has been overridden in the code
            newStatus = resultDetails.overrideStatus;
        }

        // Set the status of the request
        request.state = newStatus;

        // Adding an audit entry so we can see what states we are going to for the event
        // Do not commit this uncommented, here to aid seeing what transition changes we allow
//        reshareApplicationEventHandlerService.auditEntry(request, currentState, request.state, 'Action: ' + action + ', State change: ' + currentState.code + ' -> '  + request.state.code, null);

        // Create the audit entry
        reshareApplicationEventHandlerService.auditEntry(
            request,
            currentState,
            request.state,
            resultDetails.auditMessage,
            resultDetails.auditData,
            actionEvent,
            resultDetails.messageSequenceNo
        );

        // Finally Save the request
        request.save(flush:true, failOnError:true);

        // Return the result to the caller
        return(resultDetails);
    }

    /**
     * Determines if the undo action is valid for the request
     * @param request The request we want to know if we can undo the last action
     * @return true if we can undo the action, false if not
     */
    boolean isUndoValid(PatronRequest request) {
        return(buildUndoAudits(request) != null);
    }

    /**
     * Builds up the list of audit records that require undoing
     * @param request The request we want to know if we can undo the last action
     * @return The list of audit records or null if undo is not valid
     */
    List<PatronRequestAudit> buildUndoAudits(PatronRequest request) {
        List<PatronRequestAudit> undoAudits = new ArrayList<PatronRequestAudit>();

        // Can't do anything if no audits
        if (request.audit != null) {
            // To determine this we need to run down the audit records in reverse order until we hit an action, that has
            // 1. If the undoPerformed property is set to true then skip this audit record
            // 2. If the actionEvent is set against the audit record then its undo status is not set to NO
            request.audit.sort(true, {audit -> -(audit.auditNo == null ? ZERO : audit.auditNo)}).find { audit ->
                // Only interested where an undo has not already been performed
                if (!audit.undoPerformed) {
                    // We must have an auditEvent
                    if (audit.actionEvent != null) {
                        // Now check to see if we can perform an undo
                        if ((audit.actionEvent.undoStatus == UndoStatus.NO) ||
                            ((audit.actionEvent.undoStatus == UndoStatus.DECIDE) &&
                             !audit.fromStatus.id.equals(audit.toStatus.id))) {
                            // Bail out here as we can't perform the undo
                            undoAudits = null;
                            return(true);
                        } else {
                            // If it is an action and the undo status is YES then we can perform an undo
                            if (audit.actionEvent.undoStatus == UndoStatus.YES) {
                                // Add to the list of audits that need undoing
                                undoAudits.add(audit);

                                // If this represents an action we need to look no further
                                if (audit.actionEvent.isAction) {
                                    // We no need to look at anymore audit records
                                    return(true);
                                }
                            }
                        }
                    }
                }

                // We are good to perform an undo or we are skipping this record so inspect the next one
                return(false);
            }
        }

        return(((undoAudits == null) || (undoAudits.size() == 0)) ? null : undoAudits);
    }

    /**
     * Returns the valid list of actions for a request
     * @param request The request we want the actions for
     * @return The list of valid actions
     */
    public List getValidActions(PatronRequest request) {
        List actions;

        // We only have valid actions if network activity is idle
        if (request.isNetworkActivityIdle()) {
            actions = AvailableAction.executeQuery(POSSIBLE_ACTIONS_QUERY,[fromstate: request.state, triggerType: AvailableAction.TRIGGER_TYPE_MANUAL]);
            if (isUndoValid(request)) {
                // Add the undo action to the list of valid actions
                actions.add(Actions.ACTION_UNDO);
            }
        } else {
            actions = [];
        }

        // Return the valid list of actions
        return(actions);
    }
}
