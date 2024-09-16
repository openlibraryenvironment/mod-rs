package org.olf.rs.statemodel.actions

import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AbstractAction
import org.olf.rs.statemodel.ActionResultDetails
import org.olf.rs.statemodel.Actions

import static org.olf.rs.RerequestService.preserveFields;

/**
 * This action is performed when a request has terminated in Cancelled or End of Rota
 * and the requester wishes to edit it and submit the request again
 *
 */
public class ActionPatronRequestRerequestService extends AbstractAction {

    //static List<String> preserveFields = ['author','edition','isbn','isRequester','issn','issue','neededBy','numberOfPages','oclcNumber','patronEmail','patronGivenName','patronIdentifier','patronNote','patronReference','patronSurname','patronType','pickLocation','pickupLocationSlug','placeOfPublication','publicationDate','publisher','requestingInstitutionSymbol','sponsoringBody','startPage','stateModel','subtitle','systemInstanceIdentifier','title','volume'];

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_REREQUEST);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        def newParams = parameters.subMap(preserveFields);
        def newReq = new PatronRequest(newParams);
        request.succeededBy = newReq;
        request.save();
        newReq.precededBy = request;
        newReq.save();
        actionResultDetails.responseResult.status = true;

        return(actionResultDetails);
    }
}
