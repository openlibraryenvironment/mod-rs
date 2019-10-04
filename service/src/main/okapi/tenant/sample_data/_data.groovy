import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address

AppSetting Z3950 = AppSetting.findByKey('z3950') ?: new AppSetting(
  version: 0,
  key: 'z3950'
  value: '',
);.save(flush:true, failOnError: true);