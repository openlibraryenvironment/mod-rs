package org.olf.rs.timers;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.olf.rs.ProtocolAudit;
import org.olf.rs.ProtocolType;
import org.olf.rs.SettingsService;
import org.olf.rs.referenceData.SettingsData;

/**
 * Removed records from protocol audit that have out stayed there welcome
 *
 * @author Chas
 *
 */
public class TimerRemoveFromProtocolAuditService extends AbstractTimer {

	/** The query to be performed to find the overdue requests */
    private static final String DELETE_AUDIT_RECORDS_QUERY = """
delete ProtocolAudit as pa
where pa.dateCreated < :dateToDeleteBefore and
      pa.protocolType = :protocolType
""";

    SettingsService settingsService;

	@Override
	public void performTask(String tenant, String config) {
        // Only interested in the date segment and that it is in UTC
        DateTime today = new DateTime(TimeZone.getTimeZone(TIME_ZONE_UTC), System.currentTimeMillis()).startOfDay();

		// Run through each of the protocols clearing out the audit records
        clearDownAuditRecords(ProtocolType.ISO18626, today, SettingsData.SETTING_LOGGING_ISO18626_DAYS);
        clearDownAuditRecords(ProtocolType.NCIP, today, SettingsData.SETTING_LOGGING_NCIP_DAYS);
        clearDownAuditRecords(ProtocolType.Z3950_REQUESTER, today, SettingsData.SETTING_LOGGING_Z3950_REQUESTER_DAYS);
        clearDownAuditRecords(ProtocolType.Z3950_RESPONDER, today, SettingsData.SETTING_LOGGING_Z3950_RESPONDER_DAYS);
	}

    private void clearDownAuditRecords(ProtocolType protocolType, DateTime todaysDate, String daysToKeepKey) {
        try {
            // First of all lookup to see how many days we need to keep the data
            int daysToKeep = settingsService.getSettingAsInt(daysToKeepKey, 30);
            Duration duration = new Duration(-1, daysToKeep, 0);
            Date dateToDeleteBefore = new Date(todaysDate.addDuration(duration).getTimestamp());;
            log.info("Deleting records from ProtocolAudit prior to " + dateToDeleteBefore.toString()+ " for protocol: " + protocolType.toString());

            // Now we can execute the delete
            ProtocolAudit.executeUpdate(DELETE_AUDIT_RECORDS_QUERY, [dateToDeleteBefore : dateToDeleteBefore, protocolType : protocolType]);
        } catch (Exception e) {
            log.error("Exception thrown while trying to delete old records from protocol audit", e);
        }
    }
}
