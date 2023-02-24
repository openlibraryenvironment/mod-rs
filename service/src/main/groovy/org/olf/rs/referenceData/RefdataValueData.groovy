package org.olf.rs.referenceData;

import com.k_int.web.toolkit.custprops.CustomPropertyDefinition;
import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition;
import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;

import groovy.util.logging.Slf4j;

/**
 * Class that contains the Reference Data values required by ReShare
 * @author Chas
 *
 */
@Slf4j
public class RefdataValueData {

    // Values used in multiple categories
    private static final String OFF = 'Off';
    private static final String ON  = 'On';

    public static final String VOCABULARY_ACCEPT_ITEM_METHOD               = 'AcceptItemMethod';
    public static final String VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE = 'ActopnEventResultSaveRestore';
    public static final String VOCABULARY_AUTO_RESPONDER                   = 'AutoResponder';
    public static final String VOCABULARY_AUTO_RESPONDER_CANCEL            = 'AutoResponder_Cancel';
    public static final String VOCABULARY_AUTO_RESPONDER_LOCAL             = 'AutoResponder_Local';
    public static final String VOCABULARY_BORROWER_CHECK_METHOD            = 'BorrowerCheckMethod';
    public static final String VOCABULARY_CANCELLATION_REASONS             = 'cancellationReasons';
    public static final String VOCABULARY_CANNOT_SUPPLY_REASONS            = 'cannotSupplyReasons';
    public static final String VOCABULARY_CHAT_AUTO_READ                   = 'ChatAutoRead';
    public static final String VOCABULARY_CHECK_IN_METHOD                  = 'CheckInMethod';
    public static final String VOCABULARY_CHECK_OUT_METHOD                 = 'CheckOutMethod';
    public static final String VOCABULARY_CHECK_IN_ON_RETURN               = 'CheckInOnReturn';
    public static final String VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER     = 'HostLMSIntegrationAdapter';
    public static final String VOCABULARY_LOAN_CONDITIONS                  = 'loanConditions';
    public static final String VOCABULARY_LOAN_POLICY                      = 'LoanPolicy';
    public static final String VOCABULARY_NCIP_DUE_DATE                    = 'NCIPDueDate';
    public static final String VOCABULARY_NOTICE_FORMATS                   = 'noticeFormats';
    public static final String VOCABULARY_NOTICE_TRIGGERS                  = 'noticeTriggers';
    public static final String VOCABULARY_PATRON_STORE_ADAPTER             = 'PatronStoreAdapter';
    public static final String VOCABULARY_PULL_SLIP_TEMPLATE               = 'pullslipTemplate';
    public static final String VOCABULARY_REQUEST_ROUTING_ADAPTER          = 'RequestRoutingAdapter';
    public static final String VOCABULARY_SHARED_INDEX_ADAPTER             = 'SharedIndexAdapter';
    public static final String VOCABULARY_YES_NO                           = 'YesNo';
    public static final String VOCABULARY_YES_NO_OTHER                     = 'YNO';

    // Action Event Result Save / Restore
    public static final String ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE = 'Restore';
    public static final String ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE    = 'Save';

    // Auto Responder
    public static final String AUTO_RESPONDER_OFF                          = OFF;
    public static final String AUTO_RESPONDER_ON_WILL_SUPPLY_CANNOT_SUPPLY = 'On: will supply and cannot supply';
    public static final String AUTO_RESPONDER_ON_WILL_SUPPLY_ONLY          = 'On: will supply only';

    // Auto Responder Cancel
    public static final String AUTO_RESPONDER_CANCEL_OFF = OFF;
    public static final String AUTO_RESPONDER_CANCEL_ON  = 'On';

    // Auto Responder Local
    public static final String AUTO_RESPONDER_LOCAL_OFF = OFF;
    public static final String AUTO_RESPONDER_LOCAL_ON  = 'On';

    // Check-in on return
    public static final String CHECK_IN_ON_RETURN_OFF = OFF;
    public static final String CHECK_IN_ON_RETURN_ON  = 'On';

    // Host LMS integration adapter
    public static final String HOST_LMS_INTEGRATION_ADAPTER_ALEPH     = 'Aleph';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_ALMA      = 'ALMA';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_FOLIO     = 'FOLIO';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_KOHA      = 'Koha';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_MANUAL    = 'Manual';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_MILLENIUM = 'Millennium';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_SIERRA    = 'Sierra';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_SYMPHONY  = 'Symphony';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_TLC       = 'TLC';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_VOYAGER   = 'Voyager';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_WMS       = 'WMS';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_WMS2      = 'WMS2';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_HORIZON   = 'Horizon';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_NCSU      = 'NCSU';

