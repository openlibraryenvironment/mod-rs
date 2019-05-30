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

    // This is fluff - obviously what we shoulds really be doing here is looking up the active rota position
    // and sending there.

    Map directoryEntryForResponder = directoryService.getDirectoryEntryForSymbol('RESHARE','DIKUA');

    // Chas says this should be done for us by the framework, but doing this here makes things appear to work as they should
    // requestToBeProcessed.awaitingProtocolResponse = true;
    // requestToBeProcessed.pendingAction = Action.get(Action.SEND_MESSAGE);
    // requestToBeProcessed.save(flush:true, failOnError:true);

    rabbitService.sendToExchange(
                       'RSExchange',
                       'RSOutViaProtocol.ISO18626/HTTP(S)',// 'RSOutViaProtocol.ISO18626/HTTP(S)',
                       requestToBeProcessed.id,
                       [
                         "header":[
                           // This will come from directory service in time, for now, loop back to locally running edge module
                           "address":'http://localhost:8079/iso18626'
                         ],
                         'message':[
                           "request":[
                             "header":[
                               "requestingAgencyId":[
                                 "agencyIdValue":"DIKUA",
                                 "agencyIdType":[
                                   'value':'RESHARE'
                                 ]
                               ],
                               "supplyingAgencyId":[
                                 "agencyIdValue":"DIKUB",
                                 "agencyIdType":[
                                   'value':'RESHARE'
                                 ]
                               ],
                               'requestingAgencyRequestId':requestToBeProcessed.id
                             ],
                             "bibliographicInfo":[
                                "title":"Platform for Change",
                                "subtitle":"A message from Stafford Beer",
                                "author":"Beer, S"
                             ]
                           ]
                         ]
                       ]);

    log.debug("rabbitService.Send completed");
    return(ActionResponse.IN_PROTOCOL_QUEUE);
    // return(ActionResponse.SUCCESS);
  }
}
