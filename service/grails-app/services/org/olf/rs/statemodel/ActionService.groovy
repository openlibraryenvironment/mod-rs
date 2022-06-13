package org.olf.rs.statemodel;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;

import grails.util.Holders;

/**
 * Checks the incoming action to ensure it is valid and dispatches it to the appropriate service
 */
public class ActionService {

    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
    StatusService statusService;

    /** Holds map of the action to the bean that will do the processing for this action */
    private Map serviceActions = [ : ];

    /**
     * Checks whether an action being performed is valid
     */
    boolean isValid(boolean isRequester, Status status, String action) {
        // We default to not being valid
        boolean isValid = false;

        // Can only continue if we have been supplied the values
        if (action && status) {
            // Get hold of the state model id
            StateModel stateModel = StateModel.getStateModel(isRequester);
            if (stateModel) {
                // It is a valid state model
                // Now is this a valid action for this state
                isValid = (AvailableAction.countByModelAndFromStateAndActionCode(stateModel, status, action) == 1);
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
        // Get gold of the state model
        StateModel stateModel = StateModel.getStateModel(isRequester);

        // Determine the bean name, if we had a separate action table we could store it as a transient against that
        String beanName = "action" + stateModel.shortcode.capitalize() + actionCode.capitalize() + "Service";

        // Get hold of the bean and store it in our map, if we previously havn't been through here
        if (serviceActions[beanName] == null) {
            // Now setup the link to the service action that actually does the work
            try {
                serviceActions[beanName] = Holders.grailsApplication.mainContext.getBean(beanName);
            } catch (Exception e) {
                log.error("Unable to locate action bean: " + beanName);
            }
        }
        return(serviceActions[beanName]);
    }

    /**
     * Performs the desired action against the supplied request
     * @param action the action to be performed
     * @param request the request the action is to be performed against
     * @param parameters any parameters that may be required for the action
     * @return An ActionResultDetails that contains the result of performing the action
     */
    ActionResultDetails performAction(String action, PatronRequest request, Object parameters) {
        ActionResultDetails resultDetails = new ActionResultDetails();
        Status currentState = request.state;

        // Default a few fields
        resultDetails.newStatus = currentState;
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
        Status newStatus = statusService.lookupStatus(request, action, resultDetails.qualifier, resultDetails.result == ActionResult.SUCCESS, true);
        String newStatusId = newStatus.id;

        // if the new status is not the same as the hard coded state then we are either missing a qualifier or an actionEventResult record
        if (!resultDetails.newStatus.id.equals(newStatusId)) {
            String message = 'Hard coded status (' + resultDetails.newStatus.code +
                             ') is not the same as the calculated status (' + newStatus.code +
                              ') for action ' + action +
                              ' with result ' + resultDetails.result.toString() +
                              ' and qualifier ' + ((resultDetails.qualifier == null) ? 'null' : resultDetails.qualifier);
            log.error(message);
            reshareApplicationEventHandlerService.auditEntry(
                request,
                currentState,
                currentState,
                message,
                null);
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
            resultDetails.auditData);

        // Finally Save the request
        request.save(flush:true, failOnError:true);

        // Return the result to the caller
        return(resultDetails);
    }
}