    // Loan Policy
    public static final String LOAN_POLICY_LENDING_ALL_TYPES        = 'Lending all types';
    public static final String LOAN_POLICY_NOT_LENDING              = 'Not Lending';
    public static final String LOAN_POLICY_LENDING_PHYSICAL_ONLY    = 'Lending Physical only';
    public static final String LOAN_POLICY_LENDING_ELECTRONIC_ONLY  = 'Lending Electronic only';

    // NCIP due date
    public static final String NCIP_DUE_DATE_OFF = OFF;
    public static final String NCIP_DUE_DATE_ON  = ON;

    // Notice triggers
    public static final String NOTICE_TRIGGER_END_OF_ROTA                    = 'End of rota';
    public static final String NOTICE_TRIGGER_NEW_HOST_LMS_LOCATION          = 'New Host LMS Location';
    public static final String NOTICE_TRIGGER_NEW_HOST_LMS_SHELVING_LOCATION = 'New Host LMS Shelving Location';
    public static final String NOTICE_TRIGGER_NEW_PATRON_PROFILE             = 'New patron profile';
    public static final String NOTICE_TRIGGER_NEW_REQUEST                    = 'New request';
    public static final String NOTICE_TRIGGER_REQUEST_CANCELLED              = 'Request cancelled';

    // Patron store adapter
    public static final String PATRON_STORE_ADAPTER_FOLIO  = 'FOLIO';
    public static final String PATRON_STORE_ADAPTER_MANUAL = 'Manual';

    // Request routing adapter
    public static final String REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX = 'FOLIOSharedIndex';
    public static final String REQUEST_ROUTING_ADAPTER_STATIC             = 'Static';

    // Shared index adapter
    public static final String SHARED_INDEX_ADAPTER_FOLIO = 'FOLIO';
    public static final String SHARED_INDEX_ADAPTER_JISC_LHD = 'jiscDiscover';
    public static final String SHARED_INDEX_ADAPTER_OAIPMH = 'OAIPMH';

    // Yes / No
    public static final String YES_NO_NO  = 'No';
    public static final String YES_NO_YES = 'Yes';

    public static void loadAll() {
        (new RefdataValueData()).load();
    }

   /**
     * Ensures a CustomPropertyDefinition exists in the database
     * @param name the name of the property
     * @param local is this local or not
     * @param category the category the property belongs to
     * @param label the label associated with this property (default: null)
     * @return the CustomPropertyDefinition
     */
    private CustomPropertyDefinition ensureRefdataProperty(String name, boolean local, String category, String label = null) {
        CustomPropertyDefinition result = null;
        RefdataCategory rdc = RefdataCategory.findByDesc(category);

        if (rdc != null) {
            result = CustomPropertyDefinition.findByName(name);
            if (result == null) {
                result = new CustomPropertyRefdataDefinition(
                    name:name,
                    defaultInternal: local,
                    label:label,
                    category: rdc
                );
                // Not entirely sure why type can't be set in the above, but other bootstrap scripts do this
                // the same way, so copying. Type doesn't work when set as a part of the definition above
                result.type = com.k_int.web.toolkit.custprops.types.CustomPropertyRefdata;
                result.save(flush:true, failOnError:true);
            }
        } else {
            println("Unable to find category ${category}");
        }
        return result;
    }

    /**
     * Loads the RfdataValue records into the database
     */
    private void load() {
        try {
            log.info('Adding RefdataValue values to the database');

            RefdataValue.lookupOrCreate(VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE);
            RefdataValue.lookupOrCreate(VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE);

            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_DUE_DATE, NCIP_DUE_DATE_ON);
            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_DUE_DATE, NCIP_DUE_DATE_OFF);


