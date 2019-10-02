package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import java.util.concurrent.ThreadLocalRandom;

/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class SharedIndexService {

  public static def mockData = [
    [ symbol: 'RESHARE:DIKU' ],
    [ symbol: 'RESHARE:KINT' ],
    [ symbol: 'RESHARE:TestInst01' ],
    [ symbol: 'RESHARE:TestInst02' ],
    [ symbol: 'RESHARE:TestInst03' ],
    [ symbol: 'RESHARE:TestInst04' ],
    [ symbol: 'RESHARE:TestInst05' ],
    [ symbol: 'RESHARE:TestInst06' ],
    [ symbol: 'RESHARE:TestInst07' ],
    [ symbol: 'RESHARE:TestInst09' ],
    [ symbol: 'RESHARE:TestInst10' ]
  ]

  /**
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         title - the title of the item
   * @return instance of SharedIndexAvailability which tells us where we can find the item.
   */
  public List<AvailabilityStatement> findAppropriateCopies(Map description) {

    List<AvailabilityStatement> result = new ArrayList<AvailabilityStatement>()

    log.debug("findAppropriateCopies(${description}) - tenant is ${Tenants.currentId()}");

    List<String> all_libs = mockData.collect { it.symbol };
    int num_responders = ThreadLocalRandom.current().nextInt(0, 5 + 1);

    List<String> lendingStrings = new ArrayList<String>();
    for ( int i=0; i<num_responders; i++ ) {
      lendingStrings.add(all_libs.remove(ThreadLocalRandom.current().nextInt(0,all_libs.size())));
    }

    log.debug("Decded these are the lenders: Num lenders: ${num_responders} ${lendingStrings}");

    lendingStrings.each { ls ->
      // String instance_id = java.util.UUID.randomUUID().toString();
      String instance_id = '000026460'
      String copy_id = java.util.UUID.randomUUID().toString();
      result.add(new AvailabilityStatement(symbol:ls,instanceIdentifier:instance_id,copyIdentifier:copy_id));
    }
    // result.add(new AvailabilityStatement(symbol:'RESHARE:LOCALSYMBOL',instanceIdentifier:'MOCK_INSTANCE_ID_00001',copyIdentifier:'MOCK_COPY_ID_00001'));

    // Return an empty list
    return result;
  }
}

