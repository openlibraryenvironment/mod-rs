package org.olf.rs

enum ProtocolResultStatus {

    /** There was an error sending the message */
    Error,

    /** There was a protocol error */
    ProtocolError,

    /** There was a validation error */
    ValidationError,

    /** The message was sent */
    Sent,

    /** A timeout occurred while sending the message */
    Timeout
}
