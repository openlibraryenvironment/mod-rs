package org.olf.rs.referenceData;

import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.settings.AppSetting;

import groovy.util.logging.Slf4j;

/**
 * Class that contains the Settings and Reference Data values required by ReShare
 * @author Chas
 *
 */
@Slf4j
public class Settings {

    private static final String SETTING_TYPE_PASSWORD = 'Password';
    private static final String SETTING_TYPE_REF_DATA = 'Refdata';
    private static final String SETTING_TYPE_STRING   = 'String';
    private static final String SETTING_TYPE_TEMPLATE = 'Template';

    private static final String SECTION_AUTO_RESPONDER       = 'autoResponder';
    private static final String SECTION_CHAT                 = 'chat';
    private static final String SECTION_HOST_LMS_INTEGRATION = 'hostLMSIntegration';
    private static final String SECTION_LOCAL_NCIP           = 'localNCIP';
    private static final String SECTION_PATRON_STORE         = 'patronStore';
    private static final String SECTION_PULLSLIP_TEMPLATE    = 'pullslipTemplateConfig';
    private static final String SECTION_REQUESTS             = 'requests';
    private static final String SECTION_ROUTING              = 'Routing';
    private static final String SECTION_SHARED_INDEX         = 'sharedIndex';
    private static final String SECTION_STATE_ACTION_CONFIG  = 'state_action_config';
    private static final String SECTION_WMS                  = 'wmsSettings';
    private static final String SECTION_Z3950                = 'z3950';

    // Settings for the z3950 section
    public static final String SETTING_Z3950_SERVER_ADDRESS = 'z3950_server_address';

    // Settings for the localNCIP section
    public static final String SETTING_NCIP_APP_PROFILE    = 'ncip_app_profile';
    public static final String SETTING_NCIP_FROM_AGENCY    = 'ncip_from_agency';
    public static final String SETTING_NCIP_SERVER_ADDRESS = 'ncip_server_address';
    public static final String SETTING_NCIP_TO_AGENCY      = 'ncip_to_agency';
    public static final String SETTING_NCIP_USE_DUE_DATE   = 'ncip_use_due_date';

    // Settings for the wmsSettings section
    public static final String SETTING_WMS_API_KEY                = 'wms_api_key';
    public static final String SETTING_WMS_API_SECRET             = 'wms_api_secret';
    public static final String SETTING_WMS_CONNECTOR_ADDRESS      = 'wms_connector_address';
    public static final String SETTING_WMS_CONNECTOR_PASSWORD     = 'wms_connector_password';
    public static final String SETTING_WMS_CONNECTOR_USERNAME     = 'wms_connector_username';
    public static final String SETTING_WMS_LOOKUP_PATRON_ENDPOINT = 'wms_lookup_patron_endpoint';
    public static final String SETTING_WMS_REGISTRY_ID            = 'wms_registry_id';

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

    public static void loadAll() {
        (new Settings()).load();
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
    public AppSetting ensureAppSetting(String key, String section, String settingType, String vocabulary = null, String defaultValue = null, String value = null) {
        AppSetting result = AppSetting.findByKey(key);
        if (result == null) {
            result = new AppSetting(
                section: section,
                settingType: settingType,
                vocab: vocabulary,
                key: key,
                defValue: defaultValue,
                value: value
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
            ensureAppSetting(SETTING_Z3950_SERVER_ADDRESS, SECTION_Z3950, SETTING_TYPE_STRING);

            ensureAppSetting(SETTING_NCIP_SERVER_ADDRESS, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_NCIP_FROM_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_TO_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_NCIP_APP_PROFILE, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, 'EZBORROW');
            ensureAppSetting(SETTING_NCIP_USE_DUE_DATE, SECTION_LOCAL_NCIP, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_NCIP_DUE_DATE, null, RefdataValue.normValue(RefdataValueData.NCIP_DUE_DATE_ON));

            ensureAppSetting(SETTING_WMS_API_KEY, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_API_SECRET, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_LOOKUP_PATRON_ENDPOINT, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_REGISTRY_ID, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_CONNECTOR_ADDRESS, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_CONNECTOR_USERNAME, SECTION_WMS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_WMS_CONNECTOR_PASSWORD, SECTION_WMS, SETTING_TYPE_STRING);

            ensureAppSetting(SETTING_PULL_SLIP_TEMPLATE_ID, SECTION_PULLSLIP_TEMPLATE, SETTING_TYPE_TEMPLATE, RefdataValueData.VOCABULARY_PULL_SLIP_TEMPLATE);

            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_OUT_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_OUT_METHOD, 'NCIP');

            ensureAppSetting(SETTING_CHECK_OUT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_CHECK_OUT_METHOD);

            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_METHOD, 'NCIP');

            ensureAppSetting(SETTING_CHECK_IN_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_CHECK_IN_METHOD);

            RefdataValue checkInOnReturnOff = RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_ON_RETURN, 'Off');
            RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_ON_RETURN, 'On');

            ensureAppSetting(SETTING_CHECK_IN_ON_RETURN, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_CHECK_IN_ON_RETURN, null, checkInOnReturnOff?.value);

            RefdataValue.lookupOrCreate(VOCABULARY_ACCEPT_ITEM_METHOD, 'None');
            RefdataValue.lookupOrCreate(VOCABULARY_ACCEPT_ITEM_METHOD, 'NCIP');

