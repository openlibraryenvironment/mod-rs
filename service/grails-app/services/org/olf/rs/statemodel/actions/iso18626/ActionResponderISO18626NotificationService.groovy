package org.olf.rs.statemodel.actions.iso18626;

import java.time.LocalDate;

import org.olf.rs.PatronRequest;
import org.olf.rs.iso18626.NoteSpecials;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionResultDetails;
import org.olf.rs.statemodel.StatusStage;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

/**
 * Action that deals with the ISO18626 Notification message
 * @author Chas
 *
 */
public class ActionResponderISO18626NotificationService extends ActionISO18626ResponderService {

    // These are all the fields that can be updated through the note
    private static final List updateAbleFields = [
        [ field: "author", notePrefix: NoteSpecials.UPDATED_FIELD_AUTHOR_PREFIX, isDate: false ],
        [ field: "edition", notePrefix: NoteSpecials.UPDATED_FIELD_EDITION_PREFIX, isDate: false ],
        [ field: "isbn", notePrefix: NoteSpecials.UPDATED_FIELD_ISBN_PREFIX, isDate: false ],
        [ field: "issn", notePrefix: NoteSpecials.UPDATED_FIELD_ISSN_PREFIX, isDate: false ],
        [ field: "neededBy", notePrefix: NoteSpecials.UPDATED_FIELD_NEEDED_BY_PREFIX, isDate: true ],
        [ field: "oclcNumber", notePrefix: NoteSpecials.UPDATED_FIELD_OCLC_NUMBER_PREFIX, isDate: false ],
        [ field: "patronNote", notePrefix: NoteSpecials.UPDATED_FIELD_PATRON_NOTE_PREFIX, isDate: false ],
        [ field: "pickupLocation", notePrefix: NoteSpecials.UPDATED_FIELD_PICKUP_LOCATION_PREFIX, isDate: false],
        [ field: "placeOfPublication", notePrefix: NoteSpecials.UPDATED_FIELD_PLACE_OF_PUBLICATION_PREFIX, isDate: false ],
        [ field: "publicationDate", notePrefix: NoteSpecials.UPDATED_FIELD_PUBLICATION_DATE_PREFIX, isDate: false ],
        [ field: "publisher", notePrefix: NoteSpecials.UPDATED_FIELD_PUBLISHER_PREFIX, isDate: false ],
        [ field: "systemInstanceIdentifier", notePrefix: NoteSpecials.UPDATED_FIELD_SYSTEM_INSTANCE_IDENTIFIER_PREFIX, isDate: false ],
        [ field: "title", notePrefix: NoteSpecials.UPDATED_FIELD_TITLE_PREFIX, isDate: false ],
        [ field: "volume", notePrefix: NoteSpecials.UPDATED_FIELD_VOLUME_PREFIX, isDate: false ]
    ];

    @Override
    String name() {
        return(EventISO18626IncomingAbstractService.ACTION_NOTIFICATION);
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {
        /* If the message is preceded by #ReShareLoanConditionAgreeResponse#
         * then we'll need to check whether or not we need to change state.
         */
        Map messageData = parameters.activeSection;
        String note = messageData?.note;
        if (note != null) {
            // Check for the reshare special of loan conditions agreed
            if (note.startsWith(NoteSpecials.AGREE_LOAN_CONDITION)) {
                // First check we're in the state where we need to change states, otherwise we just ignore this and treat as a regular message, albeit with warning
                if (request.state.stage == StatusStage.ACTIVE_PENDING_CONDITIONAL_ANSWER) {
                    // We need to change the state to the saved state
                    actionResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_CONDITIONS_AGREED;
                    actionResultDetails.auditMessage = 'Requester agreed to loan conditions, moving request forward';

                    // Make all conditions agreed
                    reshareApplicationEventHandlerService.markAllLoanConditionsAccepted(request);
                } else {
                    // Loan conditions were already marked as agreed
                    actionResultDetails.auditMessage = 'Requester agreed to loan conditions, no action required on supplier side';
                }

                // Remove the keyword
                note = note.replace(NoteSpecials.AGREE_LOAN_CONDITION, "");
            } else {
                // Do we have any fields that need updating
                StringBuffer auditMessage = new StringBuffer();
                updateAbleFields.each() { fieldDetails ->
                    Map extractedFieldResult = extractFieldFromNote(note, fieldDetails.notePrefix);
                    if (extractedFieldResult.data != null) {
                        boolean validValue = true;
                        Object value = extractedFieldResult.data;

                        // Are we dealing with a date
                        if (fieldDetails.isDate) {
                            // Will need converting to a string
                            try {
                                // Convert the value
                                value = LocalDate.parse(extractedFieldResult.data);
                            } catch (Exception e) {
                                log.error("Failed to parse date field ${fieldDetails.field} with value ${extractedFieldResult.data}", e);
                                validValue = false;
                            }
                        }

                        // Can we update the field
                        if (validValue) {
                            request[fieldDetails.field] = value;
                            auditMessage.append("${fieldDetails.field} updated to \"" + extractedFieldResult.data + "\".")
                        }

                        // Reset the message note
                        note = extractedFieldResult.note;
                    }
                }

                // Did we update at least 1 field
                if (auditMessage.length() > 0) {
                    // Set the audit message to what we have updated
                    actionResultDetails.auditMessage = auditMessage.toString();
                } else {
                    // Nothing updated so we just trat it as a message
                    actionResultDetails.auditMessage = "Notification message received from requesting agency: ${note}";
                }
            }
        }

        // If we were successful, call the base class
        if (actionResultDetails.result == ActionResult.SUCCESS) {
            // We call process note now as we may have manipulated the note
            // Unfortunately we cannot just replace the note field on parameters.activeSection
            actionResultDetails = processNote(request, parameters, note, actionResultDetails);
        }

        // Now return the result to the caller
        return(actionResultDetails);
    }

    /**
     * Extracts a field from the note
     * @param note The note that has been sent
     * @param field The field to be extracted
     * @return A map containing the extracted string and the note without this special field
     */
    private Map extractFieldFromNote(String note, String fieldPrefix) {
        Map result = [ note : note ];

        // Lets see if we can find this label
        int fieldStart = note.indexOf(fieldPrefix);
        if (fieldStart > -1) {
            // We have found this label, so move the start to the end
            int dataStart = fieldStart + fieldPrefix.length();

            // Let us find the end of the data
            int fieldEnd = note.indexOf(NoteSpecials.SPECIAL_WRAPPER, dataStart);

            // Have we found a field end
            if (fieldEnd > -1) {
                // We have, do not forget to exclude the terminator
                result.data = note.substring(dataStart, fieldEnd);

                // Now need to replace this section of the note
                StringBuilder updatedNote = new StringBuilder(note);
                updatedNote.delete(fieldStart, fieldEnd + 1);
                result.note = updatedNote.toString();
            }
        }

        // Return the result to the caller
        return(result);
    }
}
