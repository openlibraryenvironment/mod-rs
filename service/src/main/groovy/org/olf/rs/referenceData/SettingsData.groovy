package org.olf.rs.referenceData

import org.olf.rs.ReferenceDataService;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.StateModel;

import com.k_int.web.toolkit.files.FileUploadService;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.rs.ProtocolReferenceDataValue;

import groovy.util.logging.Slf4j;

/**
 * Class that contains the Settings and Reference Data values required by ReShare
 * @author Chas
 *
 */
@Slf4j
public class SettingsData {

    private static final String SETTING_TYPE_PASSWORD = 'Password';
    private static final String SETTING_TYPE_REF_DATA = 'Refdata';
    private static final String SETTING_TYPE_STRING   = 'String';
    private static final String SETTING_TYPE_TEMPLATE = 'Template';

    private static final String SECTION_AUTO_RESPONDER         = 'autoResponder';
    private static final String SECTION_CHAT                   = 'chat';
    private static final String SECTION_FILE_STORAGE           = 'fileStorage';
    private static final String SECTION_HOST_LMS_INTEGRATION   = 'hostLMSIntegration';
    private static final String SECTION_LOCAL_NCIP             = 'localNCIP';
    private static final String SECTION_LOGGING                = 'logging';
    private static final String SECTION_OTHER                  = 'other';
    private static final String SECTION_NETWORK                = 'network';
    private static final String SECTION_PATRON_STORE           = 'patronStore';
    private static final String SECTION_PULLSLIP_CONFIGURATION = 'pullslipConfiguration';
    private static final String SECTION_PULLSLIP_TEMPLATE      = 'pullslipTemplateConfig';
    private static final String SECTION_REQUESTS               = 'requests';
    private static final String SECTION_ROUTING                = 'Routing';
    private static final String SECTION_SHARED_INDEX           = 'sharedIndex';
    public static final  String SECTION_STATE_ACTION_CONFIG    = 'state_action_config';
    private static final String SECTION_STATE_MODEL            = 'state_model';
    private static final String SECTION_WMS                    = 'wmsSettings';
    private static final String SECTION_VOYAGER                = 'voyagerSettings';
    private static final String SECTION_Z3950                  = 'z3950';
    private static final String SECTION_AUTOMATIC_FEES         = 'automaticFees';
    private static final String SECTION_FEATURE_FLAGS          = 'featureFlags';

    // Settings for the z3950 section
    public static final String SETTING_Z3950_SERVER_ADDRESS = 'z3950_server_address';
    public static final String SETTING_Z3950_PROXY_ADDRESS = 'z3950_proxy_address';

    // Settings for the localNCIP section
    public static final String SETTING_NCIP_APP_PROFILE                  = 'ncip_app_profile';
    public static final String SETTING_NCIP_FROM_AGENCY                  = 'ncip_from_agency';
    public static final String SETTING_NCIP_FROM_AGENCY_AUTHENTICATION   = 'ncip_from_agency_authentication';
    public static final String SETTING_NCIP_SERVER_ADDRESS               = 'ncip_server_address';
    public static final String SETTING_NCIP_TO_AGENCY                    = 'ncip_to_agency';
    public static final String SETTING_NCIP_USE_DUE_DATE                 = 'ncip_use_due_date';
    public static final String SETTING_NCIP_DUE_DATE_FORMAT              = 'ncip_due_date_format';
    public static final String SETTING_NCIP_USE_BARCODE                  = 'ncip_use_barcode_for_accept_item';
    public static final String SETTING_NCIP_USE_TITLE                    = 'ncip_use_title_request_type'
    public static final String SETTING_NCIP_REQUEST_ITEM_PICKUP_LOCATION = 'ncip_request_item_pickup_location'

