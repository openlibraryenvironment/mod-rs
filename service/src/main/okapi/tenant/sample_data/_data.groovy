import org.olf.rs.HostLMSLocation
import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*

import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import org.olf.okapi.modules.directory.NamingAuthority;
import org.olf.rs.statemodel.*;
import com.k_int.web.toolkit.refdata.RefdataValue;

import org.olf.templating.*;

CustomPropertyDefinition ensureRefdataProperty(String name, boolean local, String category, String label = null) {

  CustomPropertyDefinition result = null;
  def rdc = RefdataCategory.findByDesc(category);

  if ( rdc != null ) {
    result = CustomPropertyDefinition.findByName(name)
    if ( result == null ) {
      result = new CustomPropertyRefdataDefinition(
                                        name:name,
                                        defaultInternal: local,
                                        label:label,
                                        category: rdc)
      // Not entirely sure why type can't be set in the above, but other bootstrap scripts do this
      // the same way, so copying. Type doesn't work when set as a part of the definition above
      result.type=com.k_int.web.toolkit.custprops.types.CustomPropertyRefdata.class
      result.save(flush:true, failOnError:true);
    }
  }
  else {
    println("Unable to find category ${category}");
  }
  return result;
}


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

  AppSetting wms_api_key = AppSetting.findByKey('wms_api_key') ?: new AppSetting( 
                                section:'wmsSettings',
                                settingType:'String',
                                key: 'wms_api_key'
                                ).save(flush:true, failOnError: true);

  AppSetting wms_api_secret = AppSetting.findByKey('wms_api_secret') ?: new AppSetting( 
                                section:'wmsSettings',
                                settingType:'Password',
                                key: 'wms_api_secret'
                                  ).save(flush:true, failOnError: true);

  AppSetting wms_lookup_patron_endpoint = AppSetting.findByKey('wms_lookup_patron_endpoint') ?: new AppSetting( 
                                section:'wmsSettings',
                                settingType:'String',
                                key: 'wms_lookup_patron_endpoint'
                                ).save(flush:true, failOnError: true);

  AppSetting wms_registry_id = AppSetting.findByKey('wms_registry_id') ?: new AppSetting( 
                              section:'wmsSettings',
                              settingType:'String',
                              key: 'wms_registry_id'
                              ).save(flush:true, failOnError: true);

  AppSetting wms_connector_address = AppSetting.findByKey('wms_connector_address') ?: new AppSetting( 
                              section:'wmsSettings',
                              settingType:'String',
                              key: 'wms_connector_address'
                              ).save(flush:true, failOnError: true);
  
  AppSetting wms_connector_username = AppSetting.findByKey('wms_connector_username') ?: new AppSetting( 
                              section:'wmsSettings',
                              settingType:'String',
                              key: 'wms_connector_username'
                              ).save(flush:true, failOnError: true);

  AppSetting wms_connector_password = AppSetting.findByKey('wms_connector_password') ?: new AppSetting( 
                              section:'wmsSettings',
                              settingType:'Password',
                              key: 'wms_connector_password'
                              ).save(flush:true, failOnError: true);

  AppSetting pull_slip_template_id = AppSetting.findByKey('pull_slip_template_id') ?: new AppSetting( 
    section:'pullslipTemplateConfig',
    settingType:'Template',
    vocab: 'pullslipTemplate',
    key: 'pull_slip_template_id'
  ).save(flush:true, failOnError: true);

  // External LMS call methods -- none represents no integration and we will spoof a passing response instead
  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'None');
  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'NCIP');

  AppSetting borrower_check = AppSetting.findByKey('borrower_check') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'BorrowerCheckMethod',
                                  key: 'borrower_check'
                                  ).save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('CheckOutMethod', 'None');
  RefdataValue.lookupOrCreate('CheckOutMethod', 'NCIP');
  
  AppSetting check_out_item = AppSetting.findByKey('check_out_item') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'CheckOutMethod',
                                  key: 'check_out_item').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('CheckInMethod', 'None');
  RefdataValue.lookupOrCreate('CheckInMethod', 'NCIP');
  
  AppSetting check_in_item = AppSetting.findByKey('check_in_item') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'CheckInMethod',
                                  key: 'check_in_item').save(flush:true, failOnError: true);


  RefdataValue.lookupOrCreate('AcceptItemMethod', 'None');
  RefdataValue.lookupOrCreate('AcceptItemMethod', 'NCIP');
  
  AppSetting accept_item = AppSetting.findByKey('accept_item') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'AcceptItemMethod',
                                  key: 'accept_item').save(flush:true, failOnError: true);

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

  def folio_si_rdv = RefdataValue.lookupOrCreate('SharedIndexAdapter', 'FOLIO');

  AppSetting shared_index_integration = AppSetting.findByKey('shared_index_integration') ?: new AppSetting(
                                  section:'sharedIndex',
                                  settingType:'Refdata',
                                  vocab:'SharedIndexAdapter',
                                  key: 'shared_index_integration',
                                  value: folio_si_rdv.value).save(flush:true, failOnError: true);
                                  
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

  AppSetting ncip_to_agency = AppSetting.findByKey('ncip_to_agency') ?: new AppSetting( 
                                  section:'localNCIP',
                                  settingType:'String',
                                  key: 'ncip_to_agency',
                                  defValue: '').save(flush:true, failOnError: true);

  AppSetting ncip_app_profile = AppSetting.findByKey('ncip_app_profile') ?: new AppSetting( 
                                  section:'localNCIP',
                                  settingType:'String',
                                  key: 'ncip_app_profile',
                                  defValue: 'EZBORROW').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'ALMA');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Aleph');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'FOLIO');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Koha');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Millennium');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Sierra');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Symphony');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Voyager');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'WMS');
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'TLC');
  def manual_adapter_rdv = RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Manual');

  AppSetting host_lms_integration = AppSetting.findByKey('host_lms_integration') ?: new AppSetting( 
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'HostLMSIntegrationAdapter',
                                  key: 'host_lms_integration',
                                  value: manual_adapter_rdv.value).save(flush:true, failOnError: true);
                                
  AppSetting patron_store_base_url = AppSetting.findByKey('patron_store_base_url') ?: new AppSetting( 
                                  section:'patronStore',
                                  settingType:'String',
                                  key: 'patron_store_base_url',
                                  defValue: 'http://127.0.0.1:9130'
                                  ).save(flush:true, failOnError: true);
  AppSetting patron_store_tenant = AppSetting.findByKey('patron_store_tenant') ?: new AppSetting( 
                                  section:'patronStore',
                                  settingType:'String',
                                  key: 'patron_store_tenant',
                                  defValue: 'diku').save(flush:true, failOnError: true);
  AppSetting patron_store_user = AppSetting.findByKey('patron_store_user') ?: new AppSetting( 
                                  section:'patronStore',
                                  settingType:'String',
                                  key: 'patron_store_user',
                                  defValue: 'diku_admin').save(flush:true, failOnError: true);
  AppSetting patron_store_pass = AppSetting.findByKey('patron_store_pass') ?: new AppSetting( 
                                  section:'patronStore',
                                  settingType:'Password',
                                  key: 'patron_store_pass',
                                  defValue: '').save(flush:true, failOnError: true);
  AppSetting patron_store_group = AppSetting.findByKey('patron_store_group') ?: new AppSetting(
                                  section:'patronStore',
                                  settingType: 'String',
                                  key: 'patron_store_group',
                                  defValue: 'bdc2b6d4-5ceb-4a12-ab46-249b9a68473e').save(flush:true, failOnError: true);
                                
  RefdataValue.lookupOrCreate("PatronStoreAdapter", "FOLIO");
  def manual_patronstore_adapter_rdv = RefdataValue.lookupOrCreate("PatronStoreAdapter", "Manual"); //Fallback
  
  AppSetting patron_store = AppSetting.findByKey("patron_store") ?: new AppSetting(
                                  section: 'patronStore',
                                  settingType: 'Refdata',
                                  vocab: 'PatronStoreAdapter',
                                  key: 'patron_store',
                                  value: manual_patronstore_adapter_rdv.value).save(flush:true, failOnError:true);
  
                                
  

  RefdataValue.lookupOrCreate('AutoResponder', 'Off');

  // Auto responder is on when an item can be found - will respond Will-Supply, when not found, left for a user to respond.
  RefdataValue.lookupOrCreate('AutoResponder', 'On: will supply only');

  // AutoResponder is ON and will automatically reply not-available if an item cannot be located
  def ar_on = RefdataValue.lookupOrCreate('AutoResponder', 'On: will supply and cannot supply');


  AppSetting auto_responder_status = AppSetting.findByKey('auto_responder_status') ?: new AppSetting( 
                                  section:'autoResponder',
                                  settingType:'Refdata',
                                  vocab:'AutoResponder',
                                  key: 'auto_responder_status',
                                  value: ar_on?.value).save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('AutoResponder_Cancel', 'Off');
  def arc_on = RefdataValue.lookupOrCreate('AutoResponder_Cancel', 'On');
  
  AppSetting auto_responder_cancel = AppSetting.findByKey('auto_responder_cancel') ?: new AppSetting( 
                                  section:'autoResponder',
                                  settingType:'Refdata',
                                  vocab:'AutoResponder_Cancel',
                                  key: 'auto_responder_cancel',
                                  value: arc_on?.value).save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'No longer available', 'unavailable');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'Missing', 'missing');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'Incorrect', 'incorrect');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'Other', 'other');

  RefdataValue.lookupOrCreate('cancellationReasons', 'Requested item is locally available', 'available_locally');
  RefdataValue.lookupOrCreate('cancellationReasons', 'User account is invalid', 'invalid_user');
  RefdataValue.lookupOrCreate('cancellationReasons', 'User requested cancellation', 'patron_requested');

  RefdataValue.lookupOrCreate('ChatAutoRead', 'Off');
  RefdataValue.lookupOrCreate('ChatAutoRead', 'On');
  RefdataValue.lookupOrCreate('ChatAutoRead', 'On (excluding action messages)');

  AppSetting chat_auto_read = AppSetting.findByKey('chat_auto_read') ?: new AppSetting( 
                                  section:'chat',
                                  settingType:'Refdata',
                                  vocab:'ChatAutoRead',
                                  key: 'chat_auto_read',
                                  defValue: 'on').save(flush:true, failOnError: true);

  AppSetting default_institutional_patron_id = AppSetting.findByKey('default_institutional_patron_id') ?: new AppSetting( 
    section:'requests',
    settingType:'String',
    key: 'default_institutional_patron_id').save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('loanConditions', 'LibraryUseOnly');
  RefdataValue.lookupOrCreate('loanConditions', 'NoReproduction');
  RefdataValue.lookupOrCreate('loanConditions', 'SignatureRequired');
  RefdataValue.lookupOrCreate('loanConditions', 'SpecCollSupervReq');
  RefdataValue.lookupOrCreate('loanConditions', 'WatchLibraryUseOnly');
  RefdataValue.lookupOrCreate('loanConditions', 'Other');

  RefdataValue.lookupOrCreate('YNO', 'Yes')
  RefdataValue.lookupOrCreate('YNO', 'No')
  RefdataValue.lookupOrCreate('YNO', 'Other')

  RefdataValue.lookupOrCreate('LoanPolicy', 'Lending all types')
  RefdataValue.lookupOrCreate('LoanPolicy', 'Not Lending')
  RefdataValue.lookupOrCreate('LoanPolicy', 'Lendin Physical only')
  RefdataValue.lookupOrCreate('LoanPolicy', 'Lending Electronic only')

  RefdataValue.lookupOrCreate('noticeFormats', 'E-mail', 'email');
  RefdataValue.lookupOrCreate('noticeTriggers', 'New request');
  RefdataValue.lookupOrCreate('noticeTriggers', 'End of rota');
  RefdataValue.lookupOrCreate('noticeTriggers', 'Request cancelled');

  def cp_ns = ensureTextProperty('ILLPreferredNamespaces', false);
  def cp_url = ensureTextProperty('url', false);
  def cp_z3950_base_name = ensureTextProperty('Z3950BaseName', false);
  def cp_local_institutionalPatronId = ensureTextProperty('local_institutionalPatronId', true, label='Institutional patron ID');
  def cp_local_alma_agency = ensureTextProperty('ALMA_AGENCY_ID', true, label='ALMA Agency ID');
  def cp_additional_headers = ensureTextProperty('AdditionalHeaders', false, 'Additional Headers');

  NamingAuthority reshare = NamingAuthority.findBySymbol('RESHARE') ?: new NamingAuthority(symbol:'RESHARE').save(flush:true, failOnError:true);
  NamingAuthority isil = NamingAuthority.findBySymbol('ISIL') ?: new NamingAuthority(symbol:'ISIL').save(flush:true, failOnError:true);
  NamingAuthority oclc = NamingAuthority.findBySymbol('OCLC') ?: new NamingAuthority(symbol:'OCLC').save(flush:true, failOnError:true);
  NamingAuthority exl = NamingAuthority.findBySymbol('EXL') ?: new NamingAuthority(symbol:'EXL').save(flush:true, failOnError:true);
  NamingAuthority palci = NamingAuthority.findBySymbol('PALCI') ?: new NamingAuthority(symbol:'PALCI').save(flush:true, failOnError:true);
  NamingAuthority cardinal = NamingAuthority.findBySymbol('CARDINAL') ?: new NamingAuthority(symbol:'CARDINAL').save(flush:true, failOnError:true);

  def cp_accept_returns_policy = ensureRefdataProperty('policy.ill.returns', false, 'YNO', 'Accept Returns' )
  def cp_physical_loan_policy = ensureRefdataProperty('policy.ill.loan_policy', false, 'LoanPolicy', 'ILL Loan Policy' )
  def cp_last_resort_policy = ensureRefdataProperty('policy.ill.last_resort', false, 'YNO', 'Consider Institution As Last Resort' )
  def cp_lb_ratio = ensureTextProperty('policy.ill.InstitutionalLoanToBorrowRatio', false, label='ILL Loan To Borrow Ratio');

  def folio_si_routing_adapter = RefdataValue.lookupOrCreate('RequestRoutingAdapter', 'FOLIOSharedIndex');
  def static_routing_adapter = RefdataValue.lookupOrCreate('RequestRoutingAdapter', 'Static');

  AppSetting routing_adapter = AppSetting.findByKey('routing_adapter') ?: new AppSetting(
                                  section:'Request Routing',
                                  settingType:'Refdata',
                                  vocab:'RequestRoutingAdapter',
                                  key: 'routing_adapter',
                                  value: folio_si_routing_adapter.value).save(flush:true, failOnError: true);


  AppSetting static_routing = AppSetting.findByKey('static_routes') ?: new AppSetting(
                                  section:'Request Routing',
                                  settingType:'String',
                                  key: 'static_routes',
                                  value: '').save(flush:true, failOnError: true);

  // To delete an unwanted action add State Model, State, Action to this array
  [
    [ 'PatronRequest', 'REQ_LOCAL_REVIEW', 'requesterCancel' ],
    [ 'PatronRequest', 'REQ_LOCAL_REVIEW', 'supplierCannotSupply' ]
  ].each { action_to_remove ->
    println("Remove available action ${action_to_remove}");
    try {
      AvailableAction.executeUpdate('''delete from AvailableAction 
                                       where id in ( select aa.id from AvailableAction as aa where aa.actionCode=:code and aa.fromState.code=:fs and aa.model.shortcode=:sm)''',
                                    [code:action_to_remove[2],fs:action_to_remove[1],sm:action_to_remove[0]]);
    }
    catch ( Exception e ) {
      println("Unable to delete action ${action_to_remove} - ${e.message}");
    }
  }

  // This looks slightly odd, but rather than litter this file with an ever growing list of
  // random delete statements, if you wish to delete
  // deprecated refdata values, add a new line to the array here consisting of [ 'VALUE', 'CATEGORY' ]
  [ 
    [ 'sirsi', 'HostLMSIntegrationAdapter' ]
  ].each { r ->
    println("Remove refdata value : ${r}");
    try {
      RefdataValue.executeUpdate('delete from RefdataValue where id in ( select rdv.id from RefdataValue as rdv where rdv.value = :v and rdv.owner.desc = :d)',
                                 [v:RefdataValue.normValue(r[0]),d:r[1]]);
    }
    catch ( Exception e ) {
      println("Unable to delete refdata ${r} - ${e.message}");
    }
  }


  println("_data.groovy complete");
}
catch ( Exception e ) {
  e.printStackTrace();
}


