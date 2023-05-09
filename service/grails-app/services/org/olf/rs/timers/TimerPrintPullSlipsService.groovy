package org.olf.rs.timers;

import org.olf.rs.EmailService
import org.olf.rs.HostLMSLocation;
import org.olf.rs.OkapiSettingsService
import org.olf.rs.PatronRequest;
import org.olf.templating.TemplateContainer;
import org.olf.templating.TemplatingService;

import com.k_int.web.toolkit.settings.AppSetting;

import groovy.json.JsonSlurper;

/**
 * Checks to see if there are any pull slips that need printing and if there are sends an email if configured
 *
 * @author Chas
 *
 */
public class TimerPrintPullSlipsService extends AbstractTimer {

    private static String PULL_SLIP_QUERY='''
Select pr
from PatronRequest as pr
where pr.pickLocation.id in ( :loccodes ) and
      pr.state in (select sms.state
                   from StateModel as sm
                       inner join sm.states as sms
                   where sm = pr.stateModel and
                         sms.triggerPullSlipEmail = true)
''';

    private static String PULL_SLIP_SUMMARY = '''
Select count(pr.id), pr.pickLocation.code
from PatronRequest as pr
where pr.state in (select sms.state
                   from StateModel as sm
                       inner join sm.states as sms
                   where sm = pr.stateModel and
                         sms.triggerPullSlipEmail = true)
group by pr.pickLocation.code
''';

    EmailService emailService;
    OkapiSettingsService okapiSettingsService;
    TemplatingService templatingService;

    @Override
    public void performTask(String config) {
        log.debug("Fire pull slip timer task. Config is ${config}");
        JsonSlurper jsonSlurper = new JsonSlurper();
        Map task_config = jsonSlurper.parseText(config);
        checkPullSlips(task_config);
    }

    private void checkPullSlips(Map timer_cfg) {
        log.debug("checkPullSlips(${timer_cfg})");
        checkPullSlipsFor(timer_cfg.locations,
                          timer_cfg.confirmNoPendingRequests ?: true,
                          timer_cfg.emailAddresses);
    }

    private void checkPullSlipsFor(ArrayList loccodes,
                                   boolean confirm_no_pending_slips,
                                   ArrayList emailAddresses) {

        // See /configurations/entries?query=code==FOLIO_HOST
        // See /configurations/entries?query=code==localeSettings
        log.debug("checkPullSlipsFor(${loccodes},${confirm_no_pending_slips},${emailAddresses})");

        try {
            AppSetting pull_slip_template_setting = AppSetting.findByKey('pull_slip_template_id');
            TemplateContainer tc = TemplateContainer.read(pull_slip_template_setting?.value);

            if (tc != null) {
                def pull_slip_overall_summary = PatronRequest.executeQuery(PULL_SLIP_SUMMARY);
                log.debug("pull slip summary: ${pull_slip_overall_summary}");

                List<HostLMSLocation> pslocs = HostLMSLocation.executeQuery('select h from HostLMSLocation as h where h.id in ( :loccodes )', [loccodes:loccodes]);

                if ( ( pslocs.size() > 0 ) && ( emailAddresses != null ) ) {
                    log.debug("Resolved locations ${pslocs} - send to ${emailAddresses}");

                    List<PatronRequest> pending_ps_printing = PatronRequest.executeQuery(PULL_SLIP_QUERY, [loccodes:loccodes]);

                    if ( pending_ps_printing != null ) {
                        if ( ( pending_ps_printing.size() > 0 ) || confirm_no_pending_slips ) {
                            log.debug("${pending_ps_printing.size()} pending pull slip printing for locations ${pslocs}");

                            String locationsText = pslocs.inject('', { String output, HostLMSLocation loc ->
                                output + (output != '' ? ', ' : '') + (loc.name ?: loc.code);
                            });

                            String locFilters = loccodes.collect { "location.${it }"}.join('%2C');

                            // Grab the unique list of states that have triggered the bull slip email
                            String stateFilters = null;
                            pending_ps_printing.unique(false) { request ->
                                request.state.code
                            }.each { request ->
                                if (stateFilters == null) {
                                    stateFilters = "";
                                } else {
                                    stateFilters += "%2C";
                                }
                                stateFilters += "state." + request.state.code;
                            }

                            // If there is more than 1 state that can trigger this then
                            // 'from':'admin@reshare.org',
                            def tmplResult = templatingService.performTemplate(
                                tc,
                                [
                                    locations: locationsText,
                                    pendingRequests: pending_ps_printing,
                                    numRequests:pending_ps_printing.size(),
                                    summary: pull_slip_overall_summary,
                                    reshareURL: "${okapiSettingsService.getSetting('FOLIO_HOST')?.value}/supply/requests?filters=${locFilters}%2C${stateFilters}&sort=-dateCreated"
                                ],
                                'en'
                            );

                            emailAddresses.each { to ->
                                Map email_params = [
                                    notificationId: '1',
                                    to: to,
                                    header: tmplResult.result.header,
                                    body: tmplResult.result.body,
                                    outputFormat: 'text/html'
                                ];

                                Map email_result = emailService.sendEmail(email_params);
                            }
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
        } catch ( Exception e ) {
            log.error('Exception thrown in checkPullSlipsFor', e);
        }
    }
}