    // Settings for the wmsSettings section
    public static final String SETTING_WMS_API_KEY                = 'wms_api_key';
    public static final String SETTING_WMS_API_SECRET             = 'wms_api_secret';
    public static final String SETTING_WMS_CONNECTOR_ADDRESS      = 'wms_connector_address';
    public static final String SETTING_WMS_CONNECTOR_PASSWORD     = 'wms_connector_password';
    public static final String SETTING_WMS_CONNECTOR_USERNAME     = 'wms_connector_username';
    public static final String SETTING_WMS_LOOKUP_PATRON_ENDPOINT = 'wms_lookup_patron_endpoint';
    public static final String SETTING_WMS_REGISTRY_ID            = 'wms_registry_id';

    // Settings for the voyagerSettings section
    public static final String SETTING_VOYAGER_ITEM_API_ADDRESS   = 'voyager_item_api_address';

    // Settings for the pull slip configuration section
    public static final String SETTING_PULL_SLIP_LOGO_ID          = 'pull_slip_logo_id';
    public static final String SETTING_PULL_SLIP_MAX_ITEMS        = 'pull_slip_max_items';
    public static final String SETTING_PULL_SLIP_MAX_ITEMS_MANUAL = 'pull_slip_max_items_manual';
    public static final String SETTING_PULL_SLIP_REPORT_ID        = 'pull_slip_report_id';

    // Settings for the pullslipTemplateConfig section
    public static final String SETTING_PULL_SLIP_TEMPLATE_ID = 'pull_slip_template_id';

    // Settings for the hostLMSIntegration section
    public static final String SETTING_ACCEPT_ITEM          = 'accept_item';
    public static final String SETTING_BORROWER_CHECK       = 'borrower_check';
    public static final String SETTING_CHECK_IN_ITEM        = 'check_in_item';
    public static final String SETTING_CHECK_OUT_ITEM       = 'check_out_item';
    public static final String SETTING_CHECK_IN_ON_RETURN   = 'check_in_on_return';
    public static final String SETTING_HOST_LMS_INTEGRATION = 'host_lms_integration';
    public static final String SETTING_USE_REQUEST_ITEM     = 'use_request_item';

    // Settings for the requests section
    public static final String SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID = 'default_institutional_patron_id';
    public static final String SETTING_DEFAULT_REQUEST_SYMBOL          = 'default_request_symbol';
    public static final String SETTING_LAST_RESORT_LENDERS             = 'last_resort_lenders';
    public static final String SETTING_MAX_REQUESTS                    = 'max_requests';
    public static final String SETTING_REQUEST_ID_PREFIX               = 'request_id_prefix';
    public static final String SETTING_LOCAL_SYMBOLS                   = 'local_symbols';
    public static final String SETTING_DEFAULT_PEER_SYMBOL             = 'default_peer_symbol';
    public static final String SETTING_FREE_PICKUP_LOCATION            = 'free_text_pickup_location';
    public static final String SETTING_DIRECTORY_API                   = 'directory_api_url';
    public static final String SETTING_DEFAULT_LOAN_PERIOD             = 'default_loan_period';


    // Settings for the sharedIndex section
    public static final String SETTING_SHARED_INDEX_BASE_URL    = 'shared_index_base_url';
    public static final String SETTING_SHARED_INDEX_INTEGRATION = 'shared_index_integration';
    public static final String SETTING_SHARED_INDEX_PASS        = 'shared_index_pass';
    public static final String SETTING_SHARED_INDEX_TENANT      = 'shared_index_tenant';
    public static final String SETTING_SHARED_INDEX_USER        = 'shared_index_user';

    // Settings for the patronStore section
    public static final String SETTING_PATRON_STORE          = 'patron_store';
    public static final String SETTING_PATRON_STORE_BASE_URL = 'patron_store_base_url';
    public static final String SETTING_PATRON_STORE_GROUP    = 'patron_store_group';
    public static final String SETTING_PATRON_STORE_PASS     = 'patron_store_pass';
    public static final String SETTING_PATRON_STORE_TENANT   = 'patron_store_tenant';
    public static final String SETTING_PATRON_STORE_USER     = 'patron_store_user';

    // Settings for the autoResponder section

