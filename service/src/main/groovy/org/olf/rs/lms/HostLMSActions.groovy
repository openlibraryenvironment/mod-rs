package org.olf.rs.lms;

import org.olf.rs.PatronRequest;
import org.olf.okapi.modules.directory.Symbol;

public interface HostLMSActions {

  /**
   * Re:Share has determined that an item located using RTAC is a candidate to be loaned,
   * and that the item has been pulled from the shelf by staff. The core engine would like
   * the host LMS to check the item out of the core LMS so that it can be checked into the
   * reshare system for loaning. This function is called with the local item barcode and
   * the barcode of the borrower at the remote system.
   * @param itemBarcode - the barcode of the item to be checked out of the host LMS and into reshare
   * @param borrowerBarcode - the borrower at the remote LMS
   * @param requesterDirectorySymbol - the symbol from the directory for the requester - can be used to get all directory info
   * @return A map containing the following keys
   *    'result' - a mandatory Boolean True if the checkout succeeded, False otherwise
   *    'status' - an optional String which is the state in the Supplier state model that this request should be transitioned to
   */
  public Map checkoutItem(String itemBarcode, 
                          String borrowerBarcode, 
                          Symbol requesterDirectorySymbol);

  /**
   * @return a Map containing the following keys userid, givenName, surname, status
   */
  public Map lookupPatron(String patron_id);

  public ItemLocation determineBestLocation(PatronRequest pr);

}
