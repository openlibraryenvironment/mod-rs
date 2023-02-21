package org.olf.rs.timers;

import org.olf.rs.PatronNoticeService;

/**
 * Processes patron notices
 *
 * @author Chas
 *
 */
public class TimerProcessPatronNoticesService extends AbstractTimer {

    def grailsApplication;
    PatronNoticeService patronNoticeService;

	@Override
	public void performTask(String tenant, String config) {
        // Are the patron notices enabled
        if ( grailsApplication.config?.reshare?.patronNoticesEnabled == true ) {
            // They are so process the queue
            patronNoticeService.processQueue()
        }
	}
}
