package org.olf.rs

enum NetworkStatus {

    /** There was an error sending the message and the maximum number of retries was reached */
    Error,

    /** Nothing needing to happen on the network at the moment */
    Idle,

    /** An error previously occurred and we are still within our retry limit */
    Retry,

    /** Message has been sent */
    Sent,

    /** There was timeout error when we send the message */
    Timeout,

    /** Message is waiting to be sent */
    Waiting
}
