package org.olf.rs.hostlms;

import org.olf.rs.PatronRequest
import groovyx.net.http.HttpBuilder
import org.olf.rs.ItemLocation;
import org.olf.rs.statemodel.Status;
import com.k_int.web.toolkit.settings.AppSetting
import groovy.xml.StreamingMarkupBuilder
import static groovyx.net.http.HttpBuilder.configure
import groovyx.net.http.FromServer;
import com.k_int.web.toolkit.refdata.RefdataValue
import static groovyx.net.http.ContentTypes.XML
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.lms.HostLMSActions;
import org.olf.okapi.modules.directory.Symbol;



/**
 * The interface between mod-rs and any host Library Management Systems
 *
 */
public class ManualHostLMSService implements HostLMSActions {
  Map placeHold(String instanceIdentifier, String itemIdentifier) {
    def result=[:]
    result
  }

  ItemLocation determineBestLocation(PatronRequest pr) {
    ItemLocation location = null;
    return location;
  }
  
  public Map lookupPatron(String patron_id) {
    log.debug("lookupPatron(${patron_id})");
    Map result = [status: 'OK', reason: 'spoofed', result: true ];
    return result
  }

  public Map checkoutItem(String requestId,
                          String itemBarcode,
                          String borrowerBarcode,
                          Symbol requesterDirectorySymbol) {
    log.debug("checkoutItem(${itemBarcode},${borrowerBarcode},${requesterDirectorySymbol})");

    //FIXME this should be removed -- only here for testing purposes
    def randNum = Math.random()

    if (randNum < 0.5) {
      return [
        result:true,
        reason: 'spoofed'
      ]
    }
    
    return [
      result:false,
      reason: 'spoofed'
    ]
  }

  public Map acceptItem(String item_id,
                            String request_id,
                            String user_id,
                            String author,
                            String title,
                            String isbn,
                            String call_number,
                            String pickup_location,
                            String requested_action) {
    return [
      result:true,
      reason: 'spoofed'
    ];
  }

  public Map checkInItem(String item_id) {
    return [
      result:true,
      reason: 'spoofed'
    ];
  }

}
