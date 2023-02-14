package org.olf.rs.statemodel.actions;

import java.time.LocalDate;

import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.iso18626.NoteSpecials;
import org.olf.rs.patronRequest.PickupLocationService;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StatusStage;

/**
 * Abstract action class that deals with a cancel being requested
 * @author Chas
 *
 */
public class ActionPatronRequestEditService extends AbstractAction {

    PickupLocationService pickupLocationService;
    ReshareActionService reshareActionService;

    @Override
    String name() {
        return(Actions.ACTION_REQUESTER_EDIT);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        // We allow editing of the following fields
        // 1. Date needed
        // 2. Volume
        // 3. Patron note
        // 4. System Identifier
        // 5. Title
        // 6. Author
        // 7. Date of publication
        // 8. Publisher
        // 9. Edition
        // 10. Place of publication
        // 11. ISBN
        // 12. ISSN
        // 13. OCLC number
        // 14. Pickup location
        StringBuffer auditMessage = new StringBuffer("Record has been edited:");
        updateField(request, parameters, "neededBy", auditMessage, true);
        updateField(request, parameters, "volume", auditMessage);
        updateField(request, parameters, "patronNote", auditMessage);
        updateField(request, parameters, "systemInstanceIdentifier", auditMessage);
        updateField(request, parameters, "title", auditMessage);
        updateField(request, parameters, "author", auditMessage);
        updateField(request, parameters, "publicationDate", auditMessage);
        updateField(request, parameters, "publisher", auditMessage);
        updateField(request, parameters, "edition", auditMessage);
        updateField(request, parameters, "placeOfPublication", auditMessage);
        updateField(request, parameters, "isbn", auditMessage);
        updateField(request, parameters, "issn", auditMessage);
        updateField(request, parameters, "oclcNumber", auditMessage);

        // We need to do additional stuff with pickup location
        if (updateField(request, parameters, "pickupLocationSlug", auditMessage)) {
            // Perform the appropriate checks on the pickup location
            pickupLocationService.check(request);

            // Pickup location has changed, so we need to inform the supplier, but only if it is active and not shipped
            if ((request.state.stage == StatusStage.ACTIVE) || (request.state.stage == StatusStage.ACTIVE_PENDING_CONDITIONAL_ANSWER)) {
                // It is active and not been shipped
                reshareActionService.sendMessage(request, [ note : (NoteSpecials.UPDATED_FIELD_PICKUP_LOCATION_PREFIX + request.pickupLocation + NoteSpecials.SPECIAL_WRAPPER)], actionResultDetails);
            }
        }

        // Status stays the same, we are not coming through the normal route so we need to override
        actionResultDetails.overrideStatus = request.state;

        // Finally set the audit message
        actionResultDetails.auditMessage = auditMessage.toString();

        // We do not want any of the following being stored in the audit trail
        // The lists
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

        return(actionResultDetails);
    }

    /**
     *
     * @param originalRecord The original request record that will get updated
     * @param newRecord The updated request record that we want to take values from
     * @param field The field that we want to update it
     * @param updateMessage The string buffer that we use to build up the audit messsage of what has been changed tree
     * @param isDate Is this a LocalDate field
     * @return true if the field was updated otherwise false
     */
    private boolean updateField(Object originalRecord, Object newRecord, String field, StringBuffer updateMessage, boolean isDate = false) {
        boolean updated = false;

        Object newValue = newRecord[field];
        // Do we need to turn a string value into a LocalDate
        if (isDate && (newValue != null)) {
            newValue = LocalDate.parse(newValue);
        }

        // Is the original value null
        if (originalRecord[field] == null) {
            // it is, so is the new value not null
            if (newValue != null) {
                // Value has been set
                updated = true;
            }
        } else if (newValue == null) {
            // Value has been cleared
            updated = true;
        } else if (newValue != originalRecord[field]) {
            // Value has changed
            updated = true;
        }

        // Has the field been updated
        if (updated) {
            // It has so add an appropriate message in the audit log
            updateMessage.append("\nField " + field +
                 " has changed from \"" + ((originalRecord[field] == null) ? "" : originalRecord[field]) + "\"" +
                 " to \"" + ((newRecord[field] == null) ? "" : newRecord[field]) + "\"");

             // Now update the field
             originalRecord[field] = newValue;
        }

        // Let them know if the field was updated or not
        return(updated);
    }
}
