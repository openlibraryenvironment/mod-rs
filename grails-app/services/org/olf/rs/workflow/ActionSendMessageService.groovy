package org.olf.rs.workflow

import grails.gorm.transactions.Transactional;
import org.olf.rs.PatronRequest;
import org.olf.rs.workflow.AbstractAction.ActionResponse;
import groovy.util.logging.Slf4j
import org.olf.rs.RabbitService
import org.olf.rs.DirectoryService
import org.olf.rs.rabbit.Queue;

@Slf4j
@Transactional
class ActionSendMessageService extends AbstractAction {

  /** The service that we use to put the message on the queue - Injected by framework */
  RabbitService rabbitService;

  DirectoryService directoryService;

  /** Returns the action that this class represents */
  @Override
  String getActionCode() {
    return(Action.SEND_MESSAGE);
  }

  /** Performs the action */
  @Override
  ActionResponse perform(PatronRequest requestToBeProcessed) {
    // For the time being we just return OK as we do not do anything
    log.debug("ActionSendMessageService::perform(${requestToBeProcessed})");

    Map directoryEntryForResponder = directoryService.getDirectoryEntryForSymbol('RESHARE','DIKUA');

    // Not sure this is what's intended, but want to start wiring together the edge module
    // so sending a message this way for now, expect this to change when @chas gets to it.
    rabbitService.Send(Queue.ISO18626, 
                       java.util.UUID.randomUUID().toString(),
                       [
                         "stub":"stub"
                       ]);

    return(ActionResponse.SUCCESS);
  }
}
