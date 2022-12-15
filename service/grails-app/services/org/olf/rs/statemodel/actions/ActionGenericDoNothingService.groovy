package org.olf.rs.statemodel.actions;

import org.olf.rs.PatronRequest;
import org.olf.rs.statemodel.AbstractAction;
import org.olf.rs.statemodel.ActionResultDetails;

/**
 * A generic action service that does nothing
 * @author Chas
 *
 */
public class ActionGenericDoNothingService extends AbstractAction {

    @Override
    String name() {
        // Could be hooked to from multiple actions, so we just call it GenericDoNothing
        return("GenericDoNothing");
    }

    @Override
    ActionResultDetails performAction(PatronRequest request, Object parameters, ActionResultDetails actionResultDetails) {

        // Just return the action result details as supplied
        return(actionResultDetails);
    }
}
