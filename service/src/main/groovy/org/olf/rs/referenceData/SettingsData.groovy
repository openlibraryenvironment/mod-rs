package org.olf.rs.referenceData;

import org.olf.rs.ReferenceDataService;
import org.olf.rs.ReshareActionService;
import org.olf.rs.statemodel.StateModel;

import com.k_int.web.toolkit.files.FileUploadService;
import com.k_int.web.toolkit.settings.AppSetting;

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
    private static final String SECTION_NETWORK                = 'network';
    private static final String SECTION_PATRON_STORE           = 'patronStore';
    private static final String SECTION_PULLSLIP_CONFIGURATION = 'pullslipConfiguration';
    private static final String SECTION_PULLSLIP_TEMPLATE      = 'pullslipTemplateConfig';
    private static final String SECTION_REQUESTS               = 'requests';
    private static final String SECTION_ROUTING                = 'Routing';
    private static final String SECTION_SHARED_INDEX           = 'sharedIndex';
    private static final String SECTION_STATE_ACTION_CONFIG    = 'state_action_config';
    private static final String SECTION_STATE_MODEL            = 'state_model';
    private static final String SECTION_WMS                    = 'wmsSettings';
    private static final String SECTION_VOYAGER                = 'voyagerSettings';
    private static final String SECTION_Z3950                  = 'z3950';

    // Settings for the z3950 section
    public static final String SETTING_Z3950_SERVER_ADDRESS = 'z3950_server_address';
    public static final String SETTING_Z3950_PROXY_ADDRESS = 'z3950_proxy_address';

    // Settings for the localNCIP section
    public static final String SETTING_NCIP_APP_PROFILE     = 'ncip_app_profile';
    public static final String SETTING_NCIP_FROM_AGENCY     = 'ncip_from_agency';
    public static final String SETTING_NCIP_SERVER_ADDRESS  = 'ncip_server_address';
    public static final String SETTING_NCIP_TO_AGENCY       = 'ncip_to_agency';
    public static final String SETTING_NCIP_USE_DUE_DATE    = 'ncip_use_due_date';
    public static final String SETTING_NCIP_DUE_DATE_FORMAT = 'ncip_due_date_format';

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

    // Settings for the requests section
    public static final String SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID = 'default_institutional_patron_id';
    public static final String SETTING_DEFAULT_REQUEST_SYMBOL          = 'default_request_symbol';
    public static final String SETTING_LAST_RESORT_LENDERS             = 'last_resort_lenders';
    public static final String SETTING_REQUEST_ID_PREFIX               = 'request_id_prefix';

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
    public static final String SETTING_AUTO_RESPONDER_CANCEL           = 'auto_responder_cancel';
    public static final String SETTING_AUTO_RESPONDER_LOCAL            = 'auto_responder_local';
    public static final String SETTING_AUTO_RESPONDER_STATUS           = 'auto_responder_status';
    public static final String SETTING_STALE_REQUEST_2_DAYS            = 'stale_request_2_days';
    public static final String SETTING_STALE_REQUEST_1_ENABLED         = 'stale_request_1_enabled';
    public static final String SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND = 'stale_request_3_exclude_weekend';

    // Settings for the chat section
    public static final String SETTING_CHAT_AUTO_READ = 'chat_auto_read';

    // Settings for the Routing section
    public static final String SETTING_ROUTING_ADAPTER = 'routing_adapter';
    public static final String SETTING_STATIC_ROUTES   = 'static_routes';

    // State/Action configuration settings
    public static final String SETTING_COMBINE_FILL_AND_SHIP                      = 'combine_fill_and_ship';
    public static final String SETTING_COMBINE_RETURNED_BY_PATRON_AND_RETURN_SHIP = 'combine_returned_by_patron_and_return_ship';

    // Network configuration settings
    public static final String SETTING_NETWORK_MAXIMUM_SEND_ATEMPTS = 'network_maximum_send_attempts';
    public static final String SETTING_NETWORK_RETRY_PERIOD         = 'network_retry_period';
    public static final String SETTING_NETWORK_TIMEOUT_PERIOD       = 'network_timeout_period';

    // State model configuration settings
    public static final String SETTING_STATE_MODEL_REQUESTER     = 'state_model_requester';
    public static final String SETTING_STATE_MODEL_REQUESTER_CDL = 'state_model_requester_cdl';
    public static final String SETTING_STATE_MODEL_RESPONDER     = 'state_model_responder';
    public static final String SETTING_STATE_MODEL_RESPONDER_CDL = 'state_model_responder_cdl';

    public static final String SETTING_FILE_STORAGE_ENGINE           = 'storageEngine';
    public static final String SETTING_FILE_STORAGE_S3_ENDPOINT      = 'S3Endpoint';
    public static final String SETTING_FILE_STORAGE_S3_ACCESS_KEY    = 'S3AccessKey';
    public static final String SETTING_FILE_STORAGE_S3_SECRET_KEY    = 'S3SecretKey';
    public static final String SETTING_FILE_STORAGE_S3_BUCKET_NAME   = 'S3BucketName';
    public static final String SETTING_FILE_STORAGE_S3_BUCKET_REGION = 'S3BucketRegion';
    public static final String SETTING_FILE_STORAGE_S3_OBJECT_PREFIX = 'S3ObjectPrefix';

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
     * Loads the settings into the database
     */
    public void load() {
        try {
            log.info('Adding settings to the database');
            // We are not a service, so we need to look it up
            ReferenceDataService referenceDataService = ReferenceDataService.getInstance();

            ensureAppSetting(SETTING_Z3950_SERVER_ADDRESS, SECTION_Z3950, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_Z3950_PROXY_ADDRESS, SECTION_Z3950, SETTING_TYPE_STRING, null, 'http://reshare-mp.folio-dev.indexdata.com:9000');

            ensureAppSetting(SETTING_NCIP_SERVER_ADDRESS, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_NCIP_FROM_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_TO_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_APP_PROFILE, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, 'EZBORROW');
            ensureAppSetting(SETTING_NCIP_USE_DUE_DATE, SECTION_LOCAL_NCIP, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_NCIP_DUE_DATE, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_NCIP_DUE_DATE, RefdataValueData.NCIP_DUE_DATE_ON).value);
            ensureAppSetting(SETTING_NCIP_DUE_DATE_FORMAT, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, ReshareActionService.DEFAULT_DATE_FORMAT);

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
            ensureAppSetting(SETTING_PULL_SLIP_MAX_ITEMS, SECTION_PULLSLIP_CONFIGURATION, SETTING_TYPE_STRING, null, "100", null, true);
            ensureAppSetting(SETTING_PULL_SLIP_MAX_ITEMS_MANUAL, SECTION_PULLSLIP_CONFIGURATION, SETTING_TYPE_STRING, null, "100", null, true);

            ensureAppSetting(SETTING_BORROWER_CHECK, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_BORROWER_CHECK_METHOD);
            ensureAppSetting(SETTING_CHECK_OUT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_OUT_METHOD);
            ensureAppSetting(SETTING_CHECK_IN_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_IN_METHOD);
            ensureAppSetting(SETTING_CHECK_IN_ON_RETURN, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_IN_ON_RETURN, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_CHECK_IN_ON_RETURN, RefdataValueData.CHECK_IN_ON_RETURN_OFF).value);
            ensureAppSetting(SETTING_ACCEPT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_ACCEPT_ITEM_METHOD);
            ensureAppSetting(SETTING_HOST_LMS_INTEGRATION, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, RefdataValueData.HOST_LMS_INTEGRATION_ADAPTER_MANUAL).value);

            ensureAppSetting(SETTING_REQUEST_ID_PREFIX, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_DEFAULT_REQUEST_SYMBOL, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_LAST_RESORT_LENDERS, SECTION_REQUESTS, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID, SECTION_REQUESTS, SETTING_TYPE_STRING);

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

            // Setup the Stale request settings (added the numbers so they appear in the order I want them in
            ensureAppSetting(SETTING_STALE_REQUEST_1_ENABLED, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_STALE_REQUEST_2_DAYS, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null, '3');
            ensureAppSetting(SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_YES).value);

            ensureAppSetting(SETTING_CHAT_AUTO_READ, SECTION_CHAT, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHAT_AUTO_READ, 'on');

            ensureAppSetting(SETTING_ROUTING_ADAPTER, SECTION_ROUTING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_REQUEST_ROUTING_ADAPTER, null, referenceDataService.lookup(RefdataValueData.VOCABULARY_REQUEST_ROUTING_ADAPTER, RefdataValueData.REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX).value);
            ensureAppSetting(SETTING_STATIC_ROUTES, SECTION_ROUTING, SETTING_TYPE_STRING, null, null, '');

            ensureAppSetting(SETTING_COMBINE_FILL_AND_SHIP, SECTION_STATE_ACTION_CONFIG, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);
            ensureAppSetting(SETTING_COMBINE_RETURNED_BY_PATRON_AND_RETURN_SHIP, SECTION_STATE_ACTION_CONFIG, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, referenceDataService.lookup(RefdataValueData.VOCABULARY_YES_NO, RefdataValueData.YES_NO_NO).value);

            ensureAppSetting(SETTING_NETWORK_MAXIMUM_SEND_ATEMPTS, SECTION_NETWORK, SETTING_TYPE_STRING, null, '0');
            ensureAppSetting(SETTING_NETWORK_RETRY_PERIOD, SECTION_NETWORK, SETTING_TYPE_STRING, null, '10');
            ensureAppSetting(SETTING_NETWORK_TIMEOUT_PERIOD, SECTION_NETWORK, SETTING_TYPE_STRING, null, '30');

            ensureAppSetting(SETTING_NETWORK_TIMEOUT_PERIOD, SECTION_NETWORK, SETTING_TYPE_STRING, null, '30');
            ensureAppSetting(SETTING_NETWORK_TIMEOUT_PERIOD, SECTION_NETWORK, SETTING_TYPE_STRING, null, '30');

            ensureAppSetting(SETTING_STATE_MODEL_REQUESTER, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_REQUESTER, null, true);
            ensureAppSetting(SETTING_STATE_MODEL_RESPONDER, SECTION_STATE_MODEL, SETTING_TYPE_STRING, null, StateModel.MODEL_RESPONDER, null, true);

            ensureAppSetting(SETTING_FILE_STORAGE_ENGINE, SECTION_FILE_STORAGE, SETTING_TYPE_STRING, null, FileUploadService.S3_STORAGE_ENGINE);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_ENDPOINT, SECTION_FILE_STORAGE, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_ACCESS_KEY, SECTION_FILE_STORAGE, SETTING_TYPE_PASSWORD);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_SECRET_KEY, SECTION_FILE_STORAGE, SETTING_TYPE_PASSWORD);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_BUCKET_NAME, SECTION_FILE_STORAGE, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_BUCKET_REGION, SECTION_FILE_STORAGE, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_FILE_STORAGE_S3_OBJECT_PREFIX, SECTION_FILE_STORAGE, SETTING_TYPE_STRING, null, 'reshare-');

        } catch (Exception e) {
            log.error('Exception thrown while loading settings', e);
        }
    }
}
