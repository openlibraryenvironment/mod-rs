package org.olf.rs.timers;

import org.olf.rs.BatchService;
import org.olf.rs.EmailService;
import org.olf.rs.HostLMSLocation;
import org.olf.rs.OkapiSettingsService;
import org.olf.rs.PatronRequest;
import org.olf.rs.SettingsService;
import org.olf.rs.files.FileFetchResult;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.reporting.ReportService;
import org.olf.templating.TemplateContainer;
import org.olf.templating.TemplatingService;

import groovy.json.JsonSlurper;

/**
 * Checks to see if there are any pull slips that need printing and if there are sends an email if configured
 *
 * @author Chas
 *
 */
public class TimerPrintPullSlipsService extends AbstractTimer {

    private static final String PULL_SLIP_QUERY='''
Select pr
from PatronRequest as pr
where pr.pickLocation.id in ( :loccodes ) and
      pr.state in (select sms.state
                   from StateModel as sm
                       inner join sm.states as sms
                   where sm = pr.stateModel and
                         sms.triggerPullSlipEmail = true)
''';

    private static final String PULL_SLIP_SUMMARY = '''
Select count(pr.id), pr.pickLocation.code
from PatronRequest as pr
where pr.state in (select sms.state
                   from StateModel as sm
                       inner join sm.states as sms
                   where sm = pr.stateModel and
                         sms.triggerPullSlipEmail = true)
group by pr.pickLocation.code
''';

    private static final String LOCATIONS_QUERY = '''
select h
from HostLMSLocation as h
where h.id in ( :loccodes )
''';

    BatchService batchService;
    EmailService emailService;
    OkapiSettingsService okapiSettingsService;
    ReportService reportService;
    SettingsService settingsService;
    TemplatingService templatingService;

    @Override
    public void performTask(String tenant, String config) {
        log.debug("Fire pull slip timer task. Config is ${config}");
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map task_config = jsonSlurper.parseText(config);
        checkPullSlips(tenant, task_config);
    }

    private void checkPullSlips(String tenant, Map timer_cfg) {
        log.debug("checkPullSlips(${timer_cfg})");
        checkPullSlipsFor(
            tenant,
            timer_cfg.locations,
            timer_cfg.emailAddresses,
            (timer_cfg.attachPullSlips == null) ? false : timer_cfg.attachPullSlips);
    }

