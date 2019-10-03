import org.olf.rs.HostLMSLocation
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Address

DirectoryEntry dm1 = DirectoryEntry.findById('Ethan1') ?: new DirectoryEntry(
                                                                        id: 'Ethan1',
                                                                        name: 'UT Austin',
                                                                        slug: 'UT_Austin').save(flush:true, failOnError:true);


DirectoryEntry dm2 = DirectoryEntry.findById('Ethan2') ?: new DirectoryEntry(
                                                                        id: 'Ethan2',
                                                                        name: 'UT Austin: Main Branch',
                                                                        slug: 'UT_Austin_Main',
                                                                        parent: dm1).save(flush:true, failOnError:true);

DirectoryEntry dm3 = DirectoryEntry.findById('Ethan3') ?: new DirectoryEntry(
                                                                        id: 'Ethan3',
                                                                        name: 'New School',
                                                                        slug: 'New_School').save(flush:true, failOnError:true);


DirectoryEntry dm4 = DirectoryEntry.findById('Ethan4') ?: new DirectoryEntry(
                                                                        id: 'Ethan4',
                                                                        name: 'New School: Law Library',
                                                                        slug: 'New_School_Law',
                                                                        parent: dm3).save(flush:true, failOnError:true);

DirectoryEntry dm5 = DirectoryEntry.findById('Ethan5') ?: new DirectoryEntry(
                                                                        id: 'Ethan5',
                                                                        name: 'University of Sheffield',
                                                                        slug: 'UoS').save(flush:true, failOnError:true);


DirectoryEntry dm6 = DirectoryEntry.findById('Ethan6') ?: new DirectoryEntry(
                                                                        id: 'Ethan6',
                                                                        name: 'University of Sheffield: The IC',
                                                                        slug: 'UoS_IC',
                                                                        parent: dm5).save(flush:true, failOnError:true);

DirectoryEntry dm7 = DirectoryEntry.findById('Ethan7') ?: new DirectoryEntry(
                                                                        id: 'Ethan7',
                                                                        name: 'University of Leeds',
                                                                        slug: 'UoL').save(flush:true, failOnError:true);


DirectoryEntry dm8 = DirectoryEntry.findById('Ethan8') ?: new DirectoryEntry(
                                                                        id: 'Ethan8',
                                                                        name: 'University of Leeds: Brotherton Library',
                                                                        slug: 'UoL_BL',
                                                                        parent: dm7).save(flush:true, failOnError:true);
