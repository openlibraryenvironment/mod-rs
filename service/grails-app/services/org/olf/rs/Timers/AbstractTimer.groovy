package org.olf.rs.Timers;

public abstract class AbstractTimer {

	
	/**
	 * Method that performs the required task of the timer
	 * @param config The config as configured against the timer
	 * @return Nothing 
	 */
	public abstract void performTask(String config);
}
