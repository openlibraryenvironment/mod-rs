package org.olf.rs.referenceData

import org.olf.rs.Timer;

import groovy.util.logging.Slf4j

@Slf4j
public class TimerData {

	public void load() {
		log.info("Adding timer data to the database");

		Timer.ensure("CheckForStaleSupplierRequests", "Check supplier requests have not become stale", "FREQ=DAILY", "CheckForStaleSupplierRequests");
	}
	
	public static void loadAll() {
		(new TimerData()).load();
	}
}
