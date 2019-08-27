package org.olf.rs;

import grails.events.annotation.Subscriber

public class ReshareApplicationEventHandlerService {

  @Subscriber('PREventIndication')
  public handleApplicationEvent(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleApplicationEvent(${eventData})");
  }
}
