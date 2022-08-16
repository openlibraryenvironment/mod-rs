package org.olf.rs;

/**
 * Perform generic services required by the code / name domains, primarily the host lms domains
 *
 */
public class GenericCodeNameService<TDomainClass> {

    /** Required for sending patron notices */
    PatronNoticeService patronNoticeService;

    /** The class object for the domain we are dealing with */
    Class domainClass;

    GenericCodeNameService(Class domainClass) {
        this.domainClass = domainClass;
    }

    /**
     * Given a code and name lookup to see if the record exists, if it does not we we create a new one
     * All updates are performed in a separate transaction as they are independent of the main transaction
     * @param code The code we are looking up
     * @param name The name for this instance
     * @param additionalFieldsUpdate If any additional fields need updating then this is the place to do it, the closure take 2 parameters, the instance and a boolean to say if if it is a new record or not
     * @return The record that represents this code and name
     */
    public TDomainClass ensureExists(String code, String name, Closure additionalFieldsUpdate = null) {
        log.debug('Entering GenericCodeNameService::ensureExists(' + code + ', ' + name + ');');
        TDomainClass instance;

        // We will need to create a separate transaction
        domainClass.withNewSession { session ->
            try {
                // Start a new transaction
                domainClass.withNewTransaction {

                    instance = domainClass.findByCodeOrName(code, name);
                    if (instance == null) {
                        // Dosn't exist so we need to create it
                        instance = domainClass.newInstance();
                        instance.code = code;
                        instance.name = name;

                        // Are there any additional fields that want setting
                        if (additionalFieldsUpdate != null) {
                            additionalFieldsUpdate(instance, true);
                        }
                        instance.save(flush:true, failOnError:true);
                        patronNoticeService.triggerNotices(instance);
                    } else {
                        // Do we have any additional fields that want updating
                        if (additionalFieldsUpdate != null) {
                            additionalFieldsUpdate(instance, false);
                            instance.save(flush:true, failOnError:true);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception thrown while creating / update within GenericCodeNameService::ensureExists: " + code, e);
            }
        }

        // As the session no longer exists we need to attach it to the current session
        if (instance != null) {
            // Sometimes, the id already exists in the cache as a different object, so you get the exception DuplicateKeyException
            // So rather than attach, we will do a lookup
            instance = domainClass.get(instance.id);
        }
        log.debug('Exiting GenericCodeNameService::ensureExists');
        return(instance);
    }
}
