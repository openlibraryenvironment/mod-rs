package org.olf.rs.workflow

import grails.gorm.transactions.Transactional;
import org.olf.rs.PatronRequest;
import org.olf.rs.workflow.AbstractAction.ActionResponse;

@Transactional
class ActionNotSupplyService extends AbstractAction {

  /** Returns the action that this class represents */
  @Override
  String getActionCode() {
    return(Action.NOT_SUPPLY);
  }

  /** Performs the action */
  @Override
  ActionResponse perform(PatronRequest requestToBeProcessed) {
    // For the time being we just return OK as we do not do anything
    return(ActionResponse.SUCCESS);
  }
}
