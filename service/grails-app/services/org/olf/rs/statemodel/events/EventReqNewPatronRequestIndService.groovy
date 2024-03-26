package org.olf.rs.statemodel.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.PatronNoticeService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ProtocolReferenceDataValue;
import org.olf.rs.ReshareActionService;
import org.olf.rs.SettingsService;
import org.olf.rs.SharedIndexService;
import org.olf.rs.patronRequest.PickupLocationService;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEventResultQualifier;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Events;
import org.olf.rs.referenceData.RefdataValueData;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.referenceData.StatusData;

import com.k_int.web.toolkit.settings.AppSetting;
import com.k_int.web.toolkit.refdata.RefdataValue;

import groovy.json.JsonSlurper;
import groovy.sql.Sql;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;

/**
 * This event service takes a new requester patron request and validates it and tries to determine the rota
 * @author Chas
 */
public class EventReqNewPatronRequestIndService extends AbstractEvent {

    PatronNoticeService patronNoticeService;
    PickupLocationService pickupLocationService;
    ReshareActionService reshareActionService;
    SharedIndexService sharedIndexService;

    SettingsService settingsService;

    @Override
    String name() {
        return(Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    // Notify us of a new requester patron request in the database
    //
    // Requests are created with a STATE of IDLE, this handler validates the request and sets the state to VALIDATED, or ERROR
    // Called when a new patron request indication happens - usually in response to a new request being created but
    // also to re-validate
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        if (request == null) {
            log.warn("Unable to locate request for ID ${eventData.payload.id} isRequester=${request?.isRequester}");
            return(eventResultDetails);
        }

        // Generate a human readable ID to use
        if (!request.hrid) {
            request.hrid = generateHrid();
            log.debug("set request.hrid to ${request.hrid}");
        }

        if (request.serviceType == null) {
            request.serviceType = ProtocolReferenceDataValue.lookupServiceType(ProtocolReferenceDataValue.SERVICE_TYPE_LOAN);
        }

        // If we were supplied a pickup location, attempt to resolve it
        if (!request.resolvedPickupLocation) {
            pickupLocationService.check(request)
        }

        if (request.requestingInstitutionSymbol != null) {
            // We need to validate the requesting location - and check that we can act as requester for that symbol
            Symbol s = reshareApplicationEventHandlerService.resolveCombinedSymbol(request.requestingInstitutionSymbol);
            if (s != null) {
                // We do this separately so that an invalid patron does not stop information being appended to the request
                request.resolvedRequester = s;
            }

            Map lookupPatron = reshareActionService.lookupPatron(request, null);
            if (lookupPatron.callSuccess) {
                boolean patronValid = lookupPatron.patronValid;

                if (s == null) {
                    // An unknown requesting institution symbol is a bigger deal than an invalid patron
                    request.needsAttention = true;
                    log.warn("Unknown requesting institution symbol : ${request.requestingInstitutionSymbol}");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO_INSTITUTION_SYMBOL;
                    eventResultDetails.auditMessage = 'Unknown Requesting Institution Symbol: ' + request.requestingInstitutionSymbol;
                } else if (!patronValid) {
                    // If we're here then the requesting institution symbol was fine but the patron is invalid
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_INVALID_PATRON;
                    String errors = (lookupPatron?.problems == null) ? '' : (' (Errors: ' + lookupPatron.problems + ')');
                    String status = lookupPatron?.status == null ? '' : (' (Patron state = ' + lookupPatron.status + ')');
                    eventResultDetails.auditMessage = "Failed to validate patron with id: \"${request.patronIdentifier}\".${status}${errors}".toString();
                    request.needsAttention = true;
                } else if (isOverLimit(request)) {
                    log.debug("Request is over limit");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_OVER_LIMIT;
                    patronNoticeService.triggerNotices(request,  RefdataValue.lookupOrCreate('noticeTriggers', RefdataValueData.NOTICE_TRIGGER_OVER_LIMIT));

                } else if (isPossibleDuplicate(request)) {
                    request.needsAttention = true;
                    log.warn("Request ${request.hrid} appears to be a duplicate (patronReference ${request.patronReference})");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_DUPLICATE_REVIEW;
                } else if (!hasClusterID(request)) {
                    request.needsAttention = true;
                    log.warn("No cluster id set for request ${request.id}");
                    eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_BLANK_FORM_REVIEW;
                    eventResultDetails.auditMessage = 'Blank Request Form, needs review';
                } else {
                    // The request has passed validation
                    log.debug("Got request ${request}");
                }

            } else {
                // unexpected error in Host LMS call
                request.needsAttention = true;
                eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_HOST_LMS_CALL_FAILED;
                eventResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + lookupPatron?.problems;
            }
        } else {
            eventResultDetails.qualifier = ActionEventResultQualifier.QUALIFIER_NO_INSTITUTION_SYMBOL;
            request.needsAttention = true;
            eventResultDetails.auditMessage = 'No Requesting Institution Symbol';
        }

        // TODO: reconcile these two identifiers as both are in use
        if ((request.bibliographicRecordId == null) && (request.systemInstanceIdentifier != null)) {
            request.bibliographicRecordId = request.systemInstanceIdentifier
        }

        if ((request.bibliographicRecordId != null) && (request.bibliographicRecordId.length() > 0)) {
            log.debug('calling fetchSharedIndexRecords');
            List<String> bibRecords = sharedIndexService.getSharedIndexActions().fetchSharedIndexRecords([systemInstanceIdentifier: request.bibliographicRecordId]);
            if (bibRecords?.size() == 1) {
                request.bibRecord = bibRecords[0];
                // If our OCLC field isn't set, let's try to set it from our bibrecord
                if (!request.oclcNumber) {
                    try {
                        JsonSlurper slurper = new JsonSlurper();
                        Object bibJson = slurper.parseText(bibRecords[0]);
                        for (identifier in bibJson.identifiers) {
                            String oclcId = getOCLCId(identifier.value);
                            if (oclcId) {
                                log.debug("Setting request oclcNumber to ${oclcId}");
                                request.oclcNumber = oclcId;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Unable to parse bib json: ${e}");
                    }
                }
            }
        } else {
            log.debug("No request.bibliographicRecordId : ${request.bibliographicRecordId}");
        }

        return(eventResultDetails);
    }

    private String generateHrid() {
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

    private String getOCLCId(String id) {
        Pattern pattern = ~/^(ocn|ocm|on)(\d+)/;
        Matcher matcher = id =~ pattern;
        if (matcher.find()) {
            return matcher.group(2);
        }
        return(null);
    }

    private Boolean isOverLimit(PatronRequest request) {
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

    private Boolean isPossibleDuplicate(PatronRequest request) {
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

    private Boolean hasClusterID(PatronRequest request) {
        if (request.systemInstanceIdentifier != null) {
            return true;
        }
        return false;
    }
}
