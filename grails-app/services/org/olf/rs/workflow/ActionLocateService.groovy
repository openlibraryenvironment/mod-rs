package org.olf.rs.workflow

import grails.gorm.transactions.Transactional;
import org.olf.rs.PatronRequest;
import org.olf.rs.PatronRequestRota;
import org.olf.rs.workflow.AbstractAction.ActionResponse;
import groovy.util.logging.Slf4j
import org.olf.rs.DirectoryService;

@Slf4j
@Transactional
class ActionLocateService extends AbstractAction {

  DirectoryService directoryService

  /** Returns the action that this class represents */
  @Override
  String getActionCode() {
    return(Action.LOCATE);
  }

  /** Performs the action */
  @Override
  ActionResponse perform(PatronRequest requestToBeProcessed) {
    // For the time being we just return OK as we do not do anything
    log.debug("ActionLocateService::perform(${requestToBeProcessed})");

    if ( requestToBeProcessed.rota.size() == 0 ) { 
      log.debug("Patron request has no rota - find appropriate copies and rank rota according to routing policy");

      // Initially, we are going to mock this out by looking up the directory entry for RESHARE:DIKUA
      requestToBeProcessed.addToRota(new PatronRequestRota(
                                           directoryId:directoryService.getIDForSymbol('RESHARE','DIKUA'), 
                                           rotaPosition:0) );

      requestToBeProcessed.save(flush:true, failOnError:true);
    }
    else {
      log.debug("request already has a rota of size ${requestToBeProcessed.rota.size()}");
    }

    return(ActionResponse.SUCCESS);
  }
}
