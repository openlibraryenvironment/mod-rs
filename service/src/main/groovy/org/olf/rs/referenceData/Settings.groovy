package org.olf.rs.referenceData;

import org.olf.rs.statemodel.AvailableAction

import com.k_int.web.toolkit.custprops.CustomPropertyDefinition;
import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition;
import com.k_int.web.toolkit.refdata.RefdataCategory;
import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.settings.AppSetting

import groovy.util.logging.Slf4j

@Slf4j
public class Settings {

	private static String SETTING_TYPE_PASSWORD = "Password";
	private static String SETTING_TYPE_REF_DATA = "Refdata";
	private static String SETTING_TYPE_STRING   = "String";
	private static String SETTING_TYPE_TEMPLATE = "Template";

	private static String SECTION_AUTO_RESPONDER       = "autoResponder";
	private static String SECTION_CHAT                 = "chat";
	private static String SECTION_HOST_LMS_INTEGRATION = "hostLMSIntegration";
	private static String SECTION_LOCAL_NCIP           = "localNCIP";
	private static String SECTION_PATRON_STORE         = "patronStore";
	private static String SECTION_PULLSLIP_TEMPLATE    = "pullslipTemplateConfig";
	private static String SECTION_REQUESTS             = "requests";
	private static String SECTION_ROUTING              = "Routing";
	private static String SECTION_SHARED_INDEX         = "sharedIndex";
	private static String SECTION_WMS                  = "wmsSettings"; 	
	private static String SECTION_Z3950                = "z3950"; 	

	public static String VOCABULARY_ACCEPT_ITEM_METHOD           = "AcceptItemMethod";
	public static String VOCABULARY_AUTO_RESPONDER               = "AutoResponder";
	public static String VOCABULARY_AUTO_RESPONDER_CANCEL        = "AutoResponder_Cancel";
	public static String VOCABULARY_AUTO_RESPONDER_LOCAL         = "AutoResponder_Local";
	public static String VOCABULARY_BORROWER_CHECK_METHOD        = "BorrowerCheckMethod";
	public static String VOCABULARY_CANCELLATION_REASONS         = "cancellationReasons";
	public static String VOCABULARY_CANNOT_SUPPLY_REASONS        = "cannotSupplyReasons";
	public static String VOCABULARY_CHAT_AUTO_READ               = "ChatAutoRead";
	public static String VOCABULARY_CHECK_IN_METHOD              = "CheckInMethod";
	public static String VOCABULARY_CHECK_OUT_METHOD             = "CheckOutMethod";
	public static String VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER = "HostLMSIntegrationAdapter";
	public static String VOCABULARY_LOAN_CONDITIONS              = "loanConditions";
	public static String VOCABULARY_LOAN_POLICY                  = "LoanPolicy";
	public static String VOCABULARY_NCIP_DUE_DATE                = "NCIPDueDate";
	public static String VOCABULARY_NOTICE_FORMATS               = "noticeFormats";
	public static String VOCABULARY_NOTICE_TRIGGERS              = "noticeTriggers";
	public static String VOCABULARY_PATRON_STORE_ADAPTER         = "PatronStoreAdapter";
	public static String VOCABULARY_PULL_SLIP_TEMPLATE           = "pullslipTemplate";
	public static String VOCABULARY_REQUEST_ROUTING_ADAPTER      = "RequestRoutingAdapter";
	public static String VOCABULARY_SHARED_INDEX_ADAPTER         = "SharedIndexAdapter";
	public static String VOCABULARY_YES_NO                       = "YesNo";
	public static String VOCABULARY_YES_NO_OTHER                 = "YNO";

	// Settings for the z3950 section
	public static String SETTING_Z3950_SERVER_ADDRESS = "z3950_server_address";
	
	// Settings for the localNCIP section
	public static String SETTING_NCIP_APP_PROFILE    = "ncip_app_profile";
	public static String SETTING_NCIP_FROM_AGENCY    = "ncip_from_agency";
	public static String SETTING_NCIP_SERVER_ADDRESS = "ncip_server_address";
	public static String SETTING_NCIP_TO_AGENCY      = "ncip_to_agency";
	public static String SETTING_NCIP_USE_DUE_DATE   = "ncip_use_due_date";

