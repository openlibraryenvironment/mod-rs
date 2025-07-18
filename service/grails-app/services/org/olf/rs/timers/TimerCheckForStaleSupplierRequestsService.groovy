package org.olf.rs.timers

import grails.gorm.DetachedCriteria;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.olf.rs.PatronRequest;
import org.olf.rs.SettingsService
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.ActionService;

/**
 * Checks to see if a supplier request is idle or not, if it is we respond with the stale action on the state model
 *
 * @author Chas
 *
 */
public class TimerCheckForStaleSupplierRequestsService extends AbstractTimer {

	/** The fall back number of idle days, if the value from the settings is invalid */
	private static final int DEFAULT_IDLE_DAYS = 3;

	/** The query to be performed to find the stale requests */
    private static final String STALE_REQUESTS_QUERY = """
from PatronRequest as pr
where pr.dateCreated < :staleDate and
      pr.isRequester = false and
      pr.stateModel.staleAction is not null and
      pr.state in (select s.state
                   from StateModel as sm
                        inner join sm.states as s
                   where sm = pr.stateModel and
                         s.canTriggerStaleRequest = true)
""";


	private static Map<String, Map> checkMap = [
			"default" : [ setting: SettingsData.SETTING_STALE_REQUEST_2_DAYS, hoursMultiplier: 24] ,
			"rush" : [ setting: SettingsData.SETTING_STALE_REQUEST_RUSH_HOURS,  hoursMultiplier: 1],
			"express" : [ setting: SettingsData.SETTING_STALE_REQUEST_EXPRESS_HOURS, hoursMultiplier: 1],
	];

	/** The duration that represents 1 day, used for when when we are excluding the weekend from the idle period */
	private static final Duration DURATION_ONE_DAY = new Duration(-1, 1, 0);

	private static final String SETTING_ENABLED_YES         = 'yes';
	private static final String SETTING_EXCLUDE_WEEKEND_YES = 'yes';

	private static final int DAY_OF_WEEK_SUNDAY   = 0;
	private static final int DAY_OF_WEEK_SATURDAY = 6;

	ActionService actionService;
	SettingsService settingsService;

	@Override
	public void performTask(String tenant, String config) {
		boolean excludeWeekends = settingsService.hasSettingValue(SettingsData.SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, SETTING_EXCLUDE_WEEKEND_YES);
		boolean staleRequestsEnabled = settingsService.hasSettingValue(SettingsData.SETTING_STALE_REQUEST_1_ENABLED, SETTING_ENABLED_YES);
		log.debug("Stale Supplier Request timer task launched for tenant ${tenant}, stale requests enabled is ${staleRequestsEnabled}, exclude weekends is ${excludeWeekends}");
		def checkLevels = checkMap.keySet();
		if (staleRequestsEnabled) {
			// We look to see if a request has been sitting at a supplier for more than X days without the pull slip being printed

			// The date that we request must have been created before, for us to take notice off, I believe dates are stored as UTC
			//int numberOfIdleDays = numberOfIdleDays();
			checkLevels.each(level -> {
				Map checkEntry = checkMap[level];
				int numberOfIdleHours = getNumberOfIdleHours(checkEntry.setting, checkEntry.hoursMultiplier);
				if (!numberOfIdleHours) {
					return;
				}

				int numberOfIdleDays = Math.floor(numberOfIdleHours / 24);

				//initialize initial date before we remove durations from it
				DateTime idleBeyondDate = (new DateTime(TimeZone.getTimeZone(TIME_ZONE_UTC), System.currentTimeMillis())).startOfDay();


				// if we are ignoring weekends then the calculation for the idle start date will be slightly different
				if (excludeWeekends && (numberOfIdleDays > 0)) {
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
					int hoursPart = numberOfIdleHours % 24;
					Duration duration = new Duration(-1, numberOfIdleDays, hoursPart);
					idleBeyondDate = idleBeyondDate.addDuration(duration);
				}


				Date staleDate = new Date(idleBeyondDate.getTimestamp());
				log.debug("Checking requests at level '${level}' at a threshold of ${numberOfIdleHours} hours, stale date is ${staleDate}");

				// Now find all the stale requests
				List<PatronRequest> requests = PatronRequest.findAll(STALE_REQUESTS_QUERY, [ staleDate : staleDate ]);

				if ((requests != null) && (requests.size() > 0)) {
					for ( request in requests ) {
						if ( (level == "default" && !checkLevels.contains(request.serviceLevel?.value)) ||
							level == request.serviceLevel?.value) {
							// Perform a supplier cannot supply action
							try {
								log.debug("Calling stale request action '${request?.stateModel?.staleAction?.code}' for request ${request?.hrid} with createDate ${request?.dateCreated}");
								actionService.performAction(request.stateModel.staleAction.code, request, ['note': 'Request has been idle for more than ' + numberOfIdleHours + ' hours.']);
							} catch (Exception e) {
								log.error("Exception thrown while performing stale action " + request.stateModel.staleAction.code + " on request " + request.hrid + " ( " + request.id.toString() + " )", e);
							}
							break;
						}
					}
				}
			});

		}
	}

	/**
	 * Obtains the number of idle days
	 * @return the number of idle days
	 */
	/*
	private int numberOfIdleDays() {
		// Get hold of the number of idle days
		return(settingsService.getSettingAsInt(SettingsData.SETTING_STALE_REQUEST_2_DAYS, DEFAULT_IDLE_DAYS, false));
	}
	*/

	private int getNumberOfIdleHours(String setting, int hoursMultiplier) {
		int number = settingsService.getSettingAsInt(setting, DEFAULT_IDLE_DAYS, false);
		return number * hoursMultiplier;
	}
}
