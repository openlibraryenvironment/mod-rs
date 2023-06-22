package org.olf.rs.iso18626;

/**
 * Defines the values that can go in the ISO18626 reasonForMessage field
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
