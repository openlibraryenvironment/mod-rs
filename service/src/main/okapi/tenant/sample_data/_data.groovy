import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address
import com.k_int.web.toolkit.settings.AppSetting


try {
  println("Create z3950 server address");
  AppSetting z3950_address = AppSetting.findByKey('z3950_server_address') ?: new AppSetting(
   key: 'z3950_server_address'
  ).save(flush:true, failOnError: true);
  println("DONE Create z3950 server address");
}
catch ( Exception e ) {
  e.printStackTrace();
}
