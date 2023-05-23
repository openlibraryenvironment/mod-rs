package org.olf.rs.logging;

/**
 * Implements the IIso18626LogDetails interface by not recording any details
 * @author Chas
 *
 */
public class DoNothingIso18626LogDetails extends BaseAuditDetails implements IIso18626LogDetails {

    @Override
    public void request(String requestEndpoint, String requestBody) {
    }

    @Override
    public void response(String responseStatus, String responseBody) {
    }
}
