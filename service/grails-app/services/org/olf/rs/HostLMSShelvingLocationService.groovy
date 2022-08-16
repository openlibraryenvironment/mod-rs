package org.olf.rs;

/**
 * Perform any services required by the HostLMSShelvingLocation domain
 *
 */
public class HostLMSShelvingLocationService extends GenericCodeNameService<HostLMSShelvingLocation> {

    public HostLMSShelvingLocationService() {
        super(HostLMSShelvingLocation);
    }

    /**
     * Given a code,  name and supply preference create a new HostLMSShelvingLocation record
     * @param code The code for the location
     * @param name The name for the location
     * @param supplyPreference The supply preference defaults to 0
     * @return The record that represents this code and name
     */
    public HostLMSShelvingLocation ensureExists(String code, String name, long supplyPreference = 0) {
        log.debug('Entering HostLMSShelvingLocationService::ensureExists(' + code + ', ' + name + ', ' + supplyPreference.toString()+ ');');

        HostLMSShelvingLocation loc = ensureExists(code, name, { instance, newRecord ->
            instance.supplyPreference = supplyPreference;
        });

        log.debug('Exiting HostLMSShelvingLocationService::ensureActive');
        return(loc);
    }
}
