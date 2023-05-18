package org.olf.rs.logging;

import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;
import org.olf.rs.constants.AuditLog;

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

    /** The body of the request that was sent */
    protected String requestBody;

    /** The response status of the message */
    protected String responseStatus;

    /** The response body that was received */
    protected String responseBody;

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
    public String getRequestBody() {
        return(requestBody);
    }

    @Override
    public String getResponseStatus() {
        return(responseStatus);
    }

    @Override
    public String getResponseBody() {
        return(responseBody);
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

    @Override
    public Map toMap() {
        Map result = [ : ];
        result[AuditLog.AUDIT_PROTOCOL_TYPE] = getProtocolType().toString();
        result[AuditLog.AUDIT_PROTOCOL_METHOD] = getProtocolMethod().toString();
        result[AuditLog.AUDIT_URL] = getURL();
        result[AuditLog.AUDIT_REQUEST_BODY] = getRequestBody();
        result[AuditLog.AUDIT_RESPONSE_STATUS] = getResponseStatus();
        result[AuditLog.AUDIT_RESPONSE_BODY] = getResponseBody();
        result[AuditLog.AUDIT_DURATION] = duration();
        return(result);
    }
}
