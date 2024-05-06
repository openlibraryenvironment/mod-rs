package org.olf.rs

import com.k_int.web.toolkit.settings.AppSetting
import groovy.sql.Sql
import org.dmfs.rfc5545.DateTime
import org.dmfs.rfc5545.Duration
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.referenceData.StatusData

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Handle common checks and methods for when a new request enters the system
 */
public class NewRequestService {

    SettingsService settingsService;

    public String generateHrid() {
        String result = null;

        AppSetting prefixSetting = AppSetting.findByKey('request_id_prefix');
        log.debug("Got app setting ${prefixSetting} ${prefixSetting?.value} ${prefixSetting?.defValue}");

        String hridPrefix = prefixSetting.value ?: prefixSetting.defValue ?: '';

        // Use this to make sessionFactory.currentSession work as expected
        PatronRequest.withSession { session ->
            log.debug('Generate hrid');
            Sql sql = new Sql(session.connection())
            List queryResult  = sql.rows("select nextval('pr_hrid_seq')");
            log.debug("Query result: ${queryResult }");
            result = hridPrefix + queryResult [0].get('nextval')?.toString();
        }
        return(result);
    }

    public Boolean isOverLimit(PatronRequest request) {
        int reqLimit = settingsService.getSettingAsInt(SettingsData.SETTING_MAX_REQUESTS, 0);
        if (reqLimit < 1) return false;
        String query =
                """SELECT count(pr.id)
            FROM PatronRequest AS pr
            JOIN pr.state.tags AS tag
            WHERE tag.value = :activeTag
            AND pr.patronIdentifier = :pid
            """;
        def sqlValues = [
                activeTag: StatusData.tags.ACTIVE_PATRON,
                pid: request.patronIdentifier
        ];
        def count = PatronRequest.executeQuery(query, sqlValues)[0];
        log.debug("Active requests ${count} over limit of ${reqLimit}? ${count > reqLimit}");
        // Presuming REQ_IDLE is tagged and thus this request is included in count
        return count > reqLimit;
    }

    public Boolean isPossibleDuplicate(PatronRequest request) {
        int duplicateTimeHours = settingsService.getSettingAsInt(
                SettingsData.SETTING_CHECK_DUPLICATE_TIME, 0);
        log.debug("Checking PatronRequest ${request} ( patronReference ${request.patronReference} ) for duplicates within the last ${duplicateTimeHours} hours");
        if (duplicateTimeHours > 0) {
            //public Duration(int sign, int days, int hours, int minutes, int seconds)
            Duration duration = new Duration(-1, 0, duplicateTimeHours, 0, 0);
            DateTime beforeDate = DateTime.now().addDuration(duration);
            String query =
                    """FROM PatronRequest AS pr 
                WHERE pr.id != :requestId
                AND pr.title = :requestTitle 
                AND pr.patronIdentifier = :requestPatronIdentifier
                AND pr.dateCreated > :dateDelta
                """;
            def sqlValues = [
                    'requestId' : request.id,
                    'requestTitle' : request.title,
                    'requestPatronIdentifier' : request.patronIdentifier,
                    'dateDelta' : new Date(beforeDate.getTimestamp())
            ];
            log.debug("Using values ${sqlValues} for duplicateQuery");
            def results = PatronRequest.findAll(query, sqlValues);

            log.debug("Found ${results.size()} possible duplicate(s)");
            if (results.size() > 0) {
                for (result in results) {
                    log.debug("Query matches PR with id ${result.id} and patronReference ${result.patronReference} to original id ${request.id}, patronRefernece ${request.patronReference}");
                }
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public String getOCLCId(String id) {
        Pattern pattern = ~/^(ocn|ocm|on)(\d+)/;
        Matcher matcher = id =~ pattern;
        if (matcher.find()) {
            return matcher.group(2);
        }
        return(null);
    }



    public Boolean hasClusterID(PatronRequest request) {
        if (request.systemInstanceIdentifier != null) {
            return true;
        }
        return false;
    }

}
