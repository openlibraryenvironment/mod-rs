package org.olf.rs.logging;

import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;

/**
 * Records the details of an ncip message
 * @author Chas
 *
 */
public class NcipLogDetails extends BaseAuditDetails implements INcipLogDetails {

    public NcipLogDetails() {
        this.protocolType = ProtocolType.NCIP;
        this.protocolMethod = ProtocolMethod.POST;
    }

    @Override
    public void result(String requestEndpoint, String requestBody, String responseStatus, String responseBody) {
        url = requestEndpoint;
        this.requestBody = requestBody;
        this.responseStatus = responseStatus;
        this.responseBody = responseBody;
    }
}
