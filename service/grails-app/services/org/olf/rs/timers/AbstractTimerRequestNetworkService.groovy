package org.olf.rs.timers;

import org.olf.rs.NetworkStatus;
import org.olf.rs.PatronRequest;
import org.olf.rs.ReshareActionService;
import org.olf.rs.ReshareApplicationEventHandlerService;

/**
 * Base class for the nwtwork timers as they essentially need the same fluff
 *
 * @author Chas
 *
 */
public abstract class AbstractTimerRequestNetworkService extends AbstractTimer {

    ReshareActionService reshareActionService;
    ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;

    /** The network status we will be using to find the requests to process */
    private final String networkStatusFilter;

    AbstractTimerRequestNetworkService(NetworkStatus networkStatus) {
        // Need to convert to a string as that is what is stored
        networkStatusFilter = networkStatus.toString();
    }

    /**
     * Does the actual work required for requests in this network state
     * @param request The request that needs reprocessing
     * @param retryEventData The event data that we can retry with
     */
    abstract public void performNetworkTask(PatronRequest request, Map retryEventData);

    @Override
    public void performTask(String config) {
        // We do not want multiple threads in here at the same time, which could happen with the timers being triggered by OKAPI
        synchronized(this) {
            // First of all, get hold of all the requests that we are interested in
            List requests = PatronRequest.findAllByNetworkStatusAndNextSendAttemptLessThan(networkStatusFilter, new Date());

            // Are there any
            if (requests) {
                // Loop through all the requests
                requests.each { request ->
                    // Start a new transaction for this request as we want to treat each request independently
                    PatronRequest.withNewTransaction { transactionStatus ->
                        // Lock the request
                        PatronRequest lockedRequest = reshareApplicationEventHandlerService.delayedGet(request.id, true);

                        // If we failed to lock it, it will be picked up later
                        if (lockedRequest != null) {
                            // Do we have some protocol event data
                            if (lockedRequest.lastProtocolData == null) {
                                // That is a bit of a bummer, have to set the network status to error
                                reshareApplicationEventHandlerService.auditEntry(lockedRequest, lockedRequest.state, lockedRequest.state, 'No lastProtocolData in order to reprocess the request after a network problem (' + networkStatusFilter + ')', null);
                                lockedRequest.networkStatus = NetworkStatus.Error;
                                log.error('Network status timer and no lastProtocolData for request: ' + lockedRequest.id + ', status: ' + networkStatusFilter);
                            } else {
                                // Convert the json string into a Map
                                Map retryEventData = (new groovy.json.JsonSlurper()).parseText(lockedRequest.lastProtocolData);

                                // Now call the abstract method to do its job
                                performNetworkTask(lockedRequest, retryEventData);
                            }

                            // Now we can save the request
                            lockedRequest.save(flush:true, failOnError:true);
                        }
                    }
                }
            }
        }
    }
}
