import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*

import com.k_int.web.toolkit.custprops.types.CustomPropertyRefdataDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import org.olf.okapi.modules.directory.NamingAuthority;

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

  AppSetting pullslip_template_url = AppSetting.findByKey('pullslip_template_url') ?: new AppSetting( 
                              section:'pullslipTemplateConfig',
                              settingType:'String',
                              key: 'pullslip_template_url'
                              ).save(flush:true, failOnError: true);

  TemplateContainer DEFAULT_EMAIL_TEMPLATE = TemplateContainer.findByName('DEFAULT_EMAIL_TEMPLATE') ?: new TemplateContainer(
    name: 'DEFAULT_EMAIL_TEMPLATE',
    templateResolver: [
      value: 'handlebars'
    ],
    description: 'A default email template for pullslip templates',
    context: 'pullslipTemplate',
    localizedTemplates: [
      [
        locality: 'en',
        template: [
          templateBody: '''
            <h1>Please configure the pull_slip_template_id setting</h1>
            The template has the following variables 
            <ul>
              <li>locations: The locations this pull slip report relates to</li>
              <li>pendingRequests: The actual requests pending printing at those locations<li>
              <li>numRequests: The total number of requests pending at those sites</li>
              <li>summary: A summary of pending pull slips at all locations</li>
              <li>foliourl: The base system URL of this folio install</li>
            </ul>
            For this run these values are
            <ul>
              <li>locations: {{locations}}</li>
              <li>pendingRequests: {{pendingRequests}}</li>
              <li>numRequests: {{numRequests}}</li>
              <li>summary: {{summary}}
              <li>foliourl: {{foliourl}}</li>
            </ul>

            You can access certain properties from these as follows

            <ul>
              <li>size of an array: {{pendingRequests.length}} </li>
              <li>get index in an array: {{locations.[0]}} </li>
              <li>Return a list from an array: 
                <ul>
                  {{#each summary}}
                    <li>There are {{this.[0]}} pending pull slips at location {{this.[1]}} </li>
                  {{/each}}
                </ul>
              </li>
            </ul>
          ''',
          header: '''Reshare {{pendingRequests.length}} new pull slips available'''
        ]
      ]
    ]
  ).save(flush:true, failOnError: true);

  AppSetting pull_slip_template_id = AppSetting.findByKey('pull_slip_template_id') ?: new AppSetting( 
    section:'pullslipTemplateConfig',
    settingType:'Template',
    vocab: 'pullslipTemplate',
    key: 'pull_slip_template_id',
    value: DEFAULT_EMAIL_TEMPLATE.id
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
  RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'WMS');
  def manual_adapter_rdv = RefdataValue.lookupOrCreate('HostLMSIntegrationAdapter', 'Manual');

  AppSetting host_lms_integration = AppSetting.findByKey('host_lms_integration') ?: new AppSetting( 
                                  section:'hostLMSIntegration',
                                  settingType:'Refdata',
                                  vocab:'HostLMSIntegrationAdapter',
                                  key: 'host_lms_integration',
                                  value: manual_adapter_rdv.value).save(flush:true, failOnError: true);
                                
  

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

  def cp_ns = ensureTextProperty('ILLPreferredNamespaces', false);
  def cp_url = ensureTextProperty('url', false);
  def cp_z3950_base_name = ensureTextProperty('Z3950BaseName', false);
  def cp_local_institutionalPatronId = ensureTextProperty('local_institutionalPatronId', true, label='Institutional patron ID');
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
  def cp_lb_ratio = ensureTextProperty('policy.ill.InstitutionalLoanToBorrowRatio', false, label='ILL Loan To Borrow Ratio');

  println("_data.groovy complete");
}
catch ( Exception e ) {
  e.printStackTrace();
}


