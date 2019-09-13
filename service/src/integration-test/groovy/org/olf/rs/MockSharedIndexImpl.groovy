package org.olf.rs;

import groovy.util.logging.*


/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
@Log4j
public class MockSharedIndexImpl extends SharedIndexService {

  /**
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         title - the title of the item
   * @return instance of SharedIndexAvailability which tells us where we can find the item.
   */
  public List<AvailabilityStatement> findAppropriateCopies(Map description) {
    log.debug("MockSharedIndexImpl::findAppropriateCopies(${description})");
    List<AvailabilityStatement> result = new ArrayList<AvailabilityStatement>()
    result.add(new AvailabilityStatement(symbol:'OCLC:AVL',instanceIdentifier:'MOCK_INSTANCE_ID_00001',copyIdentifier:'MOCK_COPY_ID_00001'));
    log.debug("findAppropriateCopies returns ${result}");
    return result;
  }
}

