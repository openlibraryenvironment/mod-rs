package org.olf.rs;

import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

import grails.util.Holders;

/**
 * Houses all the functionality to do with fetching reference data
 * @author Chas
 *
 */
public class ReferenceDataService {

    /**
     * Retrieves the reference data for a the specified category and value
     * @param categoryValue The category that the reference value belongs to
     * @param refValue The reference value that needs to be retrieved for this category
     * @return The RefdataValue record for the supplied category and reference values
     */
    public RefdataValue lookup(String categoryValue, String refValue) {
        RefdataValue result = null;

        // Cannot do anything without a category value or reference value
        if ((categoryValue != null) && (refValue != null))
        {
            // Find the category
            RefdataCategory category = RefdataCategory.findByDesc(categoryValue);

            // If we have found the category lookup the ref data value
            if (category != null) {
                // Now we can lookup the value
                final String normRefValue = RefdataValue.normValue(refValue)

                result = RefdataValue.findByOwnerAndValue(category, normRefValue);
            }
        }

        // Return the result to the caller
        return(result);
    }

    public static ReferenceDataService getInstance() {
        return(Holders.grailsApplication.mainContext.getBean("referenceDataService"));
    }
}
