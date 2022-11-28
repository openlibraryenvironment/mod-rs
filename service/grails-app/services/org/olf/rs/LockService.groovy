package org.olf.rs

import services.k_int.core.FolioLockService;

/**
 * This is a gateway into FolioLockService so we ensure the locks are tenant based
 * Whereas the locks made against FolioLockService are system wide
 */
public class LockService {

    FolioLockService folioLockService;

    /**
     * Attempts to obtain the lock with the specified name and
     * if it is obtained within the specified number of seconds then execute workToBeExecuted
     * @param tenant The tenant the lock is to be obtained for
     * @param lockName The name of the lock to be obtained
     * @param maximumSeconds The maximum number of seconds to wait for the lock
     * @param workToBeExecuted A closure that is the work to be executed if we manage to obtain the lock
     * @return true if we obtained the lock otherwise false if we do not manage to get the lock
     */
    public boolean performWorkIfLockObtained(
        String tenant,
        String lockName,
        int maximumSeconds,
        Closure workToBeExecuted
    ) {
        // Default to not being successful
        boolean result = false;

        // Can only continue if we have a tenant and lock name
        if (tenant && lockName) {
            // We concatenate the tenant and lock name so that we have no effect on other tenants
            result = folioLockService.federatedLockAndDoWithTimeoutOrSkip(
                tenant + ":" + lockName,
                maximumSeconds * 1000,
                workToBeExecuted
            );
        }

        // Let the caller know if we were successful or not
        return(result);
    }
}
