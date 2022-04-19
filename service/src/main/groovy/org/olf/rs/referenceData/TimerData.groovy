package org.olf.rs.referenceData

import org.olf.rs.Timer;

import groovy.util.logging.Slf4j

@Slf4j
public class TimerData {

	public void load() {
		log.info("Adding timer data to the database");

        // Timer to check for stale requests
		Timer.ensure(
            "CheckForStaleSupplierRequests",
            "Check supplier requests have not become stale",
            "FREQ=DAILY",
            "CheckForStaleSupplierRequests");

        // Timer to check for requests we need to retry
        Timer.ensure(
            "RequestNetworkRetry",
            "Retry requests every that have the network status of Retry",
            "FREQ=MINUTELY;INTERVAL=10",
            "RequestNetworkRetry");

	}

	public static void loadAll() {
		(new TimerData()).load();
	}
}
