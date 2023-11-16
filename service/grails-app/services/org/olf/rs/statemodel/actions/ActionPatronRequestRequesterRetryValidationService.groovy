package org.olf.rs.statemodel.actions


import grails.gorm.multitenancy.Tenants
import org.olf.rs.EventPublicationService
import org.olf.rs.PatronRequest
import org.olf.rs.shared.TenantSymbolMapping;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions
import org.olf.rs.statemodel.Events;


/**
 * Action to send a request back to the "new request" state to be re-tried
 */

public class ActionPatronRequestRequesterRetryValidationService extends AbstractAction {


    EventPublicationService eventPublicationService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_RETRY_VALIDATION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        actionResultDetails.responseResult.status = true;

        String tenant = Tenants.currentId();

        //Send a new patron request event
        eventPublicationService.publishAsJSON(
                "${tenant}_PatronRequestEvents",
                null,
                [
                        event: Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION,
                        tenant: tenant,
                        oid: 'org.olf.rs.PatronRequest:' + request.id,
                        payload: [
                                id: request.id,
                                title: request.title
                        ]
                ]
        );


        return(actionResultDetails);
    }
}
