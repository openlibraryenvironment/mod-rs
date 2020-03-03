import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;


// When adding new section names into this file please make sure they are in camel case.
CustomPropertyDefinition ensureTextProperty(String name, boolean local = true, String label = null) {
  CustomPropertyDefinition result = CustomPropertyDefinition.findByName(name) ?: new CustomPropertyDefinition(
                                        name:name,
                                        type:com.k_int.web.toolkit.custprops.types.CustomPropertyText.class,
                                        defaultInternal: local,
                                        label:label
                                      ).save(flush:true, failOnError:true);
  return result;
}


try {
  AppSetting z3950_address = AppSetting.findByKey('z3950_server_address') ?: new AppSetting( 
                                  section:'z3950',
                                  settingType:'String',
                                  key: 'z3950_server_address',
                                  ).save(flush:true, failOnError: true);

  AppSetting ncip_address = AppSetting.findByKey('ncip_server_address') ?: new AppSetting( 
                                  section:'localNCIP',
                                  settingType:'String',
                                  key: 'ncip_server_address'
                                  ).save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'None');
  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'NCIP2');

  AppSetting borrower_check = AppSetting.findByKey('borrower_check') ?: new AppSetting( 
                                  section:'requesterValidation',
                                  settingType:'Refdata',
                                  vocab:'BorrowerCheckMethod',
                                  key: 'borrower_check'
                                  ).save(flush:true, failOnError: true);

  AppSetting request_id_prefix = AppSetting.findByKey('request_id_prefix') ?: new AppSetting( 
                                  section:'requests',
                                  settingType:'String',
                                  key: 'request_id_prefix',
                                  ).save(flush:true, failOnError: true);

  AppSetting default_request_symbol = AppSetting.findByKey('default_request_symbol') ?: new AppSetting( 
                                  section:'requests',
                                  settingType:'String',
                                  key: 'default_request_symbol',
                                  ).save(flush:true, failOnError: true);

  AppSetting shared_index_base_url = AppSetting.findByKey('shared_index_base_url') ?: new AppSetting( 
                                  section:'sharedIndex',
                                  settingType:'String',
                                  key: 'shared_index_base_url',
                                  defValue: 'http://shared-index.reshare-dev.indexdata.com:9130'
                                  ).save(flush:true, failOnError: true);

  AppSetting shared_index_user = AppSetting.findByKey('shared_index_user') ?: new AppSetting( 
                                  section:'sharedIndex',
                                  settingType:'String',
                                  key: 'shared_index_user',
                                  defValue: 'diku_admin').save(flush:true, failOnError: true);

  AppSetting shared_index_pass = AppSetting.findByKey('shared_index_pass') ?: new AppSetting( 
                                  section:'sharedIndex',
                                  settingType:'Password',
                                  key: 'shared_index_pass',
                                  defValue: '').save(flush:true, failOnError: true);

  AppSetting shared_index_tenant = AppSetting.findByKey('shared_index_tenant') ?: new AppSetting( 
                                  section:'sharedIndex',
                                  settingType:'String',
                                  key: 'shared_index_tenant',
                                  defValue: 'diku').save(flush:true, failOnError: true);

  AppSetting last_resort_lenders = AppSetting.findByKey('last_resort_lenders') ?: new AppSetting( 
                                  section:'requests',
                                  settingType:'String',
                                  key: 'last_resort_lenders',
                                  defValue: '').save(flush:true, failOnError: true);

  AppSetting ncip_from_agency = AppSetting.findByKey('ncip_from_agency') ?: new AppSetting( 
                                  section:'localNCIP',
                                  settingType:'String',
                                  key: 'ncip_from_agency',
                                  defValue: '').save(flush:true, failOnError: true);

  AppSetting ncip_app_profile = AppSetting.findByKey('ncip_app_profile') ?: new AppSetting( 
                                  section:'localNCIP',
                                  settingType:'String',
                                  key: 'ncip_app_profile',
                                  defValue: 'EZBORROW').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'None');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'ALMA');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Aleph');

  AppSetting host_lms_integration = AppSetting.findByKey('host_lms_integration') ?: new AppSetting( 
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'HostLMSIntegrationAdapter',
                                  key: 'host_lms_integration').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('AutoResponder', 'Off');
  RefdataValue.lookupOrCreate('AutoResponder', 'On for found items');
  RefdataValue.lookupOrCreate('AutoResponder', 'On auto not-found');

  AppSetting auto_responder_status = AppSetting.findByKey('auto_responder_status') ?: new AppSetting( 
                                  section:'autoResponder',
                                  settingType:'Refdata',
                                  vocab:'AutoResponder',
                                  key: 'auto_responder_status').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'unavailable');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'missing');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'incorrect');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'other');

  AppSetting chat_auto_read = AppSetting.findByKey('chat_auto_read') ?: new AppSetting( 
                                  section:'chat',
                                  settingType:'Refdata',
                                  vocab:'ChatAutoRead',
                                  key: 'chat_auto_read').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('ChatAutoRead', 'Off');
  RefdataValue.lookupOrCreate('ChatAutoRead', 'On');
  RefdataValue.lookupOrCreate('ChatAutoRead', 'On (excluding action messages)');

  def cp_ns = ensureTextProperty('ILLPreferredNamespaces', false);
  def cp_url = ensureTextProperty('url', false);
  def cp_demoprop = ensureTextProperty('demoCustprop', false);
  def cp_test_prop = ensureTextProperty('TestParam', false);
  def cp_z3950_base_name = ensureTextProperty('Z3950BaseName', false);
  def cp_local_institutionalPatronId = ensureTextProperty('local_institutionalPatronId', true, label='Institutional patron ID');
  def cp_local_widget2 = ensureTextProperty('local_widget_2', true, label='Widget 2');
  def cp_local_widget3 = ensureTextProperty('local_widget_3', true, label='Widget 3');
  def cp_local_alma_agency = ensureTextProperty('ALMA_AGENCY_ID', true, label='ALMA Agency ID');
  println("_data.groovy complete");
}
catch ( Exception e ) {
  e.printStackTrace();
}


