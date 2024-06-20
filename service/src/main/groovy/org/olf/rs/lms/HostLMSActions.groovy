package org.olf.rs.lms;

import org.olf.rs.PatronRequest;
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.logging.INcipLogDetails;
import org.olf.rs.settings.ISettings;

public interface HostLMSActions {

  /**
   * Re:Share has determined that an item located using RTAC is a candidate to be loaned,
   * and that the item has been pulled from the shelf by staff. The core engine would like
   * the host LMS to check the item out of the core LMS so that it can be checked into the
   * reshare system for loaning. This function is called with the local item barcode and
   * the barcode of the borrower at the remote system.
   * @param itemBarcode - the barcode of the item to be checked out of the host LMS and into reshare
   * @param borrowerBarcode - the borrower at the remote LMS
   * @param ncipLogDetails the object used to log the details of  what went of
   *
   * @return A map containing the following keys
   *    'result' - a mandatory Boolean True if the checkout succeeded, False otherwise
   *    'status' - an optional String which is the state in the Supplier state model that this request should be transitioned to. Possible states are currently defined in
   *               housekeeping service - see the link below.
   *
   * @See https://github.com/openlibraryenvironment/mod-rs/blob/master/service/grails-app/services/org/olf/rs/HousekeepingService.groovy#L97
   */
  public Map checkoutItem(
      ISettings settings,
      String requestId,
      String itemBarcode,
      String borrowerBarcode,
      INcipLogDetails ncipLogDetails
  );

  /**
   * Use a Host LMS API to look up the patron ID and return information about the patron back to Re:Share
   * @return a Map containing the following keys userid, givenName, surname, status
   */
  public Map lookupPatron(ISettings settings, String patron_id, INcipLogDetails ncipLogDetails);

  /**
   * Use whatever RTAC the LMS provides to try and determine the most appropriate available copy/location for the item identified
   * in the attached patron request.
   * @Return and ItemLocation structure
   *
   * @See https://github.com/openlibraryenvironment/mod-rs/blob/master/service/grails-app/domain/org/olf/rs/PatronRequest.groovy
   * @See https://github.com/openlibraryenvironment/mod-rs/blob/master/service/src/main/groovy/org/olf/rs/AvailabilityStatement.groovy
   */
  public ItemLocation determineBestLocation(ISettings settings, PatronRequest pr, IHoldingLogDetails holdingLogDetails);


  public Map acceptItem(
      ISettings settings,
      String item_id,
      String request_id,
      String user_id,
      String author,
      String title,
      String isbn,
      String call_number,
      String pickup_location,
      String requested_action,
      INcipLogDetails ncipLogDetails
  );

  public Map checkInItem(ISettings settings, String item_id, INcipLogDetails ncipLogDetails);

  public Map requestItem(
          ISettings settings,
          String requestId,
          String itemId,
          String borrowerBarcode,
          String pickupLocation,
          INcipLogDetails ncipLogDetails
  );

  public Map cancelRequestItem(ISettings settings, String requestId, String userId, INcipLogDetails ncipLogDetails);

  Map deleteItem(ISettings settings, String itemId, INcipLogDetails ncipLogDetails)
}
