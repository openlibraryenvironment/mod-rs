package org.olf.rs

class ProtocolMessageService {


  public void sendRequest(eventData) {

    def responseConfirmed = messageConfirmation(eventData, "request")
    log.debug("sendRequest called for ${eventData.payload.id}")
    //Make this create a new request in the responder's system
  }
  public messageConfirmation(eventData, messageType) {
    //TODO make this able to return a confirmation message if request/supplying acency message/requesting agency message are successful,
    //and returning error messages if not
    return true
  }
}
