package org.olf.rs

/**
 * Holds the names of locks that we use ni various places
 * @author Chas
 *
 */
class LockIdentifier {

    /** Defines the lock names that we use */
    public static final String BACKGROUND_TASKS     = 'mod-rs:BackgroundTaskService:performReshareTasks';
    public static final String SETUP_REFERENCE_DATA = 'mod-rs:HousekeepingService:setupReferenceData';
}
