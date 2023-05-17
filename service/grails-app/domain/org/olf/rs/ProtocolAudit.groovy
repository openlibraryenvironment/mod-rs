package org.olf.rs

import grails.gorm.MultiTenant;

class ProtocolAudit implements MultiTenant<ProtocolAudit> {

    // internal ID of the audit record
    String id;

    /** The date time the protocol action was processed */
    Date dateCreated;

    /** The request this audit record belongs to */
    PatronRequest patronRequest;

    /** The protocol that this audit is for */
    ProtocolType protocolType;

    /** The method that was used for the protocol */
    ProtocolMethod protocolMethod;

    /** The url that was used to carry out the message */
    String url;

    /** The body of the message that was sent */
    String requestBody;

    /** The response status received in sending this message */
    String responseStatus;

    /** The response body that was received */
    String responseBody;

    /** How long it took to perform this message */
    Long duration;

    /** The request this audit record belongs to */
    static belongsTo = [patronRequest : PatronRequest]

    static constraints = {
           dateCreated (nullable : true) // Because this isn't set until after validation!
              duration (nullable : false)
         patronRequest (nullable : false)
          protocolType (nullable : false)
        protocolMethod (nullable : false)
                   url (nullable : false)
           requestBody (nullable : true)
        responseStatus (nullable : true)
          responseBody (nullable : true)
    }

    static mapping = {
                    id column : 'pa_id', generator : 'uuid2', length : 36
           dateCreated column : 'pa_date_created'
              duration column : 'pa_duration'
         patronRequest column : 'pa_patron_request'
          protocolType column : 'pa_protocol_type', length : 30
        protocolMethod column : 'pa_protocol_method', length : 30
                   url column : 'pa_uri'
           requestBody column : 'pa_request_body', type : 'text'
        responseStatus column : 'pa_response_status', length : 30
          responseBody column : 'pa_response_body', type : 'text'
    }
}
