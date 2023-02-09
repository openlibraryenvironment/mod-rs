package org.olf.rs.statemodel.actions;

import java.time.LocalDate;

import org.olf.rs.PatronRequest;
import org.olf.rs.patronRequest.PickupLocationService
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.Actions;

/**
 * Abstract action class that deals with a cancel being requested
 * @author Chas
 *
 */
public class ActionPatronRequestEditService extends AbstractAction {

    PickupLocationService pickupLocationService;

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
        }

        // Status stays the same, we are not coming through the normal route so we need to override
        actionResultDetails.overrideStatus = request.state;

        // Finally set the audit message
        actionResultDetails.auditMessage = auditMessage.toString();

        return(actionResultDetails);
    }

    /**
     *
     * @param originalRecord The original request record that will get updated
     * @param newRecord The updated request record that we want to take values from
     * @param field The field that we want to update it
     * @param updateMessage The string buffer that we use to build up the audit messsage of what has been changed tree
     * @param isDate Is this a LocalDate field
     */
    private void updateField(Object originalRecord, Object newRecord, String field, StringBuffer updateMessage, boolean isDate = false) {
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
                updateMessage.append("\nField " + field + " has changed from \"\" to \"" + newRecord[field] + "\"");
            }
        } else if (newValue == null) {
            // Value has been cleared
            updateMessage.append("\nField " + field + " has changed from \"" + originalRecord[field] + "\" to \"\"");
        } else if (newValue != originalRecord[field]) {
            // Value has changed
            updateMessage.append("\nField " + field + " has changed from \"" + originalRecord[field] + "\" to \"" + newRecord[field] + "\"");
        }

        // Just update the field
        originalRecord[field] = newValue;
    }
}
