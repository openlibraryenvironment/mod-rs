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

    private static final List updateAbleFields = [
        [ field: "author", notePrefix: NoteSpecials.UPDATED_FIELD_AUTHOR_PREFIX, isDate: false ],
        [ field: "edition", notePrefix: NoteSpecials.UPDATED_FIELD_EDITION_PREFIX, isDate: false ],
        [ field: "isbn", notePrefix: NoteSpecials.UPDATED_FIELD_ISBN_PREFIX, isDate: false ],
        [ field: "issn", notePrefix: NoteSpecials.UPDATED_FIELD_ISSN_PREFIX, isDate: false ],
        [ field: "neededBy", notePrefix: NoteSpecials.UPDATED_FIELD_NEEDED_BY_PREFIX, isDate: true ],
        [ field: "oclcNumber", notePrefix: NoteSpecials.UPDATED_FIELD_OCLC_NUMBER_PREFIX, isDate: false ],
        [ field: "patronNote", notePrefix: NoteSpecials.UPDATED_FIELD_PATRON_NOTE_PREFIX, isDate: false ],
        // TODO temporary fix pending PR-1530
        [ field: "patronIdentifier", notePrefix: '', isDate: false ],
        [ field: "pickupLocationSlug", notePrefix: NoteSpecials.UPDATED_FIELD_PICKUP_LOCATION_PREFIX, isDate: false, noteField: "pickupLocation", doPickupCheck: true ],
        [ field: "placeOfPublication", notePrefix: NoteSpecials.UPDATED_FIELD_PLACE_OF_PUBLICATION_PREFIX, isDate: false ],
        [ field: "publicationDate", notePrefix: NoteSpecials.UPDATED_FIELD_PUBLICATION_DATE_PREFIX, isDate: false ],
        [ field: "publisher", notePrefix: NoteSpecials.UPDATED_FIELD_PUBLISHER_PREFIX, isDate: false ],
        [ field: "systemInstanceIdentifier", notePrefix: NoteSpecials.UPDATED_FIELD_SYSTEM_INSTANCE_IDENTIFIER_PREFIX, isDate: false ],
        [ field: "title", notePrefix: NoteSpecials.UPDATED_FIELD_TITLE_PREFIX, isDate: false ],
        [ field: "volume", notePrefix: NoteSpecials.UPDATED_FIELD_VOLUME_PREFIX, isDate: false ]
    ];

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
        StringBuffer noteToSend = new StringBuffer();
        updateAbleFields.each() { fieldDetails ->
            // Update the field
            if (updateField(request, parameters, fieldDetails.field, auditMessage, fieldDetails.isDate)) {
                // It has changed
                if (fieldDetails.doPickupCheck) {
                    // Perform the appropriate checks on the pickup location
                    pickupLocationService.check(request);
                }

                // Update the note
                String fieldToSend = (fieldDetails.noteField == null ? fieldDetails.field : fieldDetails.noteField);
                noteToSend.append(fieldDetails.notePrefix);
                noteToSend.append(request[fieldToSend]);
                noteToSend.append(NoteSpecials.SPECIAL_WRAPPER);
            }
        }

        // If anything has changed we may need to send a message
        if (noteToSend.length() > 0) {
            // We need to inform the supplier, but only if it is active and not shipped
            if ((request.state.stage == StatusStage.ACTIVE) || (request.state.stage == StatusStage.ACTIVE_PENDING_CONDITIONAL_ANSWER)) {
                // It is active and not been shipped
                reshareActionService.sendMessage(request, [ note : noteToSend.toString() ], actionResultDetails);
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
