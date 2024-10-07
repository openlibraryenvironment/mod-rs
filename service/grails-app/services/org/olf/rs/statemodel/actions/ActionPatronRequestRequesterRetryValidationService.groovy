package org.olf.rs.statemodel.actions


import grails.gorm.multitenancy.Tenants
import org.olf.rs.EventPublicationService
import org.olf.rs.PatronRequest
import org.olf.rs.iso18626.NoteSpecials
import org.olf.rs.shared.TenantSymbolMapping;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.patronRequest.PickupLocationService;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.Events
import org.olf.rs.statemodel.StateModel;


/**
 * Action to send a request back to the "new request" state to be re-tried
 */

public class ActionPatronRequestRequesterRetryValidationService extends ActionPatronRequestEditService {


    EventPublicationService eventPublicationService;
    PickupLocationService pickupLocationService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_RETRY_VALIDATION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request,
                                      Object parameters, ActionResultDetails actionResultDetails) {
        log.debug("Updating/Retrying request ${request.id} with parameters ${parameters}");
        StringBuffer auditMessage = new StringBuffer("Record has been edited:");
        boolean updateFields = !parameters.isEmpty();
        log.debug("updateFields is ${updateFields}");

        if (updateFields) {
            updateableFields.each() { fieldDetails ->
                log.debug("Updating field prior to retrying ${fieldDetails.field} in request ${request.id}");
                // Update the field
                if (updateField(request, parameters, fieldDetails, auditMessage)) {
                    // It has changed
                    if (fieldDetails.doPickupCheck) {
                        // Perform the appropriate checks on the pickup location
                        pickupLocationService.check(request);
                    }
                }
            }
            request.save();
        }



        actionResultDetails.responseResult.status = true;

        String tenant = Tenants.currentId();


        String newPatronEvent;
        if (request.stateModel.shortcode == StateModel.MODEL_NR_REQUESTER) {
            newPatronEvent = Events.EVENT_NONRETURNABLE_REQUESTER_NEW_PATRON_REQUEST_INDICATION;
        } else {
            newPatronEvent = Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION;
        }
        log.debug("For Patron statemodel ${request.stateModel.shortcode}, sending event ${newPatronEvent} ");
        //Send a new patron request event
        eventPublicationService.publishAsJSON(
                "${tenant}_PatronRequestEvents",
                null,
                [
                        event: newPatronEvent,
                        tenant: tenant,
                        oid: 'org.olf.rs.PatronRequest:' + request.id,
                        payload: [
                                id: request.id,
                                title: request.title
                        ]
                ]
        );

        if (updateFields) {
            actionResultDetails.auditMessage = auditMessage.toString();
            parameters.audit = null;
            parameters.batches = null;
            parameters.conditions = null;
            parameters.notifications = null;
            parameters.requestIdentifiers = null;
            parameters.rota = null;
            parameters.tags = null;
            parameters.volumes = null;

            // Specific fields
            parameters.lastProtocolData = null;
            parameters.resolvedPatron = null;
            parameters.resolvedPickupLocation = null;
            parameters.resolvedRequester = null;
            parameters.resolvedSupplier = null;
        }

        return(actionResultDetails);
    }
    
}
