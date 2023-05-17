package org.olf.rs

/**
 * The initial values are based on the HTTP protocol, it is not a problem to add methods for other protocols as well
 * @author Chas
 *
 */
enum ProtocolMethod {

    /** We are requesting that a deletion be carried out */
    DELETE,

    /** We are requesting information */
    GET,

    /** We are creating a new item or updating an item */
    POST,

    /** We are replacing an existing item with that supplied in the body of the message */
    PUT
}
