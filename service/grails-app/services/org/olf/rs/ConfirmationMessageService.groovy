package org.olf.rs;

import java.text.SimpleDateFormat

import groovy.xml.StreamingMarkupBuilder


class ConfirmationMessageService {

  public def confirmationMessageReadable(def confirmationMessage, boolean add_prolog = false) {
    StringWriter sw = new StringWriter();

    if ( add_prolog )
      sw << '<?xml version="1.0" encoding="utf-8"?>'

    sw << new StreamingMarkupBuilder().bind (confirmationMessage)
    String message = sw.toString();

    return message
  }

  // This method creates a confirmation message
  public def makeConfirmationMessage(def req_result) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    def currentTime = dateFormatter.format(new Date())
    return {
        ISO18626Message( 'ill:version':'1.0',
                        'xmlns':'http://illtransactions.org/2013/iso18626',
                        'xmlns:ill': 'http://illtransactions.org/2013/iso18626',
                        'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                        'xsi:schemaLocation': 'http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_1.xsd' ) {

          switch (req_result.messageType) {
            // The main difference is which heading the confirmation body comes under
            case "REQUEST":
              requestConfirmation {
                makeConfirmationMessageBody(delegate, req_result);
              }
              break;
            case "SUPPLYING_AGENCY_MESSAGE":
              supplyingAgencyMessageConfirmation {
                makeConfirmationMessageBody(delegate, req_result);
              }
              break;
            case "REQUESTING_AGENCY_MESSAGE":
              requestingAgencyMessageConfirmation {
                makeConfirmationMessageBody(delegate, req_result);
              }
              break;
            default:
              throw new Exception ("makeConfirmationMessage expects passed req_result to contain a valid messageType")
          }
        }
    }
  }

  // This method creates the body of the confirmation message--mostly uniform between the confirmation message types
  void makeConfirmationMessageBody(def del, def req_result) {
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    def currentTime = dateFormatter.format(new Date())
    exec(del) {
      confirmationHeader {
        supplyingAgencyId {
          agencyIdType(req_result.supIdType)
          agencyIdValue(req_result.supId)
        }
        requestingAgencyId {
          agencyIdType(req_result.reqAgencyIdType)
          agencyIdValue(req_result.reqAgencyId)
        }
        timestamp(currentTime)
        requestingAgencyRequestId(req_result.reqId)
        timestampReceived(req_result.timeRec)
        messageStatus(req_result.status)
        if (req_result.errorType != null) {
          errorData {
            errorType(req_result.errorType)
            errorValue(req_result.errorValue)
          }
        }
        if (req_result.messageType == "SUPPLYING_AGENCY_MESSAGE") {
          reasonForMessage(req_result.reasonForMessage)
        }
        if (req_result.messageType == "REQUESTING_AGENCY_MESSAGE") {
          action(req_result.action)
        }
      }
    }
  }

  // Clever bit of wizardry to allow us to inject the calling class into the builder
  void exec ( def del, Closure c ) {
    c.rehydrate(del, c.owner, c.thisObject)()
  }
}


