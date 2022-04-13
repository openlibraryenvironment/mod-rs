package org.olf.rs

enum ProtocolResultStatus {

    /** There was an error sending the message */
    Error,

    /** The message was sent */
    Sent,

    /** A timeout occurred while sending the message */
    Timeout
}
