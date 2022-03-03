package org.olf.rs.timers;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.Settings;
import org.olf.rs.statemodel.ActionService;
import org.olf.rs.statemodel.Actions;
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

/**
 * Checks to see if a supplier request is idle or not, if it is it respondes with the action cannot supply
 *
 * @author Chas
 *
 */
public class TimerCheckForStaleSupplierRequestsService extends AbstractTimer {

	/** The fall back number of idle days, if the value from the settings is invalid */
	private static final int DEFAULT_IDLE_DAYS = 3;

	/** The status that the supplier needs to be set to, for the request to be treated as idle */
	private static final String[] IDLE_STATUS = [Status.RESPONDER_IDLE, Status.RESPONDER_NEW_AWAIT_PULL_SLIP];

	/** The duration that represents 1 day, used for when when we are excluding the weekend from the idle period */
	private static final Duration DURATION_ONE_DAY = new Duration(-1, 1, 0);

	private static final String SETTING_ENABLED_YES         = 'yes';
	private static final String SETTING_EXCLUDE_WEEKEND_YES = 'yes';

	private static final int DAY_OF_WEEK_SUNDAY   = 0;
	private static final int DAY_OF_WEEK_SATURDAY = 6;

	private static final String TIME_ZONE_UTC = 'UTC';

	ActionService actionService;
	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
	SettingsService settingsService;

	@Override
	public void performTask(String config) {
		if (settingsService.hasSettingValue(Settings.SETTING_STALE_REQUEST_1_ENABLED, SETTING_ENABLED_YES)) {
			// We look to see if a request has been sitting at a supplier for more than X days without the pull slip being printed
			List<Status> validStatus = [ ];

			// Lookup the valid status, we need to do this as each tenant will have a different record / id
			IDLE_STATUS.each { status ->
				validStatus.add(reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, status));
			}

			// The date that we request must have been created before, for us to take notice off, I believe dates are stored as UTC
			int numberOfIdleDays = numberOfIdleDays();
			DateTime idleBeyondDate = (new DateTime(TimeZone.getTimeZone(TIME_ZONE_UTC), System.currentTimeMillis())).startOfDay();

			// if we are ignoring weekends then the calculation for the idle start date will be slightly different
			if (settingsService.hasSettingValue(Settings.SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, SETTING_EXCLUDE_WEEKEND_YES) && (numberOfIdleDays > 0)) {
				// We ignore weekends, probably not the best way of doing this but it will work, can be optimised later
				for (int i = 0; i < numberOfIdleDays; i++) {
					idleBeyondDate = idleBeyondDate.addDuration(DURATION_ONE_DAY);
					int dayOfWeek = idleBeyondDate.getDayOfWeek();
					if ((dayOfWeek == DAY_OF_WEEK_SUNDAY)  || (dayOfWeek == DAY_OF_WEEK_SATURDAY)) {
						// It is either a Saturday or Sunday, so subtract 1 from i, so we go round the loop again
						i--;
					}
				}
			} else {
				// We do not ignore weekends
				Duration duration = new Duration(-1, numberOfIdleDays, 0);
				idleBeyondDate = idleBeyondDate.addDuration(duration);
			}

			// Now find all the incoming requests
			List<PatronRequest> requests = PatronRequest.findAllByDateCreatedLessThanAndStateInList(new Date(idleBeyondDate.getTimestamp()), validStatus);
			if ((requests != null) && (requests.size() > 0)) {
				requests.each { request ->
					// Perform a supplier cannot supply action
					actionService.performAction(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, request, ['note' : 'Request has been idle for more than ' + numberOfIdleDays + ' days.' ]);
				}
			}
		}
	}

	/**
	 * Obtains the number of idle days
	 * @return the number of idle days
	 */
	private int numberOfIdleDays() {
		// Get hold of the number of idle days
		int idleDays = settingsService.getSettingAsInt(Settings.SETTING_STALE_REQUEST_2_DAYS, DEFAULT_IDLE_DAYS);

		// The number must be positive
		if (idleDays < 0) {
			// It is negative so reset to the default
			idleDays = DEFAULT_IDLE_DAYS;
		}

		return(idleDays);
	}
}
