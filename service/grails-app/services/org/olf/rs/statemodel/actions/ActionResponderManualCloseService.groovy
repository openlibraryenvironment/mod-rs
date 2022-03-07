package org.olf.rs.statemodel.actions;

import org.olf.rs.statemodel.Status;

/**
 * Action that performs a close manual for the responder
 * @author Chas
 *
 */
public class ActionResponderManualCloseService extends ActionManualCloseService {

    private static final String[] TO_STATES = [
        Status.RESPONDER_CANCELLED,
        Status.RESPONDER_COMPLETE,
        Status.RESPONDER_NOT_SUPPLIED,
        Status.RESPONDER_UNFILLED
    ];

    @Override
    String[] toStates() {
        return(TO_STATES);
    }
}
