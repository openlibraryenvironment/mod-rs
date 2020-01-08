package org.olf.rs.lms;

import org.olf.rs.PatronRequest;

public interface HostLMSActions {

       public boolean checkoutItem(String itemBarcode, String borrowerBarcode);
           public Map lookupPatron(String patron_id);
  public ItemLocation determineBestLocation(PatronRequest pr);

}
