package org.olf.rs.Timers;

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
	private static int defaultIdleDays = 3;

	/** The status that the supplier needs to be set to, for the request to be treated as idle */	
	private static String[] idleStatus = [Status.RESPONDER_IDLE, Status.RESPONDER_NEW_AWAIT_PULL_SLIP];

	/** The duration that represents 1 day, used for when when we are excluding the weekend from the idle period */	
	private static Duration durationOneDay = new Duration(-1, 1, 0);
	
	ActionService actionService;
	ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
	SettingsService settingsService;
	
	@Override
	public void performTask(String config) {
		if (settingsService.hasSettingValue(Settings.SETTING_STALE_REQUEST_1_ENABLED, "yes")) {
			// We look to see if a request has been sitting at a supplier for more than X days without the pull slip being printed
			List<Status> validStatus = new ArrayList<Status>();
			
			// Lookup the valid status, we need to do this as each tenant will have a different record / id
			idleStatus.each { status ->
				validStatus.add(reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, status));
			}
	
			// The date that we request must have been created before, for us to take notice off, I believe dates are stored as UTC
			int numberOfIdleDays = getNumberOfIdleDays();
			DateTime idleBeyondDate = (new DateTime(TimeZone.getTimeZone("UTC"), System.currentTimeMillis())).startOfDay();
			
			// if we are ignoring weekends then the calculation for the idle start date will be slightly different
			if (settingsService.hasSettingValue(Settings.SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, "yes") && (numberOfIdleDays > 0)) {
				// We ignore weekends, probably not the best way of doing this but it will work, can be optimised later
				for (int i = 0; i < numberOfIdleDays; i++) {
					idleBeyondDate = idleBeyondDate.addDuration(durationOneDay);
					int dayOfWeek = idleBeyondDate.getDayOfWeek();
					if ((dayOfWeek == 0)  || (dayOfWeek == 6)) {
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
					actionService.performAction(Actions.ACTION_RESPONDER_SUPPLIER_CANNOT_SUPPLY, request, ["note" : "Request has been idle for more than " + numberOfIdleDays + " days." ]);
				}
			}
		}
	}

	/**
	 * Obtains the number of idle days	
	 * @return the number of idle days
	 */
	private int getNumberOfIdleDays() {
		// Get hold of the number of idle days
		int idleDays = settingsService.getSettingAsInt(Settings.SETTING_STALE_REQUEST_2_DAYS, defaultIdleDays);
		
		// The number must be positive
		if (idleDays < 0) {
			// It is negative so reset to the default
			idleDays = defaultIdleDays;
		}
		
		return(idleDays);
	}
}
