package org.olf.rs.logging;

/**
 * Interface for recording what we do when we make an iso18626 call
 * @author Chas
 *
 */
public interface IIso18626LogDetails extends IBaseAuditDetails {

    /**
     * Records the request information sent in an iso18626 call
     * @param requestEndpoint The endpoint we are sending the message to
     * @param requestBody The request body we sent
     */
    void request(String requestEndpoint, String requestBody);

    /**
     * Records the response we received from an iso18626 call
     * @param responseStatus The response status
     * @param responseBody The body of the response
     */
    void response(String responseStatus, String responseBody);
}