            ensureAppSetting(SETTING_ACCEPT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_ACCEPT_ITEM_METHOD);

            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'ALMA');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Aleph');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'FOLIO');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Koha');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Millennium');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Sierra');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Symphony');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Voyager');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'WMS');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'TLC');
            RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Horizon');

            RefdataValue manualAdapterRDV = RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, 'Manual');

            ensureAppSetting(SETTING_HOST_LMS_INTEGRATION, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, null, manualAdapterRDV.value);
            ensureAppSetting(SETTING_BORROWER_CHECK, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_BORROWER_CHECK_METHOD);
            ensureAppSetting(SETTING_CHECK_OUT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_OUT_METHOD);
            ensureAppSetting(SETTING_CHECK_IN_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_IN_METHOD);
            ensureAppSetting(SETTING_CHECK_IN_ON_RETURN, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHECK_IN_ON_RETURN, null, RefdataValue.normValue(RefdataValueData.CHECK_IN_ON_RETURN_OFF));
            ensureAppSetting(SETTING_ACCEPT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_ACCEPT_ITEM_METHOD);
            ensureAppSetting(SETTING_HOST_LMS_INTEGRATION, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, null, RefdataValue.normValue(RefdataValueData.HOST_LMS_INTEGRATION_ADAPTER_MANUAL));

            ensureAppSetting(SETTING_REQUEST_ID_PREFIX, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_DEFAULT_REQUEST_SYMBOL, SECTION_REQUESTS, SETTING_TYPE_STRING);
            ensureAppSetting(SETTING_LAST_RESORT_LENDERS, SECTION_REQUESTS, SETTING_TYPE_STRING, null, '');
            ensureAppSetting(SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID, SECTION_REQUESTS, SETTING_TYPE_STRING);

            ensureAppSetting(SETTING_SHARED_INDEX_INTEGRATION, SECTION_SHARED_INDEX, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_SHARED_INDEX_ADAPTER, null, RefdataValue.normValue(RefdataValueData.SHARED_INDEX_ADAPTER_FOLIO));
            ensureAppSetting(SETTING_SHARED_INDEX_BASE_URL, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, 'http://shared-index.reshare-dev.indexdata.com:9130');
            ensureAppSetting(SETTING_SHARED_INDEX_USER, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, 'diku_admin');
            ensureAppSetting(SETTING_SHARED_INDEX_PASS, SECTION_SHARED_INDEX, SETTING_TYPE_PASSWORD, null, '');
            ensureAppSetting(SETTING_SHARED_INDEX_TENANT, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, 'diku');

            ensureAppSetting(SETTING_PATRON_STORE_BASE_URL, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'http://127.0.0.1:9130');
            ensureAppSetting(SETTING_PATRON_STORE_TENANT, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'diku');
            ensureAppSetting(SETTING_PATRON_STORE_USER, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'diku_admin');
            ensureAppSetting(SETTING_PATRON_STORE_PASS, SECTION_PATRON_STORE, SETTING_TYPE_PASSWORD, null, '');
            ensureAppSetting(SETTING_PATRON_STORE_GROUP, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, 'bdc2b6d4-5ceb-4a12-ab46-249b9a68473e');
            ensureAppSetting(SETTING_PATRON_STORE, SECTION_PATRON_STORE, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_PATRON_STORE_ADAPTER, null, RefdataValue.normValue(RefdataValueData.PATRON_STORE_ADAPTER_MANUAL));

            ensureAppSetting(SETTING_AUTO_RESPONDER_STATUS, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER, null, RefdataValue.normValue(RefdataValueData.AUTO_RESPONDER_ON_WILL_SUPPLY_CANNOT_SUPPLY));
            ensureAppSetting(SETTING_AUTO_RESPONDER_CANCEL, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_CANCEL, null, RefdataValue.normValue(RefdataValueData.AUTO_RESPONDER_CANCEL_ON));
            ensureAppSetting(SETTING_AUTO_RESPONDER_LOCAL, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_AUTO_RESPONDER_LOCAL, null, RefdataValue.normValue(RefdataValueData.AUTO_RESPONDER_LOCAL_OFF));

            // Setup the Stale request settings (added the numbers so they appear in the order I want them in
            ensureAppSetting(SETTING_STALE_REQUEST_1_ENABLED, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, RefdataValue.normValue(RefdataValueData.YES_NO_NO));
            ensureAppSetting(SETTING_STALE_REQUEST_2_DAYS, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null, '3');
            ensureAppSetting(SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, null, RefdataValue.normValue(RefdataValueData.YES_NO_YES));

            ensureAppSetting(SETTING_CHAT_AUTO_READ, SECTION_CHAT, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_CHAT_AUTO_READ, 'on');

            ensureAppSetting(SETTING_ROUTING_ADAPTER, SECTION_ROUTING, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_REQUEST_ROUTING_ADAPTER, null, RefdataValue.normValue(RefdataValueData.REQUEST_ROUTING_ADAPTER_FOLIO_SHARED_INDEX));
            ensureAppSetting(SETTING_STATIC_ROUTES, SECTION_ROUTING, SETTING_TYPE_STRING, null, null, '');

            ensureAppSetting(SETTING_COMBINE_FILL_AND_SHIP, SECTION_STATE_ACTION_CONFIG, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, RefdataValue.normValue(RefdataValueData.YES_NO_NO));
            ensureAppSetting(SETTING_COMBINE_RETURNED_BY_PATRON_AND_RETURN_SHIP, SECTION_STATE_ACTION_CONFIG, SETTING_TYPE_REF_DATA, RefdataValueData.VOCABULARY_YES_NO, RefdataValue.normValue(RefdataValueData.YES_NO_NO));

        } catch (Exception e) {
            log.error('Exception thrown while loading settings', e);
        }
    }
}
