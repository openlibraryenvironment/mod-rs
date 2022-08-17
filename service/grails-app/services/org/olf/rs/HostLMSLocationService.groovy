package org.olf.rs;

/**
 * Perform any services required by the HostLMSLocation domain
 *
 */
public class HostLMSLocationService extends GenericCodeNameService<HostLMSLocation> {

    public HostLMSLocationService() {
        super(HostLMSLocation, { instance ->
            return((instance.hidden != null) && (instance.hidden == true));
        });
    }

    /**
     * Given a code and name looks to see if the HostLMSLocation record already exists and if not creates it
     * If it it does exist, it ensures that it is active
     * @param code The code for the location
     * @param name The name for the location
     * @return The record that represents this code and name
     */
    public HostLMSLocation ensureActive(String code, String name) {
        log.debug('Entering HostLMSLocationService::ensureActive(' + code + ', ' + name + ');');

        HostLMSLocation loc = ensureExists(code, name, { instance, newRecord ->
            if (newRecord) {
                instance.icalRrule = 'RRULE:FREQ=MINUTELY;INTERVAL=10;WKST=MO';
            }

            // Ensure it is not hidden
            instance.hidden = false;
        });

        log.debug('Exiting HostLMSLocationService::ensureActive');
        return(loc);
    }
}