	// Settings for the wmsSettings section
	public static String SETTING_WMS_API_KEY                = "wms_api_key";
	public static String SETTING_WMS_API_SECRET             = "wms_api_secret";
	public static String SETTING_WMS_CONNECTOR_ADDRESS      = "wms_connector_address";
	public static String SETTING_WMS_CONNECTOR_PASSWORD     = "wms_connector_password";
	public static String SETTING_WMS_CONNECTOR_USERNAME     = "wms_connector_username";
	public static String SETTING_WMS_LOOKUP_PATRON_ENDPOINT = "wms_lookup_patron_endpoint";
	public static String SETTING_WMS_REGISTRY_ID            = "wms_registry_id";
		  
	// Settings for the pullslipTemplateConfig section
	public static String SETTING_PULL_SLIP_TEMPLATE_ID = "pull_slip_template_id";
		  
	// Settings for the hostLMSIntegration section
	public static String SETTING_ACCEPT_ITEM          = "accept_item";
	public static String SETTING_BORROWER_CHECK       = "borrower_check";
	public static String SETTING_CHECK_IN_ITEM        = "check_in_item";
	public static String SETTING_CHECK_OUT_ITEM       = "check_out_item";
	public static String SETTING_HOST_LMS_INTEGRATION = "host_lms_integration";

	// Settings for the requests section
	public static String SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID = "default_institutional_patron_id";
	public static String SETTING_DEFAULT_REQUEST_SYMBOL          = "default_request_symbol";
	public static String SETTING_LAST_RESORT_LENDERS             = "last_resort_lenders";
	public static String SETTING_REQUEST_ID_PREFIX               = "request_id_prefix";
			
	// Settings for the sharedIndex section
	public static String SETTING_SHARED_INDEX_BASE_URL    = "shared_index_base_url";
	public static String SETTING_SHARED_INDEX_INTEGRATION = "shared_index_integration";
	public static String SETTING_SHARED_INDEX_PASS        = "shared_index_pass";
	public static String SETTING_SHARED_INDEX_TENANT      = "shared_index_tenant";
	public static String SETTING_SHARED_INDEX_USER        = "shared_index_user";
		  
	// Settings for the patronStore section
	public static String SETTING_PATRON_STORE          = "patron_store";
	public static String SETTING_PATRON_STORE_BASE_URL = "patron_store_base_url";
	public static String SETTING_PATRON_STORE_GROUP    = "patron_store_group";
	public static String SETTING_PATRON_STORE_PASS     = "patron_store_pass";
	public static String SETTING_PATRON_STORE_TENANT   = "patron_store_tenant";
	public static String SETTING_PATRON_STORE_USER     = "patron_store_user";
		  
	// Settings for the autoResponder section
	public static String SETTING_AUTO_RESPONDER_CANCEL           = "auto_responder_cancel";
	public static String SETTING_AUTO_RESPONDER_LOCAL            = "auto_responder_local";
	public static String SETTING_AUTO_RESPONDER_STATUS           = "auto_responder_status";
	public static String SETTING_STALE_REQUEST_2_DAYS            = "stale_request_2_days";
	public static String SETTING_STALE_REQUEST_1_ENABLED         = "stale_request_1_enabled";
	public static String SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND = "stale_request_3_exclude_weekend";
			
	// Settings for the chat section
	public static String SETTING_CHAT_AUTO_READ = "chat_auto_read";
		  
	// Settings for the Routing section
	public static String SETTING_ROUTING_ADAPTER = "routing_adapter";
	public static String SETTING_STATIC_ROUTES   = "static_routes";
	
