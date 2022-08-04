package org.olf.rs

import com.k_int.web.toolkit.refdata.RefdataCategory
import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.MultiTenant

class ProtocolReferenceDataValue extends RefdataValue implements MultiTenant<ProtocolReferenceDataValue>{

	static public final String CATEGORY_PUBLICATION_TYPE = "request.publicationType";
	static public final String CATEGORY_SERVICE_LEVEL    = "request.serviceLevel";
	static public final String CATEGORY_SERVICE_TYPE     = "request.serviceType";

	static hasMany = [protocolConversions : ProtocolConversion];

    /**
     * Looks to see if the supplied value is a valid publication type and if so returns the ProtocolReferenceDataValue for it
     * @param value The publication type to lookup
     * @return The ProtocolReferenceDataValue that represents the publication type being looked up, which may be null if it is not found
     */
    static public ProtocolReferenceDataValue lookupPublicationType(String value) {
        return(lookup(CATEGORY_PUBLICATION_TYPE, value));
    }

    /**
     * Looks to see if the supplied value is a valid value for the supplied category type and if so returns the ProtocolReferenceDataValue for it
     * @param categoryTpe The reference type to lookup
     * @param value The value to lookup
     * @return The ProtocolReferenceDataValue that represents the value eing looked up, which may be null if it is not found
     */
    static public ProtocolReferenceDataValue lookup(String categoryName, String value) {
        ProtocolReferenceDataValue reference = null;

        // Cannot do anything without a catery name or value
        if ((categoryName != null) && (value != null)) {
            // First of all, look for the category
            RefdataCategory category = RefdataCategory.findByDesc(categoryName);

            // Did we find a category
            if (category != null) {
                // Good start we have a category, now normalise the value
                final String normalisedValue = normValue(value);

                // We can now lookup the refereence object for this category
                reference = findByOwnerAndValue(category, normalisedValue);
            }
        }

        // Return the found reference to the caller
        return(reference);
    }
}
