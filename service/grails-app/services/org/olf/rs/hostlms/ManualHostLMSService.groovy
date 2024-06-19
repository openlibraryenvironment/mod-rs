package org.olf.rs.hostlms;

import org.olf.rs.PatronRequest
import org.olf.rs.lms.HostLMSActions;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.logging.IHoldingLogDetails;
import org.olf.rs.logging.INcipLogDetails;
import org.olf.rs.settings.ISettings;

/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class ManualHostLMSService implements HostLMSActions {
  Map placeHold(String instanceIdentifier, String itemIdentifier) {
    def result=[:]
    result
  }

  ItemLocation determineBestLocation(ISettings settings, PatronRequest pr, IHoldingLogDetails holdingLogDetails) {
    ItemLocation location = null;
    return location;
  }

  public Map lookupPatron(ISettings settings, String patron_id, INcipLogDetails ncipLogDetails) {
    log.debug("lookupPatron(${patron_id})");
    Map result = [status: 'OK', reason: 'spoofed', result: true ];
    return result
  }

  public Map checkoutItem(
    ISettings settings,
    String requestId,
    String itemBarcode,
    String borrowerBarcode,
    INcipLogDetails ncipLogDetails
  ) {
    log.debug("checkoutItem(${itemBarcode},${borrowerBarcode})");

    return [
      result:true,
      reason: 'spoofed'
    ]
  }

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
  ) {

    return [
      result:true,
      reason: 'spoofed'
    ];
  }

  public Map checkInItem(ISettings settings, String item_id, INcipLogDetails ncipLogDetails) {
    return [
      result:true,
      reason: 'spoofed'
    ];
  }

  public Map requestItem(ISettings settings, String requestId, String itemId, String borrowerBarcode, String pickupLocation,
      INcipLogDetails ncipLogDetails) {
    return [
      result: true,
      reason: 'spoofed'
    ];
  }

  public Map cancelRequestItem(ISettings settings, String requestId, String userId, INcipLogDetails ncipLogDetails) {
    return [
      result: true,
      reason: 'spoofed'
    ];
  }

  boolean isManualCancelRequestItem() {
    return false
  }
}
