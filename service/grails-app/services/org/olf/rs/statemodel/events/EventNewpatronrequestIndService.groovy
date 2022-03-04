package org.olf.rs.statemodel.events;

import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.HostLMSService;
import org.olf.rs.PatronNoticeService;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.SharedIndexService
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StateModel;

import groovy.json.JsonSlurper;
import groovy.sql.Sql;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This event service takes a new patron request and validates it and tries to determine the rota
 * @author Chas
 */
public class EventNewpatronrequestIndService extends AbstractEvent {

    private static final String[] PATRON_REQUEST_FROM_STATES = [
        Status.PATRON_REQUEST_IDLE
    ];

    private static final String[] RESPONDER_FROM_STATES = [
        Status.RESPONDER_IDLE
    ];

    private static final Map FROM_STATES =  [ : ];

    private static final String[] PATRON_REQUEST_TO_STATES = [
        Status.PATRON_REQUEST_ERROR,
        Status.PATRON_REQUEST_INVALID_PATRON,
        Status.PATRON_REQUEST_VALIDATED
    ];

    private static final String[] RESPONDER_TO_STATES = [
        Status.RESPONDER_NEW_AWAIT_PULL_SLIP,
        Status.RESPONDER_UNFILLED
    ];

    private static final Map TO_STATES = [ : ];

    static {
        FROM_STATES.put(StateModel.MODEL_REQUESTER, PATRON_REQUEST_FROM_STATES);
        FROM_STATES.put(StateModel.MODEL_RESPONDER, RESPONDER_FROM_STATES);

        TO_STATES.put(StateModel.MODEL_REQUESTER, PATRON_REQUEST_TO_STATES);
        TO_STATES.put(StateModel.MODEL_RESPONDER, RESPONDER_TO_STATES);
    }

    HostLMSService hostLMSService;
    // PatronNoticeService patronNoticeService;
    ReshareActionService reshareActionService;
    SharedIndexService sharedIndexService;

    @Override
    String name() {
        return(Events.EVENT_NEW_PATRON_REQUEST_INDICATION);
    }

    @Override
    EventFetchRequestMethod fetchRequestMethod() {
        return(EventFetchRequestMethod.PAYLOAD_ID);
    }

    @Override
    Boolean canLeadToSameState() {
        return(false);
    }

    @Override
    String[] toStates(String model) {
        return(TO_STATES[model]);
    }

    @Override
    String[] fromStates(String model) {
        return(FROM_STATES[model]);
    }

    @Override
    boolean supportsModel(String model) {
        // This event
        return((model == StateModel.MODEL_REQUESTER) || (StateModel.MODEL_RESPONDER));
    }

