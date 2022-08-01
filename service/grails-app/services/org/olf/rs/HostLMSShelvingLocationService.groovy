package org.olf.rs;

import org.hibernate.Transaction;

/**
 * Perform any services required by the HostLMSShelvingLocation domain
 *
 */
public class HostLMSShelvingLocationService {

    PatronNoticeService patronNoticeService;

    /**
     * Given a code,  name and supply preference create a new HostLMSShelvingLocation record
     * @param code The code for the location
     * @param name The name for the location
     * @param supplyPreference The supply preference defaults to 0
     * @return The record that represents this code and name
     */
    public HostLMSShelvingLocation create(String code, String name, long supplyPreference = 0) {
        HostLMSShelvingLocation loc;

        // We will need to create a separate transaction
        HostLMSShelvingLocation.withNewSession { session ->
            try {
                // Start a new transaction
                Transaction transaction = session.beginTransaction();

                loc = new HostLMSShelvingLocation( code: code, name: name, supplyPreference: supplyPreference);
                loc.save(flush:true, failOnError:true);
                patronNoticeService.triggerNotices(loc);
            } catch (Exception e) {
                log.error("Exception thrown while creating HostLMSShelvingLocation: " + code, e);
            }
        }
        return(loc);
    }
}
