package org.olf.rs.logging;

import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;

/**
 * Holds the raw details required for auditing of the protocol messages
 * @author Chas
 *
 */
public class BaseAuditDetails implements IBaseAuditDetails {

    /** The protocol type that the log details is for */
    protected ProtocolType protocolType;

    /** The protocol type that the log details is for */
    protected ProtocolMethod protocolMethod;

    /** The url that was used to obtain the information */
    protected String url;

    /** The time the process using this object was started */
    private long startTime;

    public BaseAuditDetails() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public String getURL() {
        return(url);
    }

    @Override
    public ProtocolMethod getProtocolMethod() {
        return(protocolMethod);
    }

    @Override
    public ProtocolType getProtocolType() {
        return(protocolType);
    }

    @Override
    public long duration() {
        return(System.currentTimeMillis() - startTime);
    }
}
