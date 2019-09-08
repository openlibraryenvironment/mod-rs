package org.olf.rs;

import grails.gorm.multitenancy.Tenants

/**
 * The interface between mod-rs and the shared index is defined by this service.
 *
 */
public class SharedIndexService {

  /**
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         title - the title of the item
   * @return instance of SharedIndexAvailability which tells us where we can find the item.
   */
  public SharedIndexAvailability findAppropriateCopies(Map description) {

    log.debug("findAppropriateCopies(${description}) - tenant is ${Tenants.currentId()}");

    // Return an empty list
    return new SharedIndexAvailability(['OCLC:AVL']);
  }
}

