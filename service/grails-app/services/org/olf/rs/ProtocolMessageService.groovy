package org.olf.rs

/**
 * Allow callers to request that a protocol message be sent to a remote (Or local) service. Callers
 * provide the requesting and responding symbol and the content of the message, this service works out
 * the most appropriate method/protocol. Initially this will always be loopback.
 *
 */
class ProtocolMessageService {


  /**
   * @Param eventData : A map structured as followed 
   *   event: {
   *     envelope:{
   *       sender:{
   *         symbol:''
   *       }
   *       recipient:{
   *         symbol:''
   *       }
   *       messageType:''
   *       messageBody:{
   *       }
   *   }
   */
  public void sendProtocolMessage(Map eventData) {
    def responseConfirmed = messageConfirmation(eventData, "request")
    log.debug("sendRequest called for ${eventData.payload.id}")
    //Make this create a new request in the responder's system

    // The first thing to do is to look in the internal SharedConfig to see if the recipient is a
    // tenant in this system. If so, we can simply call the loopback interface.
  }

  /**
   * @Param eventData Symmetrical with the section above.
   */
  public void handleIncomingMessage(Map eventData) {
    // Recipient must be a tenant in the SharedConfig
  }


  public messageConfirmation(eventData, messageType) {
    //TODO make this able to return a confirmation message if request/supplying agency message/requesting agency message are successful,
    //and returning error messages if not
    return true
  }
}