    /**
     * Checks to see if there are any pending pull slips for the supplied locations,
     * if there are it then sends an email to the supplied email addresses and optionally attached the pull slips
     * @param tenant The tenant we are reporting the pull slips for
     * @param loccodes The ids of the location codes we want to be informed about
     * @param emailAddresses The email addresses that want to be informed about the pull slips
     * @param attachPullSlips Do we attach the pull slips or not, in doing so we action them as being printed
     */
    private void checkPullSlipsFor(
        String tenant,
        ArrayList loccodes,
        ArrayList emailAddresses,
        boolean attachPullSlips
    ) {
        log.debug("checkPullSlipsFor(${tenant}, ${loccodes}, ${emailAddresses}, ${attachPullSlips})");

        try {
            // Lookup the template id to be used
            String pullSlipTemplateId = settingsService.getSettingValue(SettingsData.SETTING_PULL_SLIP_TEMPLATE_ID);

            // Did we find a template id
            if (pullSlipTemplateId == null) {
                log.info("No pull slip template has been configured, not sending pull slip emails");
            } else {
                // Now can we find the container
                TemplateContainer tc = TemplateContainer.read(pullSlipTemplateId);

                // Did we find a template container
                if (tc != null) {
                    // We can so looking good
                    // Obtain the summary of the number of pull slips for each location
                    def pull_slip_overall_summary = PatronRequest.executeQuery(PULL_SLIP_SUMMARY);
                    log.debug("pull slip summary: ${pull_slip_overall_summary}");

                    // Obtain the list of locations we are dealing with
                    List<HostLMSLocation> pslocs = HostLMSLocation.executeQuery(LOCATIONS_QUERY, [loccodes:loccodes]);

                    // Do we have at least 1 location and at least 1 email
                    if (( pslocs.size() > 0) && (emailAddresses != null) && (emailAddresses.size() > 0)) {
                        log.debug("Resolved locations ${pslocs} - send to ${emailAddresses}");

                        // Obtain the requests that require a pull slip printing for the given locations
                        List<PatronRequest> pending_ps_printing = PatronRequest.executeQuery(PULL_SLIP_QUERY, [loccodes:loccodes]);

                        // Did we find any requests to mention in the email
                        if ((pending_ps_printing != null) && (pending_ps_printing.size() > 0)) {
                            log.debug("${pending_ps_printing.size()} pending pull slip printing for locations ${pslocs}");

                            String locationsText = pslocs.inject('', { String output, HostLMSLocation loc ->
                                output + (output != '' ? ', ' : '') + (loc.name ?: loc.code);
                            });

                            // Build the filter for the pick locations
                            String locFilters = loccodes.collect { "pickLocation.id==${it }"}.join('%7C%7C');

                            // Grab the unique list of states that have triggered the bull slip email
                            String stateFilters = null;
                            pending_ps_printing.unique(false) { request ->
                                request.state.code
                            }.each { PatronRequest request ->
                                if (stateFilters == null) {
                                    stateFilters = "";
                                } else {
                                    stateFilters += "%7C%7C";
                                }
                                stateFilters += "state.code==" + request.state.code;
                            }

                            // Convert the template into something that we can send in the email
                            def tmplResult = templatingService.performTemplate(
                                tc,
                                [
                                    locations: locationsText,
                                    pendingRequests: pending_ps_printing,
                                    numRequests:pending_ps_printing.size(),
                                    summary: pull_slip_overall_summary,
                                    reshareURL: "${okapiSettingsService.getSetting('FOLIO_HOST')?.value}/supply/requests?filters=${locFilters}&filters=${stateFilters}&sort=-dateCreated"
                                ],
                                'en'
                            );

                            // Do we need to attach the pull slips
                            if (attachPullSlips) {
                                // This gets a bit messy as we have a limit to how many request pull slips can be in 1 email
                                int numberOfRequestsPerPullSlip = reportService.getMaxRequestsInPullSlip();

                                // The list of list request maps for processing when we have broken them down to what will be contained in each pull slip
                                List requestMapsList = [ ];

                                // This is the list of requests in the current pull slip
                                Map requests = null;

                                // Loop through all the requests as we need to group them so we do not generate to many in one request
                                pending_ps_printing.each { PatronRequest request ->
                                    // Do we have a current list
                                    if (requests == null) {
                                        // No we don't so create a new one
                                        requests = new HashMap();
                                        requestMapsList.add(requests);
                                    }

                                    // Add the request identifier to the current list that
                                    requests.put(request.id, request);

                                    // Have we reached the limit for the number of requests in a pull slip
                                    if (requests.size()== numberOfRequestsPerPullSlip) {
                                        // Set the current list to null
                                        requests = null;
                                    }
                                }

                                // Now we can generate the pull slip for each request map
                                String reportId = reportService.getPullSlipReportId();
                                int startPosition = 1;
                                requestMapsList.each { Map requestMap ->
                                    // The attachments to be sent
                                    List attachments = null;

                                    // The postfix for the subject
                                    String subjectPostfix = null;

                                    // The starting position of the next block block of request ids
                                    int nextStartPosition = startPosition + requestMap.size();

                                    // Now generate the report, this does the render
                                    FileFetchResult fetchResult = reportService.generateReport(tenant, reportId, requestMap.keySet().toList(), reportService.getPullSlipLogoId(), ReportService.pullSlipDefaultReport);

                                    // Did we manage to generate the report
                                    if (fetchResult.inputStream == null) {
                                        // Failed to generate the pdf, so log it as an error and send the email without the pdf
                                        log.error("Failed to generate pdf to send as attachment containing request ids: " + requestMap.keySet().toString());
                                        sendPullSlipMail(emailAddresses, tmplResult.result.header, tmplResult.result.body, null);
                                    } else {
                                        // We need to encode the file
                                        String encoded = fetchResult.inputStream.bytes.encodeBase64().toString();

                                        // We have now finished with the input stream so close it
                                        fetchResult.inputStream.close();

                                        // Set the subject postfix so they know why they are receiving multiple emails
                                        subjectPostfix = "-requests " + startPosition.toString() + "-" + (nextStartPosition - 1).toString();

                                        // Now create the attachments array
                                        attachments = [
                                            [
                                                contentType: 'application/pdf',
                                                name: 'pullSlips' + subjectPostfix,
                                                description: 'Pull Slip for ' + subjectPostfix,
                                                data: encoded,
                                                disposition: 'base64'
                                            ]
                                        ];
                                    }

                                    // Send the email with the attachments
                                    sendPullSlipMail(emailAddresses, tmplResult.result.header + ((subjectPostfix == null) ? "" : subjectPostfix), tmplResult.result.body, attachments);

                                    // Now we have sent the email, create a batch that represents the pull slip that was created
                                    Map batchResult = batchService.generatePickListBatchFromList(requestMap.values().toList(), "Created (" + startPosition.toString() + "):", true, true);

                                    // Did we manage to create a new batch
                                    if (batchResult.batch) {
                                        // We did, so we can now mark the requests as printed
                                        batchService.markRequestsInBatchAsPrinted(batchResult.batch);
                                    }

                                    // Reset the start position
                                    startPosition = nextStartPosition;
                                }
                            } else {
                                // Just send the email with no attachments
                                sendPullSlipMail(emailAddresses, tmplResult.result.header, tmplResult.result.body, null);
                            }
                        } else {
                            log.debug("No pending pull slips for ${loccodes}");
                        }
                    } else {
                        log.warn('Problem resolving locations or email addresses');
                    }
                } else {
                    log.error('Cannot find a pull slip template');
                }
            }
        } catch ( Exception e ) {
            log.error('Exception thrown in checkPullSlipsFor', e);
        }
    }

    /**
     * send an email to the specified list of recipients
     * @param emailAddresses The email addresses to send the email to
     * @param subject The subject of the email
     * @param body The body of the email
     * @param attachments The list of attachments to send with the email
     */
    private void sendPullSlipMail(
        ArrayList emailAddresses,
        String subject,
        String body,
        List attachments
    ) {
        emailAddresses.each { String to ->
            // Now generate the email parameters
            Map emailParams = [
                notificationId: '1',
                to: to,
                header: subject,
                body: body,
                outputFormat: 'text/html',
                attachments: attachments
            ];

            // We can now send the email
            emailService.sendEmail(emailParams);
        }
    }
}
