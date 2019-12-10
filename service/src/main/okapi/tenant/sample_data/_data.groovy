import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*


try {
  println("Create z3950 server address");
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

  AppSetting last_resort_lenders = AppSetting.findByKey('last_resort_lenders') ?: new AppSetting( 
                                  section:'requests',
                                  settingType:'String',
                                  key: 'last_resort_lenders',
                                  defValue: '').save(flush:true, failOnError: true);

}
catch ( Exception e ) {
  e.printStackTrace();
}
