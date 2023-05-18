package org.olf.rs.logging;

/**
 * Interface for recording what we do when we make an NCIP call
 * @author Chas
 *
 */
public interface INcipLogDetails extends IBaseAuditDetails {

    /**
     * As we are calling an NCIP library this allows us to gather everything up in one place with regards to an ncip call
     * @param requestEndpoint The endpoint for the call
     * @param requestBody The body of the request
     * @param responseStatus The status contained in the response message
     * @param responseBody The body of the response
     */
    void result(String requestEndpoint, String requestBody, String responseStatus, String responseBody);
}