    public static final String SETTING_AUTO_RESPONDER_CANCEL                    = 'auto_responder_cancel';
    public static final String SETTING_AUTO_RESPONDER_LOCAL                     = 'auto_responder_local';
    public static final String SETTING_AUTO_RESPONDER_STATUS                    = 'auto_responder_status';
    public static final String SETTING_COPY_AUTO_RESPONDER_STATUS               = 'copy_auto_responder_status';
    public static final String SETTING_STALE_REQUEST_2_DAYS                     = 'stale_request_2_days';
    public static final String SETTING_STALE_REQUEST_1_ENABLED                  = 'stale_request_1_enabled';
    public static final String SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND          = 'stale_request_3_exclude_weekend';
    public static final String SETTING_STALE_REQUEST_RUSH_HOURS                 = 'stale_request_rush_hours';
    public static final String SETTING_STALE_REQUEST_EXPRESS_HOURS              = 'stale_request_express_hours';
    public static final String SETTING_CHECK_DUPLICATE_TIME                     = 'check_duplicate_time';
    public static final String SETTING_AUTO_RESPONDER_REQUESTER_NON_RETURNABLE  = 'auto_responder_requester_non_ret';

    public static final String SETTING_AUTO_REREQUEST                           = 'auto_rerequest';

    // Settings for the chat section
    public static final String SETTING_CHAT_AUTO_READ = 'chat_auto_read';

    // Settings for the Routing section
    public static final String SETTING_ROUTING_ADAPTER = 'routing_adapter';
    public static final String SETTING_STATIC_ROUTES   = 'static_routes';

    // Settings for the Other section
    public static final String SETTING_DEFAULT_COPYRIGHT_TYPE = 'default_copyright_type';
    public static final String SETTING_DEFAULT_SERVICE_LEVEL = 'default_service_level';
    public static final String SETTING_DISPLAYED_SERVICE_LEVELS = 'displayed_service_levels';

    // State/Action configuration settings
    public static final String SETTING_COMBINE_FILL_AND_SHIP                      = 'combine_fill_and_ship';
    public static final String SETTING_COMBINE_RETURNED_BY_PATRON_AND_RETURN_SHIP = 'combine_returned_by_patron_and_return_ship';

    // Network configuration settings
    public static final String SETTING_NETWORK_MAXIMUM_SEND_ATTEMPTS    = 'network_maximum_send_attempts';
    public static final String SETTING_NETWORK_RETRY_PERIOD             = 'network_retry_period';
    public static final String SETTING_NETWORK_TIMEOUT_PERIOD           = 'network_timeout_period';
    public static final String SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS = 'iso18626_gateway_address';

    // State model configuration settings
    public static final String SETTING_STATE_MODEL_REQUESTER                        = 'state_model_requester';
    public static final String SETTING_STATE_MODEL_REQUESTER_RETURNABLE             = 'requester_returnables_state_model';
    public static final String SETTING_STATE_MODEL_REQUESTER_NON_RETURNABLE         = 'requester_non_returnables_state_model';
    public static final String SETTING_STATE_MODEL_REQUESTER_DIGITAL_RETURNABLE     = 'requester_digital_returnables_state_model';
    public static final String SETTING_STATE_MODEL_RESPONDER_RETURNABLE             = 'responder_returnables_state_model';
    public static final String SETTING_STATE_MODEL_RESPONDER_NON_RETURNABLE         = 'responder_non_returnables_state_model';
    public static final String SETTING_STATE_MODEL_RESPONDER_CDL                    = 'state_model_responder_cdl';
    public static final String SETTING_STATE_MODEL_RESPONDER                        = 'state_model_responder';

    private static final String FEATURE_FLAG = ".feature_flag"

    // Section, key feature flags (Hide separate key for section)
    public static final String SETTING_FEATURE_FLAG_STATE_ACTION_CONFIGURATION_COMBINE_FILL_AND_SHIP  = SECTION_STATE_ACTION_CONFIG + '.' + SETTING_COMBINE_FILL_AND_SHIP + FEATURE_FLAG;

