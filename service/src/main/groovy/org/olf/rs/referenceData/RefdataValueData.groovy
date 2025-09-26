package org.olf.rs.referenceData

import org.olf.rs.ProtocolReferenceDataValue
import org.olf.rs.constants.CustomIdentifiersScheme;
import org.olf.rs.constants.Directory;

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

    // Yes / No
    public static final String YES_NO_NO  = 'No';
    public static final String YES_NO_YES = 'Yes';

    public static final String VOCABULARY_ACCEPT_ITEM_METHOD                     = 'AcceptItemMethod';
    public static final String VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE       = 'ActopnEventResultSaveRestore';
    public static final String VOCABULARY_AUTO_RESPONDER                         = 'AutoResponder';
    public static final String VOCABULARY_AUTO_RESPONDER_COPY                    = 'AutoResponder_Service_type_copy';
    public static final String VOCABULARY_AUTO_RESPONDER_CANCEL                  = 'AutoResponder_Cancel';
    public static final String VOCABULARY_AUTO_RESPONDER_LOCAL                   = 'AutoResponder_Local';
    public static final String VOCABULARY_BORROWER_CHECK_METHOD                  = 'BorrowerCheckMethod';
    public static final String VOCABULARY_CANCELLATION_REASONS                   = 'cancellationReasons';
    public static final String VOCABULARY_CANNOT_SUPPLY_REASONS                  = 'cannotSupplyReasons';
    public static final String VOCABULARY_SLNP_CANCEL_OR_ABORT_SUPPLY_REASONS    = 'slnpCancelOrAbortSupplyReasons';
    public static final String VOCABULARY_CHAT_AUTO_READ                         = 'ChatAutoRead';
    public static final String VOCABULARY_CHECK_IN_METHOD                        = 'CheckInMethod';
    public static final String VOCABULARY_CHECK_OUT_METHOD                       = 'CheckOutMethod';
    public static final String VOCABULARY_CHECK_IN_ON_RETURN                     = 'CheckInOnReturn';
    public static final String VOCABULARY_COPYRIGHT_TYPE                         = "copyrightType";
    public static final String VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER           = 'HostLMSIntegrationAdapter';
    public static final String VOCABULARY_LOAN_CONDITIONS                        = 'loanConditions';
    public static final String VOCABULARY_LOAN_POLICY                            = 'LoanPolicy';
    public static final String VOCABULARY_NCIP_DUE_DATE                          = 'NCIPDueDate';
    public static final String VOCABULARY_NOTICE_FORMATS                         = 'noticeFormats';
    public static final String VOCABULARY_NOTICE_TRIGGERS                        = 'noticeTriggers';
    public static final String VOCABULARY_PATRON_STORE_ADAPTER                   = 'PatronStoreAdapter';
    public static final String VOCABULARY_PULL_SLIP_TEMPLATE                     = 'pullslipTemplate';
    public static final String VOCABULARY_REQUEST_ROUTING_ADAPTER                = 'RequestRoutingAdapter';
    public static final String VOCABULARY_SHARED_INDEX_ADAPTER                   = 'SharedIndexAdapter';
    public static final String VOCABULARY_YES_NO                                 = 'YesNo';
    public static final String VOCABULARY_YES_NO_OTHER                           = 'YNO';
    public static final String VOCABULARY_NCIP_BARCODE                           = "NCIPAcceptItemUseBarcode";
    public static final String VOCABULARY_NCIP_TITLE                             = "NCIPRequestItemUseTitle"
    public static final String VOCABULARY_NCIP_USE_DEFAULT_PATRON_FEE            = "NCIPUseDefaultPatronFee"
    public static final String VOCABULARY_REQUEST_ITEM_METHOD                    = "RequestItemMethod";
    public static final String VOCABULARY_CUSTOM_IDENTIFIERS_SCHEME              = 'customIdentifiersScheme';
    public static final String VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY   = 'AutoSupply';
    public static final String VOCABULARY_FEATURE_FLAG                           = 'featureFlag';
    public static final String VOCABULARY_CURRENCY_CODES                         = 'CurrencyCodes';
    public static final String VOCABULARY_SERVICE_LEVELS                         = 'ServiceLevels';

    // Action Event Result Save / Restore
    public static final String ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE = 'Restore';
    public static final String ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE    = 'Save';

    // Service type - Loan Auto Responder
    public static final String AUTO_RESPONDER_OFF                          = OFF;
    public static final String AUTO_RESPONDER_ON_WILL_SUPPLY_CANNOT_SUPPLY = 'On: will supply and cannot supply';
    public static final String AUTO_RESPONDER_ON_WILL_SUPPLY_ONLY          = 'On: will supply only';
    public static final String AUTO_RESPONDER_ON_LOANED_CANNOT_SUPPLY      = 'On: loaned and cannot supply';

    // Service type - Copy Auto Responder
    public static final String COPY_AUTO_RESPONDER_OFF                     = OFF;
    public static final String COPY_AUTO_RESPONDER_ON_LOANED_CANNOT_SUPPLY = 'On: loaned and cannot supply';

    // Auto Responder Requester Non Returnable
    public static final String AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_OFF                     = OFF;
    public static final String AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_ON_AVAILABLE            = 'On: available';
    public static final String AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_ON_SUPPLIED             = 'On: supplied';

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
    public static final String HOST_LMS_INTEGRATION_ADAPTER_ALEPH       = 'Aleph';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_ALMA        = 'ALMA';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_FOLIO       = 'FOLIO';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_KOHA        = 'Koha';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_MANUAL      = 'Manual';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_MILLENIUM   = 'Millennium';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_SIERRA      = 'Sierra';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_SYMPHONY    = 'Symphony';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_TLC         = 'TLC';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_VOYAGER     = 'Voyager';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_WMS         = 'WMS';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_WMS2        = 'WMS2';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_HORIZON     = 'Horizon';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_NCSU        = 'NCSU';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_POLARIS     = 'Polaris';
    public static final String HOST_LMS_INTEGRATION_ADAPTER_EVERGREEN   = 'Evergreen';

    // Loan Policy
    public static final String LOAN_POLICY_LENDING_ALL_TYPES        = 'Lending all types';
    public static final String LOAN_POLICY_NOT_LENDING              = 'Not Lending';
    public static final String LOAN_POLICY_LENDING_PHYSICAL_ONLY    = 'Lending Physical only';
    public static final String LOAN_POLICY_LENDING_ELECTRONIC_ONLY  = 'Lending Electronic only';

    // NCIP due date
    public static final String NCIP_DUE_DATE_OFF = OFF;
    public static final String NCIP_DUE_DATE_ON  = ON;

    // NCIP use barcode for Accept Item
    public static final String NCIP_BARCODE_NO = YES_NO_NO;
    public static final String NCIP_BARCODE_YES = YES_NO_YES;

    // Notice triggers
    public static final String NOTICE_TRIGGER_END_OF_ROTA                    = 'End of rota';
    public static final String NOTICE_TRIGGER_END_OF_ROTA_REVIEWED           = 'End of rota reviewed';
    public static final String NOTICE_TRIGGER_LOANED_DIGITALLY               = 'Loaned digitally';
    public static final String NOTICE_TRIGGER_OVER_LIMIT                     = 'Over limit';
    public static final String NOTICE_TRIGGER_NEW_HOST_LMS_LOCATION          = 'New Host LMS Location';
    public static final String NOTICE_TRIGGER_NEW_HOST_LMS_SHELVING_LOCATION = 'New Host LMS Shelving Location';
    public static final String NOTICE_TRIGGER_NEW_PATRON_PROFILE             = 'New patron profile';
    public static final String NOTICE_TRIGGER_NEW_REQUEST                    = 'New request';
    public static final String NOTICE_TRIGGER_NEW_SUPPLY_REQUEST             = 'new_supply_request';
    public static final String NOTICE_TRIGGER_NEW_SUPPLY_REQUEST_EXPEDITED   = 'new_supply_request_expedited';
    public static final String NOTICE_TRIGGER_REQUEST_CANCELLED              = 'Request cancelled';
    public static final String NOTICE_TRIGGER_DOCUMENT_DELIVERED             = 'Document delivered';

    // Patron store adapter
    public static final String PATRON_STORE_ADAPTER_FOLIO  = 'FOLIO';
    public static final String PATRON_STORE_ADAPTER_MANUAL = 'Manual';

    // Request routing adapter
    public static final String REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX = 'FOLIOSharedIndex';
    public static final String REQUEST_ROUTING_ADAPTER_STATIC             = 'Static';
    public static final String REQUEST_ROUTING_ADAPTER_DISABLED           = 'Disabled';

    // Shared index adapter
    public static final String SHARED_INDEX_ADAPTER_FOLIO = 'FOLIO';
    public static final String SHARED_INDEX_ADAPTER_JISC_LHD = 'jiscDiscover';
    public static final String SHARED_INDEX_ADAPTER_OAIPMH = 'OAIPMH';
    public static final String SHARED_INDEX_ADAPTER_MOCK = 'Mock';
    public static final String SHARED_INDEX_ADAPTER_ANBD = "ANBD";


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
     * Loads the RefdataValue records into the database
     */
    private void load() {
        try {
            log.info('Adding RefdataValue values to the database');

            RefdataValue.lookupOrCreate(VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, ACTION_EVENT_RESULT_SAVE_RESTORE_RESTORE);
            RefdataValue.lookupOrCreate(VOCABULARY_ACTION_EVENT_RESULT_SAVE_RESTORE, ACTION_EVENT_RESULT_SAVE_RESTORE_SAVE);

            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_DUE_DATE, NCIP_DUE_DATE_ON);
            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_DUE_DATE, NCIP_DUE_DATE_OFF);

            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_BARCODE, NCIP_BARCODE_NO);
            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_BARCODE, NCIP_BARCODE_YES);

            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_TITLE, NCIP_BARCODE_NO);
            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_TITLE, NCIP_BARCODE_YES);

            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_USE_DEFAULT_PATRON_FEE, NCIP_BARCODE_NO)
            RefdataValue.lookupOrCreate(VOCABULARY_NCIP_USE_DEFAULT_PATRON_FEE, NCIP_BARCODE_YES)

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

            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ITEM_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ITEM_METHOD, 'NCIP');

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
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_POLARIS);
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, HOST_LMS_INTEGRATION_ADAPTER_EVERGREEN);

            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, SHARED_INDEX_ADAPTER_FOLIO);
            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, SHARED_INDEX_ADAPTER_JISC_LHD);
            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, 'OAI-PMH', SHARED_INDEX_ADAPTER_OAIPMH);
            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, SHARED_INDEX_ADAPTER_MOCK);
            RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, SHARED_INDEX_ADAPTER_ANBD);

            RefdataValue.lookupOrCreate(VOCABULARY_PATRON_STORE_ADAPTER, PATRON_STORE_ADAPTER_FOLIO);
            RefdataValue.lookupOrCreate(VOCABULARY_PATRON_STORE_ADAPTER, PATRON_STORE_ADAPTER_MANUAL);

            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_ON_WILL_SUPPLY_ONLY);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_ON_WILL_SUPPLY_CANNOT_SUPPLY);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, AUTO_RESPONDER_ON_LOANED_CANNOT_SUPPLY);

            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_COPY, COPY_AUTO_RESPONDER_OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_COPY, COPY_AUTO_RESPONDER_ON_LOANED_CANNOT_SUPPLY);

            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_OFF);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_ON_AVAILABLE);
            RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_ON_SUPPLIED);

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

            RefdataValue.lookupOrCreate(VOCABULARY_SLNP_CANCEL_OR_ABORT_SUPPLY_REASONS, 'With abort', 'true');
            RefdataValue.lookupOrCreate(VOCABULARY_SLNP_CANCEL_OR_ABORT_SUPPLY_REASONS, 'Without abort', 'false');

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
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, 'New request (supply)', NOTICE_TRIGGER_NEW_SUPPLY_REQUEST);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, 'New expedited request (supply)', NOTICE_TRIGGER_NEW_SUPPLY_REQUEST_EXPEDITED);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_END_OF_ROTA);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_LOANED_DIGITALLY);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_OVER_LIMIT);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_REQUEST_CANCELLED);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_PATRON_PROFILE);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_HOST_LMS_LOCATION);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_NEW_HOST_LMS_SHELVING_LOCATION);
            RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, NOTICE_TRIGGER_DOCUMENT_DELIVERED);

            ensureRefdataProperty(Directory.KEY_ILL_POLICY_RETURNS, false, Directory.CATEGORY_YES_NO, 'Accept Returns');
            ensureRefdataProperty(Directory.KEY_ILL_POLICY_LOAN, false, Directory.CATEGORY_LOAN_POLICY, 'ILL Loan Policy');
            ensureRefdataProperty(Directory.KEY_ILL_POLICY_LAST_RESORT, false, Directory.CATEGORY_YES_NO, 'Consider Institution As Last Resort');

            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Cat S183 – Commonwealth (Australia)', 'AU-CopyRCatS183ComW');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S183 – State (Australia)', 'AU-CopyRCatS183State');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S49  (Australia)', 'AU-CopyrightActS49');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S50[1] (Australia)', 'AU-CopyrightActS50-1');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S50[7]A (Australia)', 'AU-CopyrightActS50-7A');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S50[7]B (Australia)', 'AU-CopyrightActS50-7B');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Cleared (Australia)', 'AU-CopyrightCleared');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'General Business (Australia)', 'AU-GenBus');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S54 (New Zealand)', 'NZ-CopyrightActS54');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Act S55 (New Zealand)', 'NZ-CopyrightActS55');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Other types of copyright compliance', 'Other');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Copyright Fee Paid (UK)', 'UK-CopyRFeePaid');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'Fair Dealing (UK)', 'UK-FairDealing');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'CCG (US) - published within the last 5 years', 'US-CCG');
            RefdataValue.lookupOrCreate(VOCABULARY_COPYRIGHT_TYPE, 'CCL (US) - NOT published within the last 5 years', 'US-CCL');

            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX);
            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, REQUEST_ROUTING_ADAPTER_STATIC);
            RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, REQUEST_ROUTING_ADAPTER_DISABLED);


            RefdataValue.lookupOrCreate(VOCABULARY_CURRENCY_CODES, 'Australian Dollars','AUD');
            RefdataValue.lookupOrCreate(VOCABULARY_CURRENCY_CODES, 'Canadian Dollars', 'CAD');
            RefdataValue.lookupOrCreate(VOCABULARY_CURRENCY_CODES, 'Euros', 'EUR');
            RefdataValue.lookupOrCreate(VOCABULARY_CURRENCY_CODES, 'United States Dollars', 'USD');
            RefdataValue.lookupOrCreate(VOCABULARY_CURRENCY_CODES, 'Swedish Krona', 'SEK');
            RefdataValue.lookupOrCreate(VOCABULARY_CURRENCY_CODES, 'Danish Kroner','DKK');

            RefdataValue.lookupOrCreate(VOCABULARY_SERVICE_LEVELS, 'Express', 'Express');
            RefdataValue.lookupOrCreate(VOCABULARY_SERVICE_LEVELS, 'Normal', 'Normal');
            RefdataValue.lookupOrCreate(VOCABULARY_SERVICE_LEVELS, 'SecondaryMail', 'SecondaryMail');
            RefdataValue.lookupOrCreate(VOCABULARY_SERVICE_LEVELS, 'Standard', 'Standard');
            RefdataValue.lookupOrCreate(VOCABULARY_SERVICE_LEVELS, 'Urgent', 'Urgent');
            RefdataValue.lookupOrCreate(VOCABULARY_SERVICE_LEVELS, 'Rush', 'Rush');


            // ISO18626 Custom options for - Custom identifiers schemes
            RefdataValue.lookupOrCreate(VOCABULARY_CUSTOM_IDENTIFIERS_SCHEME, CustomIdentifiersScheme.ZFL);

            // ISO18626 Custom options for - Custom identifiers schemes
            RefdataValue.lookupOrCreate(VOCABULARY_CUSTOM_IDENTIFIERS_SCHEME, CustomIdentifiersScheme.ZFL);

            ProtocolReferenceDataValue.lookupOrCreate(ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE, ProtocolReferenceDataValue.SERVICE_TYPE_NO)
            ProtocolReferenceDataValue.lookupOrCreate(ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE, ProtocolReferenceDataValue.SERVICE_TYPE_LOAN)
            ProtocolReferenceDataValue.lookupOrCreate(ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE, ProtocolReferenceDataValue.SERVICE_TYPE_COPY)
            ProtocolReferenceDataValue.lookupOrCreate(ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE, ProtocolReferenceDataValue.SERVICE_TYPE_COPY_OR_LOAN)

            // This looks slightly odd, but rather than litter this file with an ever growing list of
            // random delete statements, if you wish to delete
            // deprecated refdata values, add a new line to the array here consisting of [ "VALUE", "CATEGORY" ]
            [
                [ 'sirsi', 'HostLMSIntegrationAdapter' ],
                [ 'on:_auto_loan', 'AutoResponder']
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

    // To create an empty vocabulary (RefdataCategory doesn't provide this)
    private static RefdataCategory lookupOrCreateCategory(String categoryName) {
        RefdataCategory cat = RefdataCategory.findByDesc(categoryName);
        if (!cat) {
            cat = new RefdataCategory();
            cat.desc = categoryName;
            cat.internal = false;
            cat.save(flush:true, failOnError:true);
        }
        return cat;
    }
}