            // External LMS call methods -- none represents no integration and we will spoof a passing response instead
            RefdataValue.lookupOrCreate(VOCABULARY_BORROWER_CHECK_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_BORROWER_CHECK_METHOD, 'NCIP');

            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_OUT_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_OUT_METHOD, 'NCIP');

            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_METHOD, 'NCIP');

            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_ON_RETURN, CHECK_IN_ON_RETURN_OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_ON_RETURN, CHECK_IN_ON_RETURN_ON);

            RefdataValue.lookupOrCreate(VOCABULARY_ACCEPT_ITEM_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_ACCEPT_ITEM_METHOD, 'NCIP');

            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_ALMA);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_ALEPH);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_FOLIO);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_KOHA);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_MANUAL);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_MILLENIUM);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_SIERRA);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_SYMPHONY);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_TLC);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_VOYAGER);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_WMS);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_WMS2);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_HORIZON);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_NCSU);


            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, SHARED_INDEX_ADAPTER_FOLIO);
            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, SHARED_INDEX_ADAPTER_JISC_LHD);
            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, 'OAI-PMH', SHARED_INDEX_ADAPTER_OAIPMH);

            RefdataValue.lookupOrCreate(VOCABULARY_PATRON_STORE_ADAPTER, PATRON_STORE_ADAPTER_FOLIO);
            RefdataValue.lookupOrCreate(VOCABULARY_PATRON_STORE_ADAPTER, PATRON_STORE_ADAPTER_MANUAL);

            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_ON_WILL_SUPPLY_ONLY);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_ON_WILL_SUPPLY_CANNOT_SUPPLY);

            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_CANCEL, AUTO_RESPONDER_CANCEL_OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_CANCEL, AUTO_RESPONDER_CANCEL_ON);

            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_LOCAL, AUTO_RESPONDER_LOCAL_ON);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_LOCAL, AUTO_RESPONDER_LOCAL_OFF);

            RefdataValue.lookupOrCreate(VOCABULARY_YES_NO, YES_NO_NO);
            RefdataValue.lookupOrCreate(VOCABULARY_YES_NO, YES_NO_YES);

            RefdataValue.lookupOrCreate(VOCABULARY_YES_NO_OTHER, 'Yes')
            RefdataValue.lookupOrCreate(VOCABULARY_YES_NO_OTHER, 'No')
            RefdataValue.lookupOrCreate(VOCABULARY_YES_NO_OTHER, 'Other')

            RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, 'No longer available', 'unavailable');
            RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, 'Missing', 'missing');
            RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, 'Incorrect', 'incorrect');
            RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, 'Other', 'other');

            RefdataValue.lookupOrCreate(VOCABULARY_CANCELLATION_REASONS, 'Requested item is locally available', 'available_locally');
            RefdataValue.lookupOrCreate(VOCABULARY_CANCELLATION_REASONS, 'User account is invalid', 'invalid_user');
            RefdataValue.lookupOrCreate(VOCABULARY_CANCELLATION_REASONS, 'User requested cancellation', 'patron_requested');

            RefdataValue.lookupOrCreate(VOCABULARY_CHAT_AUTO_READ, OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_CHAT_AUTO_READ, ON);
            RefdataValue.lookupOrCreate(VOCABULARY_CHAT_AUTO_READ, 'On (excluding action messages)');

            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, 'LibraryUseOnly');
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, 'NoReproduction');
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, 'SignatureRequired');
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, 'SpecCollSupervReq');
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, 'WatchLibraryUseOnly');
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, 'Other');

            // We need to rename "Lendin Physical Only" to Lending Physical Only"
            renameRefdata(VOCABULARY_LOAN_POLICY, 'Lendin Physical Only', LOAN_POLICY_LENDING_PHYSICAL_ONLY);

            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, LOAN_POLICY_LENDING_ALL_TYPES);
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, LOAN_POLICY_NOT_LENDING);
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, LOAN_POLICY_LENDING_PHYSICAL_ONLY);
            RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, LOAN_POLICY_LENDING_ELECTRONIC_ONLY);

            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_FORMATS, 'E-mail', 'email');

            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_REQUEST);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_END_OF_ROTA);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_REQUEST_CANCELLED);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_PATRON_PROFILE);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_HOST_LMS_LOCATION);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_HOST_LMS_SHELVING_LOCATION);

            ensureRefdataProperty('policy.ill.returns', false, 'YNO', 'Accept Returns');
            ensureRefdataProperty('policy.ill.loan_policy', false, 'LoanPolicy', 'ILL Loan Policy');
            ensureRefdataProperty('policy.ill.last_resort', false, 'YNO', 'Consider Institution As Last Resort');

            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX);
            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, REQUEST_ROUTING_ADAPTER_STATIC);

            // This looks slightly odd, but rather than litter this file with an ever growing list of
            // random delete statements, if you wish to delete
            // deprecated refdata values, add a new line to the array here consisting of [ "VALUE", "CATEGORY" ]
            [
                [ 'sirsi', 'HostLMSIntegrationAdapter' ]
            ].each { r ->
                log.warn("Remove refdata value : ${r}");
                try {
                    RefdataValue.executeUpdate('delete from RefdataValue where id in ( select rdv.id from RefdataValue as rdv where rdv.value = :v and rdv.owner.desc = :d)',
                                                  [v:RefdataValue.normValue(r[0]), d:r[1]]);
                } catch (Exception e) {
                    log.error("Unable to delete refdata ${r} - ${e.message}", e);
                }
            }
        } catch (Exception e) {
            log.error('Exception thrown while loading settings', e);
        }
    }

    private void renameRefdata(String category, String oldValue, String newValue) {
        RefdataCategory categoryRefdata = RefdataCategory.findByDesc(category);
        if (categoryRefdata != null) {
            String normValue = RefdataValue.normValue(oldValue);
            RefdataValue valueRefData = RefdataValue.findByOwnerAndValue(categoryRefdata, normValue);
            if (valueRefData != null) {
                valueRefData.label = newValue;
                valueRefData.value = RefdataValue.normValue(newValue);
                valueRefData.save(flush:true, failOnError:true);
            }
        }
    }
}
