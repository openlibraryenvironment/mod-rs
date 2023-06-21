package org.olf.rs.iso18626;

/**
 * Holds the definitions of all the specials that we have incorporated into the note field
 * Once extensions are added this can be removed
 * @author Chas
 *
 */
public class ReasonForMessage {

    // The reasons for a supplier message
    public static final String MESSAGE_REASON_CANCEL_RESPONSE         = 'CancelResponse';
    public static final String MESSAGE_REASON_NOTIFICATION            = 'Notification';
    public static final String MESSAGE_REASON_RENEW_RESPONSE          = 'RenewResponse';
    public static final String MESSAGE_REASON_REQUEST_RESPONSE        = 'RequestResponse';
    public static final String MESSAGE_REASON_STATUS_CHANGE           = 'StatusChange';
    public static final String MESSAGE_REASON_STATUS_REQUEST_RESPONSE = 'StatusRequestResponse';
}
