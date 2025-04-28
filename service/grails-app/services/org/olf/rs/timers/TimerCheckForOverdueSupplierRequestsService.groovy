package org.olf.rs.timers;

import java.time.temporal.ChronoUnit;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import org.olf.rs.OkapiSettingsService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareApplicationEventHandlerService;
import org.olf.rs.statemodel.Status;

/**
 * Checks to see if a supplier request is overdue, if it is it then we respond with the overdue action on the state model
 *
 * @author Chas
 *
 */
public class TimerCheckForOverdueSupplierRequestsService extends AbstractTimer {

	/** The query to be performed to find the overdue requests */
    private static final String OVERDUE_REQUESTS_QUERY = """
from PatronRequest as pr
where pr.parsedDueDateRS < :today and
      pr.isRequester = false and
      pr.stateModel.overdueStatus is not null and
      pr.state in (select s.state
                   from StateModel as sm
                        inner join sm.states as s
                   where sm = pr.stateModel and
                         s.canTriggerOverdueRequest = true)
""";

    OkapiSettingsService okapiSettingsService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;

	@Override
	public void performTask(String tenant, String config) {
        // We need to take the tenant timezone into account when calculating the beginning of the day
        Map localeSettings = okapiSettingsService.getLocaleSettings();
        ZoneId zoneId = ZoneId.of(localeSettings?.timezone ?: "UTC");
        Date today = Date.from(ZonedDateTime.now(zoneId).truncatedTo(ChronoUnit.DAYS).withLaterOffsetAtOverlap().toInstant());

		// Now find all the incoming requests
		List<PatronRequest> requests = PatronRequest.findAll(OVERDUE_REQUESTS_QUERY, [ today : today ]);
		if ((requests != null) && (requests.size() > 0)) {
			requests.each { request ->
				// We need to change its state
                try {
                    // Save the previous state as we need that for the audit
                    Status previousState = request.state;
                    request.state = request.stateModel.overdueStatus;

                    // Create the audit
                    reshareApplicationEventHandlerService.auditEntry(request, previousState, request.stateModel.overdueStatus, "Request is Overdue", null);
                    log.info("PatronRequest ${request.hrid} (${request.id}) is overdue -- currently ${today} and due on ${request.parsedDueDateRS}");

                    // Save the request
                    request.save(flush:true, failOnError:true);
                } catch (Exception e) {
                    log.error("Exception thrown while setting overdue status " + request.stateModel.overdueStatus.code + " on request " + request.hrid + " ( " + request.id.toString() + " )", e);
                }
			}
		}
	}
}
