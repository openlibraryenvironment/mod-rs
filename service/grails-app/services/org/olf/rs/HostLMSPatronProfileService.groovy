package org.olf.rs;

import org.hibernate.Transaction;

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

        // N.B. You can't really return an object which was materialised in one transactional context to a session
        // living in a different transactional context.
        HostLMSPatronProfile patronProfile;

            try {
              // Start a new transaction
              HostLMSPatronProfile.withTransaction { status ->

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
        return(patronProfile);
    }
}
