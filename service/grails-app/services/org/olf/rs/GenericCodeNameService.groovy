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

    /** Closure to say if the instance requires updating or not */
    Closure checkForUpdate;

    GenericCodeNameService(Class domainClass, Closure checkForUpdate = null) {
        this.domainClass = domainClass;
        this.checkForUpdate = checkForUpdate;
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

        // We first look it up in the current session
        TDomainClass instance = domainClass.findByCode(code);

        // Did we find an instance with this code
        boolean exists = (instance != null);

        // If it dosn't exists or requires updating, do this in a separate transaction
        if (!exists || ((checkForUpdate != null) && checkForUpdate(instance) && (additionalFieldsUpdate != null))) {
            // We will need to create a separate transaction
            domainClass.withNewSession { session ->
                try {
                    // Start a new transaction
                    domainClass.withNewTransaction {

                        // We need to do slightly different functionality for new and update
                        if (exists) {
                            // Lookup the instance in this session
                            instance = domainClass.findByCode(code);

                            // There must be some fields that need updating
                            additionalFieldsUpdate(instance, false);
                            instance.save(flush:true, failOnError:true);
                        } else {
                            // Dosn't exist so we need to create it
                            instance = domainClass.newInstance();
                            instance.code = code;
                            instance.name = name;

                            // Are there any additional fields that want setting
                            if (additionalFieldsUpdate != null) {
                                additionalFieldsUpdate(instance, true);
                            }
                            instance.save(flush:true, failOnError:true);

                            // Send the notice
                            patronNoticeService.triggerNotices(instance);
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
        }

        log.debug('Exiting GenericCodeNameService::ensureExists');
        return(instance);
    }
}
