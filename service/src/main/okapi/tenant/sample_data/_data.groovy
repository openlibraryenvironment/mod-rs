import org.olf.rs.HostLMSLocation

HostLMSLocation loc1 = HostLMSLocation.findByCode('MAIN') ?: new HostLMSLocation(
                                                                        code:'MAIN', 
                                                                        icalRrule:'RRULE:FREQ=MINUTELY;INTERVAL=10;WKST=MO').save(flush:true, failOnError:true);

HostLMSLocation loc2 = HostLMSLocation.findByCode('ANNEX') ?: new HostLMSLocation(
                                                                        code:'ANNEX', 
                                                                        icalRrule:'RRULE:FREQ=MINUTELY;INTERVAL=30;WKST=MO').save(flush:true, failOnError:true);