	/**
	 * Ensures a CustomPropertyDefinition exists in the database
	 * @param name the name of the property
	 * @param local is this local or not
	 * @param category the category the property belongs to
	 * @param label the label associated with this property (default: null)
	 * @return the CustomPropertyDefinition
	 */
	public CustomPropertyDefinition ensureRefdataProperty(String name, boolean local, String category, String label = null) {
		
		CustomPropertyDefinition result = null;
		def rdc = RefdataCategory.findByDesc(category);
		
		if ( rdc != null ) {
			result = CustomPropertyDefinition.findByName(name)
			if ( result == null ) {
				result = new CustomPropertyRefdataDefinition(
					name:name,
					defaultInternal: local,
					label:label,
					category: rdc
				);
				// Not entirely sure why type can't be set in the above, but other bootstrap scripts do this
				// the same way, so copying. Type doesn't work when set as a part of the definition above
				result.type=com.k_int.web.toolkit.custprops.types.CustomPropertyRefdata.class
				result.save(flush:true, failOnError:true);
			}
		} else {
			println("Unable to find category ${category}");
		}
		return result;
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
			log.info("Adding settings to the database");
			ensureAppSetting(SETTING_Z3950_SERVER_ADDRESS, SECTION_Z3950, SETTING_TYPE_STRING);

			ensureAppSetting(SETTING_NCIP_SERVER_ADDRESS, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_NCIP_FROM_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, "");
			ensureAppSetting(SETTING_NCIP_TO_AGENCY, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, "");
			ensureAppSetting(SETTING_NCIP_APP_PROFILE, SECTION_LOCAL_NCIP, SETTING_TYPE_STRING, null, "EZBORROW");
		  
			RefdataValue use_due_on = RefdataValue.lookupOrCreate(VOCABULARY_NCIP_DUE_DATE, "On");
			RefdataValue.lookupOrCreate(VOCABULARY_NCIP_DUE_DATE, "Off");
			ensureAppSetting(SETTING_NCIP_USE_DUE_DATE, SECTION_LOCAL_NCIP, SETTING_TYPE_REF_DATA, VOCABULARY_NCIP_DUE_DATE, null, use_due_on?.value);

			ensureAppSetting(SETTING_WMS_API_KEY, SECTION_WMS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_WMS_API_SECRET, SECTION_WMS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_WMS_LOOKUP_PATRON_ENDPOINT, SECTION_WMS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_WMS_REGISTRY_ID, SECTION_WMS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_WMS_CONNECTOR_ADDRESS, SECTION_WMS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_WMS_CONNECTOR_USERNAME, SECTION_WMS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_WMS_CONNECTOR_PASSWORD, SECTION_WMS, SETTING_TYPE_STRING);
		  
			ensureAppSetting(SETTING_PULL_SLIP_TEMPLATE_ID, SECTION_PULLSLIP_TEMPLATE, SETTING_TYPE_TEMPLATE, VOCABULARY_PULL_SLIP_TEMPLATE);
		  
			// External LMS call methods -- none represents no integration and we will spoof a passing response instead
			RefdataValue.lookupOrCreate(VOCABULARY_BORROWER_CHECK_METHOD, "None");
			RefdataValue.lookupOrCreate(VOCABULARY_BORROWER_CHECK_METHOD, "NCIP");
		  
			ensureAppSetting(SETTING_BORROWER_CHECK, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_BORROWER_CHECK_METHOD);
		  
			RefdataValue.lookupOrCreate(VOCABULARY_CHECK_OUT_METHOD, "None");
			RefdataValue.lookupOrCreate(VOCABULARY_CHECK_OUT_METHOD, "NCIP");

			ensureAppSetting(SETTING_CHECK_OUT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_CHECK_OUT_METHOD);
		  
			RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_METHOD, "None");
			RefdataValue.lookupOrCreate(VOCABULARY_CHECK_IN_METHOD, "NCIP");
			
			ensureAppSetting(SETTING_CHECK_IN_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_CHECK_IN_METHOD);
		  
			RefdataValue.lookupOrCreate(VOCABULARY_ACCEPT_ITEM_METHOD, "None");
			RefdataValue.lookupOrCreate(VOCABULARY_ACCEPT_ITEM_METHOD, "NCIP");
			
