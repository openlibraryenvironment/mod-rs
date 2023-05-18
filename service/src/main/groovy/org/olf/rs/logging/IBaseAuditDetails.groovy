package org.olf.rs.logging;

import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;

/**
 * The base level interface for auditing details
 * @author Chas
 *
 */
public interface IBaseAuditDetails {

    /**
     * Retrieves the last url used if multiple searches were executed
     * @return The url that was used to get hold of the holdings
     */
    String getURL();

    /**
     * Retrieves the protocol method used
     * @return The protocol method that was used
     */
    ProtocolMethod getProtocolMethod();

    /**
     * Retrieves the protocol type that was used
     * @return The protocol type that was being used
     */
    ProtocolType getProtocolType();

    /**
     * Retrieves the request body that was used as part of the message
     * @return The request body or null if there was not one
     */
    String getRequestBody();

    /**
     * The status supplied as part of the response
     * @return The response status
     */
    String getResponseStatus();

    /**
     * The body of the message that was returned
     * @return The message body that was received
     */
    String getResponseBody();

    /**
     * The time taken between this called eing made and the object being allocated
     * @return The time taken
     */
    long duration();

    /**
     * Converts the various details that have been recorded to a map
     * @return A map containing the logged details
     */
    Map toMap();
}