    // Notify us of a new patron request in the database - regardless of role
    //
    // Requests are created with a STATE of IDLE, this handler validates the request and sets the state to VALIDATED, or ERROR
    // Called when a new patron request indication happens - usually
    // New patron requests must have a  request.requestingInstitutionSymbol
    @Override
    EventResultDetails processEvent(PatronRequest request, Map eventData, EventResultDetails eventResultDetails) {
        // If the role is requester then validate the request and set the state to validated
        if ((request != null) &&
            (request.state?.code == Status.PATRON_REQUEST_IDLE) &&
            (request.isRequester == true)) {
            // Generate a human readabe ID to use
            request.hrid = generateHrid()
            log.debug("set request.hrid to ${request.hrid}");

            // If we were supplied a pickup location, attempt to resolve it here
            DirectoryEntry pickupLoc;
            if (request.pickupLocationSlug) {
                pickupLoc = DirectoryEntry.findBySlug(request.pickupLocationSlug);
            } else if (request.pickupLocationCode) { // deprecated
                pickupLoc = DirectoryEntry.find("from DirectoryEntry de where de.lmsLocationCode=:code and de.status.value='managed'", [code: request.pickupLocationCode]);
            }

            if (pickupLoc != null) {
                request.resolvedPickupLocation = pickupLoc;
                List pickupSymbols  = pickupLoc?.symbols?.findResults { symbol ->
                    symbol?.priority == 'shipping' ? symbol?.authority?.symbol + ':' + symbol?.symbol : null
                }

                // TODO this deserves a better home
                request.pickupLocation = pickupSymbols.size > 0 ? "${pickupLoc.name} --> ${pickupSymbols [0]}" : pickupLoc.name;
            }

            if (request.requestingInstitutionSymbol != null) {
                // We need to validate the requsting location - and check that we can act as requester for that symbol
                Symbol s = reshareApplicationEventHandlerService.resolveCombinedSymbol(request.requestingInstitutionSymbol);
                if (s != null) {
                    // We do this separately so that an invalid patron does not stop information being appended to the request
                    request.resolvedRequester = s;
                }

                Map lookupPatron = reshareActionService.lookupPatron(request, null);
                if (lookupPatron.callSuccess) {
                    boolean patronValid = lookupPatron.patronValid;

                    // If s != null and patronValid == true then the request has passed validation
                    if (s != null && patronValid) {
                        log.debug("Got request ${request}");
                        log.debug(' -> Request is currently ' + Status.PATRON_REQUEST_IDLE + ' - transition to ' + Status.PATRON_REQUEST_VALIDATED);
                        eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_VALIDATED);
                    // patronNoticeService.triggerNotices(request, RefdataValue.lookupOrCreate('noticeTriggers', 'New request'));
                    } else if (s == null) {
                        // An unknown requesting institution symbol is a bigger deal than an invalid patron
                        request.needsAttention = true;
                        log.warn("Unkown requesting institution symbol : ${request.requestingInstitutionSymbol}");
                        eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_ERROR);
                        eventResultDetails.auditMessage = 'Unknown Requesting Institution Symbol: ' + request.requestingInstitutionSymbol;
                    } else {
                        // If we're here then the requesting institution symbol was fine but the patron is invalid
                        eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_INVALID_PATRON);
                        eventResultDetails.auditMessage = "Failed to validate patron with id: \"${request.patronIdentifier}\".${lookupPatron?.status != null ? " (Patron state=${lookupPatron.status})" : ''}".toString();
                        request.needsAttention = true;
                    }
                } else {
                    // unexpected error in Host LMS call
                    request.needsAttention = true;
                    eventResultDetails.auditMessage = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. ' + lookupPatron?.problems;
                }
            } else {
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_REQUESTER, Status.PATRON_REQUEST_ERROR);
                request.needsAttention = true;
                eventResultDetails.auditMessage = 'No Requesting Institution Symbol';
            }

            // This is a bit dirty - some clients continue to send request.systemInstanceIdentifier rather than request.bibliographicRecordId
            // If we find we are missing a bib record id but do have a system instance identifier, copy it over. Needs sorting properly post PALCI go live
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
        } else if ((request != null) &&
                   (request.state?.code == Status.RESPONDER_IDLE) &&
                   (request.isRequester == false)) {
            try {
                log.debug('Launch auto responder for request');
                String autoRespond = AppSetting.findByKey('auto_responder_status')?.value
                if (autoRespond?.toLowerCase().startsWith('on')) {
                    autoRespond(request, autoRespond.toLowerCase(), eventResultDetails);
                } else {
                    eventResultDetails.auditMessage = "Auto responder is ${autoRespond} - manual checking needed";
                    request.needsAttention = true;
                }
            } catch (Exception e) {
                log.error('Problem in auto respond', e);
            }
        } else {
            log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != ${Status.PATRON_REQUEST_IDLE} (${request?.state?.code}) isRequester=${request?.isRequester}");
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

    private void autoRespond(PatronRequest request, String autoRespondVariant, EventResultDetails eventResultDetails) {
        log.debug('autoRespond....');

        // Use the hostLMSService to determine the best location to send a pull-slip to
        ItemLocation location = hostLMSService.getHostLMSActions().determineBestLocation(request);

        log.debug("result of determineBestLocation = ${location}");

        // Were we able to locate a copy?
        boolean unfilled = false;
        if (location != null) {
            // set localCallNumber to whatever we managed to look up
            if (reshareApplicationEventHandlerService.routeRequestToLocation(request, location)) {
                eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_NEW_AWAIT_PULL_SLIP);
                eventResultDetails.auditMessage = 'autoRespond will-supply, determine location=' + location;
                log.debug("Send ExpectToSupply response to ${request.requestingInstitutionSymbol}");
                reshareActionService.sendResponse(request,  'ExpectToSupply', [:])
            } else {
                unfilled = true;
                eventResultDetails.auditMessage = 'AutoResponder Failed to route to location ' + location;
            }
        } else {
            // No - is the auto responder set up to sent not-supplied?
            if (autoRespondVariant == 'on:_will_supply_and_cannot_supply') {
                unfilled = true;
                eventResultDetails.auditMessage = 'AutoResponder cannot locate a copy.';
            }
        }

        // If it was unfilled then send a response
        if (unfilled == true) {
            log.debug("Send unfilled(No copy) response to ${request.requestingInstitutionSymbol}");
            reshareActionService.sendResponse(request,  'Unfilled', ['reason':'No copy']);
            eventResultDetails.newStatus = reshareApplicationEventHandlerService.lookupStatus(StateModel.MODEL_RESPONDER, Status.RESPONDER_UNFILLED);
        }
    }
}
