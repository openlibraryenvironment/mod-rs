package org.olf.rs.lms;

import org.olf.rs.PatronRequest;

public interface HostLMSActions {

  public boolean checkoutItem(String itemBarcode, String borrowerBarcode);

  /**
   * @return a Map containing the following keys userid, givenName, surname, status
   */
  public Map lookupPatron(String patron_id);

  public ItemLocation determineBestLocation(PatronRequest pr);

}