    // Section feature flags (Hide whole section)
    public static final String SETTING_FEATURE_FLAG_AUTOMATIC_FEES            = SECTION_AUTOMATIC_FEES + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_CHAT                      = SECTION_CHAT + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_FILE_STORAGE              = SECTION_FILE_STORAGE + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_PATRON_STORE              = SECTION_PATRON_STORE + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_ROUTING                   = SECTION_ROUTING + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_SHARED_INDEX              = SECTION_SHARED_INDEX + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_VOYAGER_SETTINGS          = SECTION_VOYAGER + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_WMS_SETTINGS              = SECTION_WMS + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_Z3950                     = SECTION_Z3950 + FEATURE_FLAG;

    // Endpoint feature flags (Return 404 if disabled)
    public static final String SETTING_FEATURE_FLAG_HOST_LMS_ITEM_LOAN_POLICIES          = "hostLMSItemLoanPolicies" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_HOST_LMS_LOCATIONS                   = "hostLMSLocations" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_HOST_LMS_PATRON_PROFILES             = "hostLMSPatronProfiles" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_HOST_LMS_SHELVING_LOCATIONS          = "hostLMSShelvingLocations" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_NOTICE_POLICIES                      = "noticePolicies" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_NOTICE_TEMPLATES                     = "notices" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_PULL_SLIP_NOTIFICATION_TEMPLATES     = "pullslipTemplates" + FEATURE_FLAG;
    public static final String SETTING_FEATURE_FLAG_PULL_SLIP_NOTIFICATIONS              = "pullslipNotifications" + FEATURE_FLAG;

    // Automatic fees settings
    public static final String SETTING_REQUEST_SERVICE_TYPE  = 'request_service_type';

    public static final String SETTING_FILE_STORAGE_ENGINE           = 'storageEngine';
    public static final String SETTING_FILE_STORAGE_S3_ENDPOINT      = 'S3Endpoint';
    public static final String SETTING_FILE_STORAGE_S3_ACCESS_KEY    = 'S3AccessKey';
    public static final String SETTING_FILE_STORAGE_S3_SECRET_KEY    = 'S3SecretKey';
    public static final String SETTING_FILE_STORAGE_S3_BUCKET_NAME   = 'S3BucketName';
    public static final String SETTING_FILE_STORAGE_S3_BUCKET_REGION = 'S3BucketRegion';
    public static final String SETTING_FILE_STORAGE_S3_OBJECT_PREFIX = 'S3ObjectPrefix';

    public static final String SETTING_LOGGING_ISO18626             = 'loggingISO18626';
    public static final String SETTING_LOGGING_ISO18626_DAYS        = 'loggingISO18626Days';
    public static final String SETTING_LOGGING_NCIP                 = 'loggingNCIP';
    public static final String SETTING_LOGGING_NCIP_DAYS            = 'loggingNCIPDays';
    public static final String SETTING_LOGGING_Z3950_REQUESTER      = 'loggingZ3950Requester';
    public static final String SETTING_LOGGING_Z3950_REQUESTER_DAYS = 'loggingZ3950RequesterDays';
    public static final String SETTING_LOGGING_Z3950_RESPONDER      = 'loggingZ3950Responder';
    public static final String SETTING_LOGGING_Z3950_RESPONDER_DAYS = 'loggingZ3950ResponderDays';

    public static void loadAll() {
        (new SettingsData()).load();
    }

    /**
     * Ensures an AppSetting value exists
     * @param key the key to the app setting
     * @param section the section this value belongs to
     * @param settingType the type ov value this setting takes
     * @param vocabulary if it this setting is a lookup into reference data, this is the key to lookup the values (default: null)
     * @param defaultValue the default value if non has been supplied (default: null)
     * @param value the the value that it is given to start off with (default: null)
     * @return the AppSetting that exists or has been created
     */
    public AppSetting ensureAppSetting(String key, String section, String settingType, String vocabulary = null, String defaultValue = null, String value = null, boolean hidden = false) {
        AppSetting result = AppSetting.findByKey(key);
        if (result == null) {
            result = new AppSetting(
                section: section,
                settingType: settingType,
                vocab: vocabulary,
                key: key,
                defValue: defaultValue,
                value: value,
                hidden: hidden
            );
            result.save(flush:true, failOnError:true);
        }
        return result;
    }

