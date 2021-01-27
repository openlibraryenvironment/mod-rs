package org.olf.rs;

public interface SharedIndexActions {
  /**
   * findAppropriateCopies - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description A Map of properies that describe the item. Currently understood properties:
   *                         systemInstanceIdentifier -
   *                         title                    - the title of the item
   * @return instance of SharedIndexAvailability which tells us where we can find the item.
   */
  public List<AvailabilityStatement> findAppropriateCopies(Map description);

  /**
   * fetch - Accept a map of name:value pairs that describe an instance and see if we can locate
   * any appropriate copies in the shared index.
   * @param description See above. 
   * @return List of records as returned by the shared index. TODO: standardise record format.
   */
  public List<String> fetchSharedIndexRecords(Map description);
}
