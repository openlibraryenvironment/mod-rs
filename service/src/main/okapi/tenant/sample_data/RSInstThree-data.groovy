import org.olf.rs.HostLMSShelvingLocation
import org.olf.rs.HostLMSLocation
import org.olf.rs.ShelvingLocationSite
import org.olf.rs.HostLMSPatronProfile


// Genuine DIKU sample data
try {

  System.out.println("Loading sample DIKU data for mod-rs");

  HostLMSLocation main = HostLMSLocation.findByCode('dikumain') ?:
    new HostLMSLocation(code:'dikumain', name:'dikumain', supplyPreference:1).save(flush:true, failOnError:true);

  HostLMSShelvingLocation stacks = HostLMSShelvingLocation.findByCode('stacks') ?:
    new HostLMSShelvingLocation(code:'stacks', name:'stacks', supplyPreference:1).save(flush:true, failOnError:true);

  ShelvingLocationSite sls = ShelvingLocationSite.findByShelvingLocationAndLocation(stacks, main) ?:
    new ShelvingLocationSite(shelvingLocation:stacks, location:main, supplyPreference:-1).save(flush:true, failOnError:true);

  HostLMSPatronProfile pp = HostLMSPatronProfile.findByCode('staff') ?:
    new HostLMSPatronProfile(code:'staff', name:'staff').save(flush:true, failOnError:true);

}
catch ( Exception e ) {
  log.error("Problem installing sample data for diku",e);
}
