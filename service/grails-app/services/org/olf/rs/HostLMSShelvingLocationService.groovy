package org.olf.rs;

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
    public HostLMSShelvingLocation ensureExists(String code, String name, long supplyPreference = 0) {
        log.debug('Entering HostLMSShelvingLocationService::ensureExists(' + code + ', ' + name + ', ' + supplyPreference.toString()+ ');');
        HostLMSShelvingLocation loc;

        // We will need to create a separate transaction
        HostLMSShelvingLocation.withNewSession { session ->
            try {
                // Start a new transaction
                HostLMSShelvingLocation.withNewTransaction {

                    loc = HostLMSShelvingLocation.findByCodeOrName(code, name);
                    if (loc == null) {
                        // Dosn't exist so we need to create it
                        loc = new HostLMSShelvingLocation( code: code, name: name, supplyPreference: supplyPreference);
                        loc.save(flush:true, failOnError:true);
                        patronNoticeService.triggerNotices(loc);
                    }
                }
            } catch (Exception e) {
                log.error("Exception thrown while creating HostLMSShelvingLocation: " + code, e);
            }
        }

        // As the session no longer exists we need to attach it to the current session
        if (loc != null) {
            // Sometimes, the id already exists in the cache as a different object, so you get the exception DuplicateKeyException
            // So rather than attach, we will do a lookup
            loc = HostLMSShelvingLocation.get(loc.id);
        }
        log.debug('Exiting HostLMSShelvingLocationService::ensureActive');
        return(loc);
    }
}