			ensureAppSetting(SETTING_ACCEPT_ITEM, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_ACCEPT_ITEM_METHOD);

			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "ALMA");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Aleph");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "FOLIO");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Koha");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Millennium");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Sierra");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Symphony");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Voyager");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "WMS");
			RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "TLC");
			RefdataValue manual_adapter_rdv = RefdataValue.lookupOrCreate(VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, "Manual");
		  
			ensureAppSetting(SETTING_HOST_LMS_INTEGRATION, SECTION_HOST_LMS_INTEGRATION, SETTING_TYPE_REF_DATA, VOCABULARY_HOST_LMS_INTEGRATION_ADAPTER, null, manual_adapter_rdv.value);

			ensureAppSetting(SETTING_REQUEST_ID_PREFIX, SECTION_REQUESTS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_DEFAULT_REQUEST_SYMBOL, SECTION_REQUESTS, SETTING_TYPE_STRING);
			ensureAppSetting(SETTING_LAST_RESORT_LENDERS, SECTION_REQUESTS, SETTING_TYPE_STRING, null, "");
			ensureAppSetting(SETTING_DEFAULT_INSTITUTIONAL_PATRON_ID, SECTION_REQUESTS, SETTING_TYPE_STRING);
			
			RefdataValue folio_si_rdv = RefdataValue.lookupOrCreate(VOCABULARY_SHARED_INDEX_ADAPTER, "FOLIO");
		  
			ensureAppSetting(SETTING_SHARED_INDEX_INTEGRATION, SECTION_SHARED_INDEX, SETTING_TYPE_REF_DATA, VOCABULARY_SHARED_INDEX_ADAPTER, null, folio_si_rdv.value);
			ensureAppSetting(SETTING_SHARED_INDEX_BASE_URL, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, "http://shared-index.reshare-dev.indexdata.com:9130");
			ensureAppSetting(SETTING_SHARED_INDEX_USER, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, "diku_admin");
			ensureAppSetting(SETTING_SHARED_INDEX_PASS, SECTION_SHARED_INDEX, SETTING_TYPE_PASSWORD, null, "");
			ensureAppSetting(SETTING_SHARED_INDEX_TENANT, SECTION_SHARED_INDEX, SETTING_TYPE_STRING, null, "diku");
		  
			ensureAppSetting(SETTING_PATRON_STORE_BASE_URL, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, "http://127.0.0.1:9130");
			ensureAppSetting(SETTING_PATRON_STORE_TENANT, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, "diku");
			ensureAppSetting(SETTING_PATRON_STORE_USER, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, "diku_admin");
			ensureAppSetting(SETTING_PATRON_STORE_PASS, SECTION_PATRON_STORE, SETTING_TYPE_PASSWORD, null, "");
			ensureAppSetting(SETTING_PATRON_STORE_GROUP, SECTION_PATRON_STORE, SETTING_TYPE_STRING, null, "bdc2b6d4-5ceb-4a12-ab46-249b9a68473e");
										  
			RefdataValue.lookupOrCreate(VOCABULARY_PATRON_STORE_ADAPTER, "FOLIO");
			RefdataValue manual_patronstore_adapter_rdv = RefdataValue.lookupOrCreate(VOCABULARY_PATRON_STORE_ADAPTER, "Manual"); //Fallback
			
			ensureAppSetting(SETTING_PATRON_STORE, SECTION_PATRON_STORE, SETTING_TYPE_REF_DATA, VOCABULARY_PATRON_STORE_ADAPTER, null, manual_patronstore_adapter_rdv.value);
		  
			RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, "Off");
		  
			// Auto responder is on when an item can be found - will respond Will-Supply, when not found, left for a user to respond.
			RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, "On: will supply only");
		  
			// AutoResponder is ON and will automatically reply not-available if an item cannot be located
			RefdataValue ar_on = RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER, "On: will supply and cannot supply");
		  
			ensureAppSetting(SETTING_AUTO_RESPONDER_STATUS, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, VOCABULARY_AUTO_RESPONDER, null, ar_on?.value);
		  
			RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_CANCEL, "Off");
			def arc_on = RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_CANCEL, "On");
			
			ensureAppSetting(SETTING_AUTO_RESPONDER_CANCEL, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, VOCABULARY_AUTO_RESPONDER_CANCEL, null, arc_on?.value);
		  
			RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_LOCAL, "On");
			RefdataValue arl_off = RefdataValue.lookupOrCreate(VOCABULARY_AUTO_RESPONDER_LOCAL, "Off");

			ensureAppSetting(SETTING_AUTO_RESPONDER_LOCAL, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, VOCABULARY_AUTO_RESPONDER_LOCAL, null, arl_off?.value);

			RefdataValue yesNo_yes = RefdataValue.lookupOrCreate(VOCABULARY_YES_NO, "Yes");
			RefdataValue yesNo_no = RefdataValue.lookupOrCreate(VOCABULARY_YES_NO, "No");

			// Setup the Stale request settings (added the numbers so they appear in the order I want them in
			ensureAppSetting(SETTING_STALE_REQUEST_1_ENABLED, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, VOCABULARY_YES_NO, null, yesNo_no.value);
			ensureAppSetting(SETTING_STALE_REQUEST_2_DAYS, SECTION_AUTO_RESPONDER, SETTING_TYPE_STRING, null, "3");
			ensureAppSetting(SETTING_STALE_REQUEST_3_EXCLUDE_WEEKEND, SECTION_AUTO_RESPONDER, SETTING_TYPE_REF_DATA, VOCABULARY_YES_NO, null, yesNo_yes.value);
			
			RefdataValue.lookupOrCreate(VOCABULARY_YES_NO_OTHER, "Yes")
			RefdataValue.lookupOrCreate(VOCABULARY_YES_NO_OTHER, "No")
			RefdataValue.lookupOrCreate(VOCABULARY_YES_NO_OTHER, "Other")

		  	RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, "No longer available", "unavailable");
			RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, "Missing", "missing");
			RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, "Incorrect", "incorrect");
			RefdataValue.lookupOrCreate(VOCABULARY_CANNOT_SUPPLY_REASONS, "Other", "other");
		  
			RefdataValue.lookupOrCreate(VOCABULARY_CANCELLATION_REASONS, "Requested item is locally available", "available_locally");
			RefdataValue.lookupOrCreate(VOCABULARY_CANCELLATION_REASONS, "User account is invalid", "invalid_user");
			RefdataValue.lookupOrCreate(VOCABULARY_CANCELLATION_REASONS, "User requested cancellation", "patron_requested");
		  
			RefdataValue.lookupOrCreate(VOCABULARY_CHAT_AUTO_READ, "Off");
			RefdataValue.lookupOrCreate(VOCABULARY_CHAT_AUTO_READ, "On");
			RefdataValue.lookupOrCreate(VOCABULARY_CHAT_AUTO_READ, "On (excluding action messages)");
		  
			ensureAppSetting(SETTING_CHAT_AUTO_READ, SECTION_CHAT, SETTING_TYPE_REF_DATA, VOCABULARY_CHAT_AUTO_READ, "on");
		  
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, "LibraryUseOnly");
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, "NoReproduction");
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, "SignatureRequired");
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, "SpecCollSupervReq");
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, "WatchLibraryUseOnly");
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_CONDITIONS, "Other");
		  
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, "Lending all types")
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, "Not Lending")
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, "Lendin Physical only")
			RefdataValue.lookupOrCreate(VOCABULARY_LOAN_POLICY, "Lending Electronic only")
		  
			RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_FORMATS, "E-mail", "email");
			
			RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, "New request");
			RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, "End of rota");
			RefdataValue.lookupOrCreate(VOCABULARY_NOTICE_TRIGGERS, "Request cancelled");
		  
			ensureRefdataProperty("policy.ill.returns", false, "YNO", "Accept Returns" );
			ensureRefdataProperty("policy.ill.loan_policy", false, "LoanPolicy", "ILL Loan Policy" );
			ensureRefdataProperty("policy.ill.last_resort", false, "YNO", "Consider Institution As Last Resort" );
		  
			RefdataValue folio_si_routing_adapter = RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, "FOLIOSharedIndex");
			RefdataValue static_routing_adapter = RefdataValue.lookupOrCreate(VOCABULARY_REQUEST_ROUTING_ADAPTER, "Static");
		  
			ensureAppSetting(SETTING_ROUTING_ADAPTER, SECTION_ROUTING, SETTING_TYPE_REF_DATA, VOCABULARY_REQUEST_ROUTING_ADAPTER, null, folio_si_routing_adapter.value);
			ensureAppSetting(SETTING_STATIC_ROUTES, SECTION_ROUTING, SETTING_TYPE_STRING, null, null, "");
		  
			// To delete an unwanted action add State Model, State, Action to this array
			[
				[ "PatronRequest", "REQ_LOCAL_REVIEW", "requesterCancel" ],
				[ "PatronRequest", "REQ_LOCAL_REVIEW", "supplierCannotSupply" ]
			].each { action_to_remove ->
				log.info("Remove available action ${action_to_remove}");
				try {
					AvailableAction.executeUpdate("""delete from AvailableAction
													 where id in ( select aa.id from AvailableAction as aa where aa.actionCode=:code and aa.fromState.code=:fs and aa.model.shortcode=:sm)""",
												  [code:action_to_remove[2],fs:action_to_remove[1],sm:action_to_remove[0]]);
				} catch ( Exception e ) {
					log.error("Unable to delete action ${action_to_remove} - ${e.message}", e);
				}
			}
			
			// This looks slightly odd, but rather than litter this file with an ever growing list of
			// random delete statements, if you wish to delete
			// deprecated refdata values, add a new line to the array here consisting of [ "VALUE", "CATEGORY" ]
			[
				[ "sirsi", "HostLMSIntegrationAdapter" ]
			].each { r ->
				log.warn("Remove refdata value : ${r}");
				try {
					RefdataValue.executeUpdate("delete from RefdataValue where id in ( select rdv.id from RefdataValue as rdv where rdv.value = :v and rdv.owner.desc = :d)",
										   	   [v:RefdataValue.normValue(r[0]),d:r[1]]);
				} catch ( Exception e ) {
					log.error("Unable to delete refdata ${r} - ${e.message}", e);
				}
			}
		} catch ( Exception e ) {
			log.error("Exception thrown while loading settings", e);
		}
	}
	
	public static void loadAll() {
		(new Settings()).load();
	}
}
