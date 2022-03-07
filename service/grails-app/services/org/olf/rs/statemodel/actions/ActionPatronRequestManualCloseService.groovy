package org.olf.rs.statemodel.actions;

import org.olf.rs.statemodel.Status;

/**
 * Action that performs a manual close for the patron request
 * @author Chas
 *
 */
public class ActionPatronRequestManualCloseService extends ActionManualCloseService {

    private static final String[] TO_STATES = [
        Status.PATRON_REQUEST_CANCELLED,
        Status.PATRON_REQUEST_END_OF_ROTA,
        Status.PATRON_REQUEST_FILLED_LOCALLY,
        Status.PATRON_REQUEST_REQUEST_COMPLETE
    ];

    @Override
    String[] toStates() {
        return(TO_STATES);
    }
}
