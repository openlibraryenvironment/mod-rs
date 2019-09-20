import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address

HostLMSLocation loc1 = HostLMSLocation.findByCode('MAIN') ?: new HostLMSLocation(
                                                                        code:'MAIN', 
                                                                        icalRrule:'RRULE:FREQ=MINUTELY;INTERVAL=10;WKST=MO').save(flush:true, failOnError:true);

HostLMSLocation loc2 = HostLMSLocation.findByCode('ANNEX') ?: new HostLMSLocation(
                                                                        code:'ANNEX', 
                                                                        icalRrule:'RRULE:FREQ=MINUTELY;INTERVAL=30;WKST=MO').save(flush:true, failOnError:true);

DirectoryEntry dm1 = DirectoryEntry.findById('Ethan1') ?: new DirectoryEntry(
                                                                        id: 'Ethan1',
                                                                        name: 'UT Austin').save(flush:true, failOnError:true);


DirectoryEntry dm2 = DirectoryEntry.findById('Ethan2') ?: new DirectoryEntry(
                                                                        id: 'Ethan2',
                                                                        name: 'UT Austin · Main Branch',
                                                                        parent: dm1).save(flush:true, failOnError:true);

DirectoryEntry dm3 = DirectoryEntry.findById('Ethan3') ?: new DirectoryEntry(
                                                                        id: 'Ethan3',
                                                                        name: 'New School').save(flush:true, failOnError:true);


DirectoryEntry dm4 = DirectoryEntry.findById('Ethan4') ?: new DirectoryEntry(
                                                                        id: 'Ethan4',
                                                                        name: 'New School · Law Library',
                                                                        parent: dm3).save(flush:true, failOnError:true);

DirectoryEntry dm5 = DirectoryEntry.findById('Ethan5') ?: new DirectoryEntry(
                                                                        id: 'Ethan5',
                                                                        name: 'University of Sheffield').save(flush:true, failOnError:true);


DirectoryEntry dm6 = DirectoryEntry.findById('Ethan6') ?: new DirectoryEntry(
                                                                        id: 'Ethan6',
                                                                        name: 'University of Sheffield · The IC',
                                                                        parent: dm5).save(flush:true, failOnError:true);

DirectoryEntry dm7 = DirectoryEntry.findById('Ethan7') ?: new DirectoryEntry(
                                                                        id: 'Ethan7',
                                                                        name: 'University of Leeds').save(flush:true, failOnError:true);


DirectoryEntry dm8 = DirectoryEntry.findById('Ethan8') ?: new DirectoryEntry(
                                                                        id: 'Ethan8',
                                                                        name: 'University of Leeds · Brotherton Library',
                                                                        parent: dm7).save(flush:true, failOnError:true);