package org.olf.rs.logging;

/**
 * Implements the INcipLogDetails interface by not recording any details
 * @author Chas
 *
 */
public class DoNothingNcipLogDetails extends BaseAuditDetails implements INcipLogDetails {

    @Override
    public void result(String requestEndpoint, String requestBody, String responseStatus, String responseBody) {
        // We do not want anything being recorded, so do nothing
    }
}
