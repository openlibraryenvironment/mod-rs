package org.olf.rs;

/**
 * Perform any services required by the HostLMSPatronProfile domain
 *
 */
public class HostLMSPatronProfileService {

    PatronNoticeService patronNoticeService;

    /**
     * Given a code and name looks to see if the HostLMSPatronProfile record already exists and if not creates it
     * If it it does exist, it ensures that it is active
     * @param code The code for the profile
     * @param name The name for the profile
     * @return The record that represents this code and name
     */
    public HostLMSPatronProfile ensureActive(String code, String name) {
        log.debug('Entering HostLMSPatronProfileService::ensureActive(' + code + ', ' + name + ');');
        HostLMSPatronProfile patronProfile;

        HostLMSPatronProfile.withNewSession { session ->
            try {
                // Start a new transaction
                HostLMSPatronProfile.withNewTransaction {
                    patronProfile = HostLMSPatronProfile.findByCode(code);
                    if (patronProfile == null) {
                        patronProfile = new HostLMSPatronProfile(code: code, name: name);
                        patronProfile.save(flush:true, failOnError:true);

                        // Trigger a notice to be sent if it has been configured
                        patronNoticeService.triggerNotices(patronProfile);
                    } else if (patronProfile.hidden == true) {
                        // Unhide it as it is active again
                        patronProfile.hidden = false;
                        patronProfile.save(flush:true, failOnError:true);
                    }
                }
            } catch(Exception e) {
                log.error('Exception thrown while creating / updating HostLMSPatronProfile: ' + code, e);
            }
        }

        // As the session no longer exists we need to attach it to the current session
        if (patronProfile != null) {
            // Sometimes, the id already exists in the cache as a different object, so you get the exception DuplicateKeyException
            // So rather than attach, we will do a lookup
            patronProfile = HostLMSPatronProfile.get(patronProfile.id);
        }
        log.debug('Exiting HostLMSPatronProfileService::ensureActive');
        return(patronProfile);
    }
}
