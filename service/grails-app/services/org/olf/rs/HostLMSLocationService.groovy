package org.olf.rs;

/**
 * Perform any services required by the HostLMSLocation domain
 *
 */
public class HostLMSLocationService {

    PatronNoticeService patronNoticeService;

    /**
     * Given a code and name looks to see if the HostLMSLocation record already exists and if not creates it
     * If it it does exist, it ensures that it is active
     * @param code The code for the location
     * @param name The name for the location
     * @return The record that represents this code and name
     */
    public HostLMSLocation ensureActive(String code, String name) {
        log.debug('Entering HostLMSLocationService::ensureActive(' + code + ', ' + name + ');');
        HostLMSLocation loc;

        // We will need to create a separate transaction
        HostLMSLocation.withNewSession { session ->
            try {
                // Start a new transaction
                HostLMSLocation.withNewTransaction {
                    // Lookup the location
                    loc = HostLMSLocation.findByCodeOrName(code, name);

                    // Did we find a location
                    if (loc == null) {
                        // We do not so create a new one
                        loc = new HostLMSLocation(
                            code : code,
                            name : name,
                            icalRrule :'RRULE:FREQ=MINUTELY;INTERVAL=10;WKST=MO'
                        );
                        loc.save(flush : true, failOnError : true);

                        // We have created a new record, so trigger a notice
                        patronNoticeService.triggerNotices(loc);
                    }  else if (loc.hidden == true) {
                        // It is hidden, so unhide it
                        loc.hidden = false;
                        loc.save(flush : true, failOnError : true);
                    }
                }
            } catch(Exception e) {
                log.error("Exception thrown while creating / updating HostLMSLocation with code: " + code, e);
            }
        }

        // As the session no longer exists we need to attach it to the current session
        if (loc != null) {
            // Sometimes, the id already exists in the cache as a different object, so you get the exception DuplicateKeyException
            // So rather than attach, we will do a lookup
            loc = HostLMSLocation.get(loc.id);
        }
        log.debug('Exiting HostLMSLocationService::ensureActive');
        return(loc);
    }
}
