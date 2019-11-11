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
                                  section:'Local NCIP',
                                  settingType:'String',
                                  key: 'ncip_server_address'
                                  ).save(flush:true, failOnError: true);

  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'None');
  RefdataValue.lookupOrCreate('BorrowerCheckMethod', 'NCIP2');

  AppSetting borrower_check = AppSetting.findByKey('borrower_check') ?: new AppSetting( 
                                  section:'Requester Validation',
                                  settingType:'Refdata',
                                  vocab:'BorrowerCheckMethod',
                                  key: 'borrower_check'
                                  ).save(flush:true, failOnError: true);

  AppSetting z3950_address = AppSetting.findByKey('request_id_prefix') ?: new AppSetting( 
                                  section:'requests',
                                  settingType:'String',
                                  key: 'request_id_prefix',
                                  ).save(flush:true, failOnError: true);

}
catch ( Exception e ) {
  e.printStackTrace();
}
