package org.olf.rs

import grails.gorm.MultiTenant

/**
 * This is so we can associate identifiers with the request, these identifiers are not sent onto the responder at the moment
 * @author Chas
 *
 */
class RequestIdentifier implements MultiTenant<RequestIdentifier> {

    /** The identifier for this record */
    String id;

    /** The type of identifier we are dealing with **/
    String identifierType;

    /** The identifier */
    String identifier;

    /** The request this identifier belongs to */
    PatronRequest patronRequest;

    static constraints = {
        identifier (nullable: false, blank: false)
        identifierType (nullable: false, blank: false)
        patronRequest (nullable: false)
    }

    static mapping = {
                    id column : 'ri_id', generator: 'uuid2', length:36
               version column : 'ri_version'
            identifier column : 'ri_identifier'
        identifierType column : 'ri_identifier_type'
         patronRequest column : 'ri_patron_request'
    }

    // Before we insert or update, uppercase the type if it it does not begin with http
    def beforeInsert() {
        if (!identifierType.startsWith('http')) {
            identifierType = identifierType.toUpperCase();
        }
    }

    def beforeUpdate() {
        // Just call beforeInsert
        beforeInsert();
    }
}
