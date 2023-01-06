package org.olf.rs.timers;

/**
 * The abstract class for timer classes called by the BackgroundTaskService which determines whether this time should run or not
 * To run the class the performTask method should be called to perform the required actions for this timer.
 *
 * @author Chas
 *
 */
public abstract class AbstractTimer {

    /** The timezone we work with, used by some of the timers */
    public static final String TIME_ZONE_UTC = 'UTC';

	/**
	 * Method that performs the required task of the timer
	 * @param tenant The tenant the timer is being run against
	 * @param config The config as configured against the timer
	 * @return Nothing
	 */
	public abstract void performTask(String tenant, String config);
}
