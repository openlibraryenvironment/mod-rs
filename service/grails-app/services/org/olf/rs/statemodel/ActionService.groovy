package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestAudit;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.dynamic.DynamicGroovyService
import org.olf.rs.logging.ContextLogging;

/**
 * Checks the incoming action to ensure it is valid and dispatches it to the appropriate service
 */
public class ActionService {

    private static final String POSSIBLE_ACTIONS_QUERY = 'select distinct aa.actionEvent.code, aa.actionEvent.isAvailableGroovy, aa.isAvailableGroovy from AvailableAction as aa where aa.model = :stateModel and aa.fromState = :fromstate and aa.triggerType in ( :triggerTypes )';

    private static final Integer ZERO = Integer.valueOf(0);

    private static final List TRIGGERS_MANUAL        = [ AvailableAction.TRIGGER_TYPE_MANUAL ];
    private static final List TRIGGERS_MANUAL_SYSTEM = [ AvailableAction.TRIGGER_TYPE_MANUAL, AvailableAction.TRIGGER_TYPE_SYSTEM ];

    DynamicGroovyService dynamicGroovyService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    StatusService statusService;

    /**
     * Executes the supplied action to the given request with the supplied parameters
     * @param patronRequestId The request the action is to be supplied to
     * @param action The action that is to be applied
     * @param parameters The parameters to be used when processing the action
     * @return A map containing the result of the processing
     */
    public Map executeAction(String patronRequestId, String action, Object parameters) {
        Map result = [ : ];

        // Lets see if this is a valid request
        if (patronRequestId) {
            try {
                // Try and find the request
                PatronRequest patronRequest = PatronRequest.lock(patronRequestId);

                // Did we find it
                if (patronRequest == null) {
                    // Unable to locate request
                    result.actionResult = ActionResult.INVALID_PARAMETERS;
                    result.message = 'Unable to find request with id: ' + patronRequestId;
                } else {
                    // We did, so we can really do the work
                    result =  executeAction(patronRequest, action, parameters);
                }
            } catch (Exception e) {
                log.error("Excption thrown while trying to execute action " + action + " on request " + patronRequestId, e);
                result.actionResult = ActionResult.INVALID_PARAMETERS;
                result.message = 'System error occured while trying to process request ' + patronRequestId;
            }
        } else {
            // Unable to locate request
            result.actionResult = ActionResult.INVALID_PARAMETERS;
            result.message = 'No patron request id supplied';
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Executes the supplied action to the given request with the supplied parameters
     * @param patronRequest The request the action is to be applied to
     * @param action The action that is to be applied
     * @param parameters The parameters to be used when processing the action
     * @return A map containing the result of the processing
     */
    public Map executeAction(PatronRequest patronRequest, String action, Object parameters) {
        Map result = [ : ];

        // Do we have a request
        if (patronRequest == null) {
            // No we do not
            result.actionResult = ActionResult.INVALID_PARAMETERS;
            result.message='No patron request supplied';
        } else {
            // Add the hrid to the logging context
            ContextLogging.setValue(ContextLogging.FIELD_HRID, patronRequest.hrid);
            ContextLogging.setValue(ContextLogging.FIELD_REQUEST_ACTION, action);

            log.debug("Apply action ${action} to ${patronRequest.id}");

            // Needs to fulfil the following criteria to be valid
            // 1. Is a valid action for the current status of the request
            // 2. Request has no network activity going on
            if (patronRequest.isNetworkActivityIdle()) {
                if (isValid(patronRequest, action) || (patronRequest.isRequester && Actions.ACTION_REQUESTER_EDIT.equals(action))) {
                    // Perform the requested action
                    ActionResultDetails resultDetails = performAction(action, patronRequest, parameters);
                    result = resultDetails.responseResult;
                    result.actionResult = resultDetails.result;
                } else {
                    // Not a valid action or the request is busy with a previous action
                    result.actionResult = ActionResult.INVALID_PARAMETERS;
                    result.message = 'A valid action was not supplied, isRequester: ' + patronRequest.isRequester +
                                     ' Current state: ' + patronRequest.state.code +
                                     ', network status: ' + patronRequest.networkStatus.toString() +
                                     ' Action being performed: ' + action;
                    reshareApplicationEventHandlerService.auditEntry(patronRequest, patronRequest.state, patronRequest.state, result.message, null);
                    patronRequest.save(flush:true, failOnError:true);
                }
            } else {
                // Not a valid action or the request is busy with a previous action
                result.actionResult = ActionResult.INVALID_PARAMETERS;
                result.message = 'Unable to perform action as we are waiting for existing network activity to complete' +
                                 ', network status: ' + patronRequest.networkStatus.toString() +
                                 ', Action being performed: ' + action;
                reshareApplicationEventHandlerService.auditEntry(patronRequest, patronRequest.state, patronRequest.state, result.message, null);
                patronRequest.save(flush:true, failOnError:true);
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Checks whether an action being performed is valid
     */
    boolean isValid(PatronRequest request, String action) {
        // We default to not being valid
        boolean isValid = false;

        // Can only continue if we have been supplied the values
        if (action && request?.state) {
            // Get hold of the valid actions
            List validActions = getValidActions(request, true);

            // Only if the passed in action is in the list of valid actions, is the action being performed valid
            isValid = validActions.contains(action);
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
     * @param includeSystem Include available actions that are triggered by the system, default: false
     * @return The list of valid actions
     */
    public List getValidActions(PatronRequest request, boolean includeSystem = false) {
        List actions;

        // We only have valid actions if network activity is idle
        if (request.isNetworkActivityIdle()) {
            // Obtain the valid list of actions
            actions = lookupAvailableActions(statusService.getStateModel(request), request, includeSystem);

            // Remove any duplicates as they might have come from multiple state models
            actions.unique();

            // Is the undo action action valid
            if (isUndoValid(request)) {
                // Add the undo action to the list of valid actions
                actions.add(Actions.ACTION_UNDO);
            }
        } else {
            // Just return an empty list
            actions = [];
        }

        // Return the valid list of actions
        return(actions);
    }

    /**
     * Returns the list of valid actions for a state model from the given state, taking into account any inherited state models
     * @param stateModel The state model that we are looking for the valid actions from
     * @param request The patron requestwe are making the decision for
     * @param includeSystem Also include system triggers when looking up the available actions
     * @return The list of actions that is valid for the state model and status combination
     */
    private List lookupAvailableActions(StateModel stateModel, PatronRequest request, boolean includeSystem) {
        List actions = [ ];

        // Was nice and simple lookup the available actions getting the distinct actions
        AvailableAction.executeQuery(POSSIBLE_ACTIONS_QUERY, [ stateModel: stateModel, fromstate: request.state, triggerTypes: (includeSystem ? TRIGGERS_MANUAL_SYSTEM : TRIGGERS_MANUAL) ]).each { availableAction ->
            // To try and make it more obvious as to what is selected, we extract them into variables here
            String code = availableAction[0];
            String actionGroovy = availableAction[1];
            String availableActionGroovy = availableAction[2];

            // Do we have some groovy to execute on the available action
            if (availableActionGroovy) {
                // We do for the available action, if it returns true then we add it to the available actions list
                if (dynamicGroovyService.executeScript("availableAction:isAvailable:" + code, availableActionGroovy, [ patronRequest : request ])) {
                    // Script says yes
                    actions.add(code);
                }
            } else if (actionGroovy) {
                // We do for the action, if it returns true then we add it to the available actions list
                if (dynamicGroovyService.executeScript("actionEvent:isAvailable:" + code, actionGroovy, [ patronRequest : request ])) {
                    // Script says yes
                    actions.add(code);
                }
            } else {
                // We do not have any groovy so just add the action
                actions.add(code);
            }
        }

        // We need to take into account any transitions we may be inheriting
        if (stateModel.inheritedStateModels) {
            // We do so go through each of the state models to see if we can find the available action
            stateModel.inheritedStateModels.each { inheritedStateModel ->
                // this is where we get recursive ...
                actions += lookupAvailableActions(inheritedStateModel.inheritedStateModel, request, includeSystem);
            }
        }

        // Return the result to the caller
        return(actions);
    }
}
