package org.olf.rs.timers;

import org.olf.rs.NetworkStatus;
import org.olf.rs.PatronRequest;

/**
 * Checks to see if There are any requests we need to retry
 *
 * @author Chas
 *
 */
public class TimerRequestNetworkRetryService extends AbstractTimerRequestNetworkService {

    TimerRequestNetworkRetryService() {
        super(NetworkStatus.Retry);
    }

    @Override
    public void performNetworkTask(PatronRequest request, Map retryEventData) {
        // Now lets us attempt to resend
        if (request.isRequester) {
            // We do not care about the result as it is all handled in the call, action and message params can be null
            reshareActionService.sendRequestingAgencyMessage(request, null, null, null, retryEventData);
        } else {
            // We do not care about the result as it is all handled in the call, action, status and message params can be null
            reshareActionService.sendSupplyingAgencyMessage(request, null, null, null, null, retryEventData);
        }
    }
}
