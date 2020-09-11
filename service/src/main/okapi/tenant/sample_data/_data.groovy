import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*

import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import org.olf.okapi.modules.directory.NamingAuthority;

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


  // External LMS call methods -- none represents no integration and we will spoof a passing response instead
  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'None');
  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'NCIP2');

AppSetting borrower_check = AppSetting.findByKey('borrower_check') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'BorrowerCheckMethod',
                                  key: 'borrower_check'
                                  ).save(flush:true, failOnError: true);

RefdataValue.lookupOrCreate('CheckOutMethod', 'None');
RefdataValue.lookupOrCreate('CheckOutMethod', 'NCIP2');
  
AppSetting host_lms_integration = AppSetting.findByKey('check_out_item') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'CheckOutMethod',
                                  key: 'check_out_item').save(flush:true, failOnError: true);

RefdataValue.lookupOrCreate('CheckInMethod', 'None');
RefdataValue.lookupOrCreate('CheckInMethod', 'NCIP2');
  
AppSetting host_lms_integration = AppSetting.findByKey('check_in_item') ?: new AppSetting(
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'CheckInMethod',
                                  key: 'check_in_item').save(flush:true, failOnError: true);


RefdataValue.lookupOrCreate('AcceptItemMethod', 'None');
RefdataValue.lookupOrCreate('AcceptItemMethod', 'NCIP2');
  
AppSetting host_lms_integration = AppSetting.findByKey('accept_item') ?: new AppSetting(
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

  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'unavailable');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'missing');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'incorrect');
  RefdataValue.lookupOrCreate('cannotSupplyReasons', 'other');

  RefdataValue.lookupOrCreate('ChatAutoRead', 'Off');
  RefdataValue.lookupOrCreate('ChatAutoRead', 'On');
  RefdataValue.lookupOrCreate('ChatAutoRead', 'On (excluding action messages)');

  AppSetting chat_auto_read = AppSetting.findByKey('chat_auto_read') ?: new AppSetting( 
                                  section:'chat',
                                  settingType:'Refdata',
                                  vocab:'ChatAutoRead',
                                  key: 'chat_auto_read',
                                  defValue: 'on').save(flush:true, failOnError: true);

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

  def cp_ns = ensureTextProperty('ILLPreferredNamespaces', false);
  def cp_url = ensureTextProperty('url', false);
  def cp_demoprop = ensureTextProperty('demoCustprop', false);
  def cp_test_prop = ensureTextProperty('TestParam', false);
  def cp_z3950_base_name = ensureTextProperty('Z3950BaseName', false);
  def cp_local_institutionalPatronId = ensureTextProperty('local_institutionalPatronId', true, label='Institutional patron ID');
  def cp_local_widget2 = ensureTextProperty('local_widget_2', true, label='Widget 2');
  def cp_local_widget3 = ensureTextProperty('local_widget_3', true, label='Widget 3');
  def cp_local_alma_agency = ensureTextProperty('ALMA_AGENCY_ID', true, label='ALMA Agency ID');

  NamingAuthority reshare = NamingAuthority.findBySymbol('RESHARE') ?: new NamingAuthority(symbol:'RESHARE').save(flush:true, failOnError:true);
  NamingAuthority isil = NamingAuthority.findBySymbol('ISIL') ?: new NamingAuthority(symbol:'ISIL').save(flush:true, failOnError:true);
  NamingAuthority oclc = NamingAuthority.findBySymbol('OCLC') ?: new NamingAuthority(symbol:'OCLC').save(flush:true, failOnError:true);
  NamingAuthority exl = NamingAuthority.findBySymbol('EXL') ?: new NamingAuthority(symbol:'EXL').save(flush:true, failOnError:true);
  NamingAuthority palci = NamingAuthority.findBySymbol('PALCI') ?: new NamingAuthority(symbol:'PALCI').save(flush:true, failOnError:true);
  NamingAuthority cardinal = NamingAuthority.findBySymbol('CARDINAL') ?: new NamingAuthority(symbol:'CARDINAL').save(flush:true, failOnError:true);

  def cp_accept_returns_policy = ensureRefdataProperty('policy.ill.returns', false, 'YNO', 'Accept Returns' )
  def cp_physical_loan_policy = ensureRefdataProperty('policy.ill.loan_policy', false, 'LoanPolicy', 'ILL Loan Policy' )
  def cp_last_resort_policy = ensureRefdataProperty('policy.ill.last_resort', false, 'YNO', 'Consider Institution As Last Resort' )
  def cp_lb_ratio = ensureTextProperty('policy.ill.InstitutionalLoanToBorrowRatio', true, label='ILL Loan To Borrow Ratio');

  println("_data.groovy complete");
}
catch ( Exception e ) {
  e.printStackTrace();
}


