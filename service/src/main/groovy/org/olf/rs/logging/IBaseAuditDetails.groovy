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
     * The time taken between this called eing made and the object being allocated
     * @return The time taken
     */
    long duration();
}