    /**
     * Deletes the AppSetting entity with the specified key.
     *
     * @param key The key of the AppSetting to be deleted.
     * @return The AppSetting that was deleted, or null if no such AppSetting exists.
     */
    AppSetting deleteByKey(String key) {
        AppSetting result = AppSetting.findByKey(key)
        if (result) {
            result.delete(flush: true)
        }
        return result
    }

    /**
     * Loads the settings into the database
     */
    public void load() {
        try {
            log.info('Adding settings to the database');
            // We are not a service, so we need to look it up
            ReferenceDataService referenceDataService = ReferenceDataService.getInstance();

            // Remove any app setting by key
            deleteByKey('feature_flag_automatic_fees')
            deleteByKey('pullslipConfiguration.feature_flag')
            deleteByKey('template.feature_flag')
            deleteByKey('shelvingLocations.feature_flag')
            deleteByKey('ncip_use_default_patron_fee')
            deleteByKey('automaticFees.feature_flag')
            deleteByKey('automatic_fees')

            ensureAppSetting(SETTING_Z3950_SERVER_ADDRESS, SECTION_Z3950, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_Z3950_PROXY_ADDRESS, SECTION_Z3950, SETTING_TYPE_STRING, null, 'http://reshare-mp.folio-dev.indexdata.com:9000');

            ensureAppSetting(SETTING_NCIP_SERVER_ADDRESS, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_NCIP_FROM_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_FROM_AGENCY_AUTHENTICATION, SECTION_LOCAL_NCIP, SETTING_TYPE_PASSWORD);
            ensureAppSetting(SETTING_NCIP_TO_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_APP_PROFILE, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, 'EZBORROW');
            ensureAppSetting(SETTING_NCIP_USE_DUE_DATE, SECTION_LOCAL_NCIP, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_NCIP_DUE_DATE, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_NCIP_DUE_DATE, RefdataValueData.NCIP_DUE_DATE_ON).value);
            ensureAppSetting(SETTING_NCIP_DUE_DATE_FORMAT, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, ReshareActionService.DEFAULT_DATE_FORMAT);
            ensureAppSetting(SETTING_NCIP_USE_BARCODE, SECTION_LOCAL_NCIP, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_NCIP_BARCODE, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_NCIP_BARCODE, RefdataValueData.NCIP_BARCODE_NO).value);
            ensureAppSetting(SETTING_NCIP_REQUEST_ITEM_PICKUP_LOCATION, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_USE_TITLE, SECTION_LOCAL_NCIP, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_NCIP_TITLE, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_NCIP_TITLE, RefdataValueData.NCIP_BARCODE_NO).value)

            ensureAppSetting(SETTING_WMS_API_KEY, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_API_SECRET, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_LOOKUP_PATRON_ENDPOINT, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_REGISTRY_ID, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_CONNECTOR_ADDRESS, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_CONNECTOR_USERNAME, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_CONNECTOR_PASSWORD, SECTION_WMS, SETTING_TYPE_STRING);

            ensureAppSetting(SETTING_VOYAGER_ITEM_API_ADDRESS, SECTION_VOYAGER, SETTING_TYPE_STRING);

            ensureAppSetting(SETTING_PULL_SLIP_TEMPLATE_ID, SECTION_PULLSLIP_TEMPLATE, SETTING_TYPE_TEMPLATE, RefdataValueData.VOCABULARY_PULL_SLIP_TEMPLATE);

            ensureAppSetting(SETTING_PULL_SLIP_REPORT_ID, SECTION_PULLSLIP_CONFIGURATION, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_PULL_SLIP_LOGO_ID, SECTION_PULLSLIP_CONFIGURATION, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_PULL_SLIP_MAX_ITEMS, SECTION_PULLSLIP_CONFIGURATION, SETTING_TYPE_STRING, null, "500", null, true);
            ensureAppSetting(SETTING_PULL_SLIP_MAX_ITEMS_MANUAL, SECTION_PULLSLIP_CONFIGURATION, SETTING_TYPE_STRING, null, "500", null, true);

            ensureAppSetting(SETTING_BORROWER_CHECK, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_BORROWER_CHECK_METHOD);
            ensureAppSetting(SETTING_CHECK_OUT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_OUT_METHOD);
            ensureAppSetting(SETTING_CHECK_IN_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_IN_METHOD);
            ensureAppSetting(SETTING_CHECK_IN_ON_RETURN, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_IN_ON_RETURN, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_CHECK_IN_ON_RETURN, RefdataValueData.CHECK_IN_ON_RETURN_OFF).value);
            ensureAppSetting(SETTING_ACCEPT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_ACCEPT_ITEM_METHOD);
            ensureAppSetting(SETTING_HOST_LMS_INTEGRATION, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, RefdataValueData.HOST_LMS_INTEGRATION_ADAPTER_MANUAL).value);
            ensureAppSetting(SETTING_USE_REQUEST_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_REQUEST_ITEM_METHOD);

            ensureAppSetting(SETTING_REQUEST_ID_PREFIX, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_DEFAULT_REQUEST_SYMBOL, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_LAST_RESORT_LENDERS, SECTION_REQUESTS, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_MAX_REQUESTS, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_LOCAL_SYMBOLS, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_DEFAULT_PEER_SYMBOL, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FREE_PICKUP_LOCATION, SECTION_REQUESTS, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_DIRECTORY_API, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_DEFAULT_LOAN_PERIOD, SECTION_REQUESTS, SETTING_TYPE_STRING);

            ensureAppSetting(SETTING_SHARED_INDEX_INTEGRATION, SECTION_SHARED_INDEX, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_SHARED_INDEX_ADAPTER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_SHARED_INDEX_ADAPTER, RefdataValueData.SHARED_INDEX_ADAPTER_FOLIO).value);
            ensureAppSetting(SETTING_SHARED_INDEX_BASE_URL, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, 'http://shared-index.reshare-dev.indexdata.com:9130');
            ensureAppSetting(SETTING_SHARED_INDEX_USER, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, 'diku_admin');
            ensureAppSetting(SETTING_SHARED_INDEX_PASS, SECTION_SHARED_INDEX, SETTING_TYPE_PASSWORD, null, '');
            ensureAppSetting(SETTING_SHARED_INDEX_TENANT, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, 'diku');

            ensureAppSetting(SETTING_PATRON_STORE_BASE_URL, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'http://127.0.0.1:9130');
            ensureAppSetting(SETTING_PATRON_STORE_TENANT, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'diku');
            ensureAppSetting(SETTING_PATRON_STORE_USER, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'diku_admin');
            ensureAppSetting(SETTING_PATRON_STORE_PASS, SECTION_PATRON_STORE, SETTING_TYPE_PASSWORD, null, '');
            ensureAppSetting(SETTING_PATRON_STORE_GROUP, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'bdc2b6d4-5ceb-4a12-ab46-249b9a68473e');
            ensureAppSetting(SETTING_PATRON_STORE, SECTION_PATRON_STORE, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_PATRON_STORE_ADAPTER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_PATRON_STORE_ADAPTER, RefdataValueData.PATRON_STORE_ADAPTER_MANUAL).value);

            ensureAppSetting(SETTING_AUTO_RESPONDER_STATUS, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER, RefdataValueData.AUTO_RESPONDER_ON_WILL_SUPPLY_CANNOT_SUPPLY).value);
            ensureAppSetting(SETTING_AUTO_RESPONDER_CANCEL, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_CANCEL, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_CANCEL, RefdataValueData.AUTO_RESPONDER_CANCEL_ON).value);
            ensureAppSetting(SETTING_AUTO_RESPONDER_LOCAL, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_LOCAL, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_LOCAL, RefdataValueData.AUTO_RESPONDER_LOCAL_OFF).value);
            ensureAppSetting(SETTING_AUTO_RESPONDER_STATUS, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER, RefdataValueData.AUTO_RESPONDER_ON_LOANED_CANNOT_SUPPLY).value);

            ensureAppSetting(SETTING_COPY_AUTO_RESPONDER_STATUS, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_COPY, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_COPY, RefdataValueData.COPY_AUTO_RESPONDER_OFF).value);
            ensureAppSetting(SETTING_COPY_AUTO_RESPONDER_STATUS, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_COPY, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_COPY, RefdataValueData.COPY_AUTO_RESPONDER_ON_LOANED_CANNOT_SUPPLY).value);

            ensureAppSetting(SETTING_AUTO_RESPONDER_REQUESTER_NON_RETURNABLE, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, RefdataValueData.AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_OFF).value);
            ensureAppSetting(SETTING_AUTO_RESPONDER_REQUESTER_NON_RETURNABLE, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, RefdataValueData.AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_ON_AVAILABLE).value)
            ensureAppSetting(SETTING_AUTO_RESPONDER_REQUESTER_NON_RETURNABLE, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY, RefdataValueData.AUTO_RESPONDER_REQUESTER_AUTO_SUPPLY_ON_SUPPLIED).value);

            // Setup the Stale request settings (added the numbers so they appear in the order I want them in
            ensureAppSetting(SETTING_STALE_REQUEST_1_ENABLED, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_STALE_REQUEST_2_DAYS, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null, '3');
            ensureAppSetting(SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_YES).value);

            ensureAppSetting(SETTING_STALE_REQUEST_RUSH_HOURS, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null);
            ensureAppSetting(SETTING_STALE_REQUEST_EXPRESS_HOURS, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null);

            ensureAppSetting(SETTING_CHECK_DUPLICATE_TIME, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null, '0');

            ensureAppSetting(SETTING_AUTO_REREQUEST, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);

            ensureAppSetting(SETTING_CHAT_AUTO_READ, SECTION_CHAT, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHAT_AUTO_READ, 'on');

            ensureAppSetting(SETTING_ROUTING_ADAPTER, SECTION_ROUTING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_REQUEST_ROUTING_ADAPTER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_REQUEST_ROUTING_ADAPTER, RefdataValueData.REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX).value);
            ensureAppSetting(SETTING_STATIC_ROUTES, SECTION_ROUTING, SETTING_TYPE_STRING, null, null, '');

            ensureAppSetting(SETTING_COMBINE_FILL_AND_SHIP, SECTION_STATE_ACTION_CONFIG, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_COMBINE_RETURNED_BY_PATRON_AND_RETURN_SHIP, SECTION_STATE_ACTION_CONFIG, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);

            ensureAppSetting(SETTING_NETWORK_MAXIMUM_SEND_ATTEMPTS, SECTION_NETWORK, SETTING_TYPE_STRING, null, '3');
            ensureAppSetting(SETTING_NETWORK_RETRY_PERIOD, SECTION_NETWORK, SETTING_TYPE_STRING, null, '10');
            ensureAppSetting(SETTING_NETWORK_TIMEOUT_PERIOD, SECTION_NETWORK, SETTING_TYPE_STRING, null, '30');
            ensureAppSetting(SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS, SECTION_NETWORK, SETTING_TYPE_STRING, null, null)

            ensureAppSetting(SETTING_STATE_MODEL_REQUESTER, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_REQUESTER, null, true);
            ensureAppSetting(SETTING_STATE_MODEL_RESPONDER, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_RESPONDER, null, true);

            ensureAppSetting(SETTING_FILE_STORAGE_ENGINE, SECTION_FILE_STORAGE, SETTING_TYPE_STRING, null, FileUploadService.S3_STORAGE_ENGINE);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_ENDPOINT, SECTION_FILE_STORAGE, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_ACCESS_KEY, SECTION_FILE_STORAGE, SETTING_TYPE_PASSWORD);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_SECRET_KEY, SECTION_FILE_STORAGE, SETTING_TYPE_PASSWORD);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_BUCKET_NAME, SECTION_FILE_STORAGE, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_BUCKET_REGION, SECTION_FILE_STORAGE, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_OBJECT_PREFIX, SECTION_FILE_STORAGE, SETTING_TYPE_STRING, null, 'reshare-');

            ensureAppSetting(SETTING_LOGGING_ISO18626, SECTION_LOGGING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_LOGGING_ISO18626_DAYS, SECTION_LOGGING, SETTING_TYPE_STRING, null, '30');
            ensureAppSetting(SETTING_LOGGING_NCIP, SECTION_LOGGING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_LOGGING_NCIP_DAYS, SECTION_LOGGING, SETTING_TYPE_STRING, null, '30');
            ensureAppSetting(SETTING_LOGGING_Z3950_REQUESTER, SECTION_LOGGING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_LOGGING_Z3950_REQUESTER_DAYS, SECTION_LOGGING, SETTING_TYPE_STRING, null, '30');
            ensureAppSetting(SETTING_LOGGING_Z3950_RESPONDER, SECTION_LOGGING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_LOGGING_Z3950_RESPONDER_DAYS, SECTION_LOGGING, SETTING_TYPE_STRING, null, '30');

            ensureAppSetting(SETTING_DEFAULT_COPYRIGHT_TYPE, SECTION_OTHER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_COPYRIGHT_TYPE);
            ensureAppSetting(SETTING_DEFAULT_SERVICE_LEVEL, SECTION_OTHER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_SERVICE_LEVELS);
            ensureAppSetting(SETTING_DISPLAYED_SERVICE_LEVELS, SECTION_OTHER, SETTING_TYPE_STRING);

            // Requester state model values
            ensureAppSetting(SETTING_STATE_MODEL_REQUESTER_NON_RETURNABLE, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_NR_REQUESTER, null, true);
            ensureAppSetting(SETTING_STATE_MODEL_REQUESTER_RETURNABLE, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_REQUESTER, null, true);
            ensureAppSetting(SETTING_STATE_MODEL_REQUESTER_DIGITAL_RETURNABLE, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER, null, true);

            // Responder state model values
            ensureAppSetting(SETTING_STATE_MODEL_RESPONDER_NON_RETURNABLE, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_NR_RESPONDER, null, true);
            ensureAppSetting(SETTING_STATE_MODEL_RESPONDER_RETURNABLE, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_RESPONDER, null, true);
            ensureAppSetting(SETTING_STATE_MODEL_RESPONDER_CDL, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_CDL_RESPONDER, null, true);


            ensureAppSetting(SETTING_REQUEST_SERVICE_TYPE, SECTION_AUTOMATIC_FEES, SETTING_TYPE_REF_DATA,  ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE, null, null)

            // Feature flag values for Settings sections
            ensureAppSetting(SETTING_FEATURE_FLAG_AUTOMATIC_FEES, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, "true", true);
            ensureAppSetting(SETTING_FEATURE_FLAG_CHAT, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_FILE_STORAGE, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_PATRON_STORE, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_ROUTING, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_SHARED_INDEX, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_VOYAGER_SETTINGS, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_WMS_SETTINGS, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_Z3950, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_PULL_SLIP_NOTIFICATION_TEMPLATES, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_HOST_LMS_ITEM_LOAN_POLICIES, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_HOST_LMS_LOCATIONS, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_HOST_LMS_PATRON_PROFILES, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_HOST_LMS_SHELVING_LOCATIONS, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_NOTICE_POLICIES, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_NOTICE_TEMPLATES, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);
            ensureAppSetting(SETTING_FEATURE_FLAG_PULL_SLIP_NOTIFICATIONS, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING, RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);

            // Feature flag values for Settings sections keys
            ensureAppSetting(SETTING_FEATURE_FLAG_STATE_ACTION_CONFIGURATION_COMBINE_FILL_AND_SHIP, SECTION_FEATURE_FLAGS, SETTING_TYPE_STRING,  RefdataValueData.VOCABULARY_FEATURE_FLAG, null, null, true);

        } catch (Exception e) {
            log.error('Exception thrown while loading settings', e);
        }
    }
}
