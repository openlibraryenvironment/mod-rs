package org.olf.rs

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.ActionService;

import com.k_int.web.toolkit.SimpleLookupService;

/**
 * This service handles everything to do with a batch
 * A batch is just a bundles of objects grouped together for a particular reason (eg. patron requests grouped together for printing)
 */
public class BatchService {

    /** The number of items we request at a time from the lookup service */
    private static final int NUMBER_PER_PAGE = 100;

    /** The formatter to use for turning the date / time into a string to append to the description */
    private static final DateTimeFormatter batchDateFormat = DateTimeFormatter.ofPattern(" E d MMM y kk:mm:ss");

    /** Required for marking the request as printed */
    ActionService actionService;

    /** for getting hold of the locale settings */
    OkapiSettingsService okapiSettingsService;

    /** Performs the query against the specified domain */
    SimpleLookupService simpleLookupService;

    /**
     * Generates a batch from the supplied filter details
     * @param term The term that is being used for the search
     * @param searchFields The fields to be searched for the supplied term
     * @param filters The filters to be applied to the search
     * @param maxBatchSize The maximum size that that the batch is allowed to be
     * @param description The description for the batch
     * @param appendDateTime Dhould the current date / time be appended to the batch
     * @return A map containing any error that occurred or the batch identifier
     */
    public Map generatePickListBatchFromFilter(
        String term,
        List<String> searchFields,
        List<String> filters,
        int maxBatchSize,
        String description,
        boolean appendDateTime)
    {
        Map result = [ : ];
        int page = 1;
        List patronRequests = [ ];
        boolean continuePageing = true;


        // Keep looping until we hit an error or we have no more results
        while (continuePageing) {
            // Search for the requests
            List pagedResults = simpleLookupService.lookup(PatronRequest, term, NUMBER_PER_PAGE, page, filters, searchFields);

            // Did we find any
            if (pagedResults.size() == 0) {
                // No we did not so no need to continue
                continuePageing = false;
            } else {
                // Add all of the results to the list
                patronRequests.addAll(pagedResults);

                // Do we have to many items
                if (patronRequests.size() > maxBatchSize) {
                    // We do so set the error and bail out
                    result.error = "Too many items to be printed, extend the filter to reduce the number of requests selected";
                    continuePageing = false;
                } else {
                    // Move on to the next page of results
                    page++;
                }
            }
        }

        // Now create the batch
        createBatch(description, appendDateTime, patronRequests, result);

        // Finally return the result to the caller
        return(result);
    }

    /**
     * Generates a pick list batch with the supplied patron requests
     * @param patronRequests The patron requests that are to be part of the batch
     * @param description The description for the batch
     * @param appendDateTime Do we append the current date / time to the description
     * @return a map containing an error or the generated batch identifier
     */
    public Map generatePickListBatchFromList(
        List patronRequests,
        String description,
        boolean appendDateTime,
        boolean wantBatch = false)
    {
        Map result = [ : ];

        // Just create the batch
        createBatch(description, appendDateTime, patronRequests, result, wantBatch);

        // Finally return the result to the caller
        return(result);
    }

    /**
     * Marks all the requests in the batch as being printed if it is valid for the request
     * @param batch The batch that the requests should be marked as being printed
     * @return A map that contains 3 arrays of request identifiers that says whether that request was a success, failed or the action is invalid for the request
     */
    public Map markRequestsInBatchAsPrinted(Batch batch) {
        List successful = [ ];
        List failed = [ ];
        List notValid = [ ];
        Map result = [
            successful : successful,
            failed : failed,
            notValid : notValid
        ];

        // Can't do anything without a batch
        if (batch) {
            // For each request we need to execute the action
            batch.patronRequests.each { PatronRequest patronRequest ->
                // Do we have an action to perform
                if (patronRequest.stateModel.pickSlipPrintedAction != null) {
                    // Is the action valid for the request
                    if (actionService.isValid(patronRequest, patronRequest.stateModel.pickSlipPrintedAction.code)) {
                        // It is valid, so execute the action
                        Map actionServiceResult = actionService.executeAction(patronRequest.id, patronRequest.stateModel.pickSlipPrintedAction.code, null);
                        if (actionServiceResult.actionResult == ActionResult.SUCCESS) {
                            // Add it to the successful array
                            successful.add(patronRequest.id);
                        } else {
                            // We failed so add it to the failed array
                            failed.add(patronRequest.id);
                        }
                    } else {
                        // Add the request id to the not valid array
                        notValid.add(patronRequest.id);
                    }
                } else {
                    // Add the request id to the not valid array
                    notValid.add(patronRequest.id);
                }
            }
        }

        // Return the result to the caller
        return(result);
    }

    /**
     * Fetches a list of the request identifiers that are contained within a batch
     * @param batchId Thw batch identifier that the request identifiers are required for
     * @return The list of request identifiers within the batch
     */
    public List fetchRequestIdentifiersForBatch(String batchId) {
        List requestIdentifiers = [ ];

        // Lookup the batch
        Batch batch = Batch.get(batchId);

        // Did we find it
        if (batch != null) {
            // We did so run through all the requests adding the id to the array
            batch.patronRequests.each { PatronRequest request ->
                requestIdentifiers.add(request.id);
            }
        }

        // Return the found request identifiers to the caller
        return(requestIdentifiers);
    }

    /**
     * Create the batch containing the specified patron requests
     * @param description The description for the batch
     * @param appendDateTime Do we append the description with the current date / time
     * @param patronRequests The list of patron requests to be held within the batch
     * @param result The result map that will be updated with the batch id if we were successful
     */
    private void createBatch(String description, boolean appendDateTime, List patronRequests, Map result, boolean wantBatch = false) {
        // Have we been supplied with any identifiers
        if ((patronRequests == null) || patronRequests.isEmpty()) {
            // We did not find any
            result.error = "No requests found to include in pick list";
        } else {
            // We did, but did we have an error while locating them
            if (result.error == null) {
                // No error so lets create the batch
                String dateTime = "";

                // Do we want to append the date / tim to the description
                if (appendDateTime) {
                    // Get hold of the locale settings
                    Map localeSettings = okapiSettingsService.getLocaleSettings();

                    // Now let us attempt to get hold of the time zone for the tenant, we default to UTC if we cannot determine one
                    ZoneId zoneId = ZoneId.of((localeSettings ==  null) ? "Z" : localeSettings.timezone);
                    if (zoneId == null) {
                        // Default to UTC
                        zoneId = ZoneId.of("Z");
                    }

                    // Now format the current date / time
                    dateTime = ZonedDateTime.now(zoneId).format(batchDateFormat);
                }

                Batch batch = new Batch();
                batch.context = batch.CONTEXT_PULL_SLIP;
                batch.description = description + dateTime;

                // We just look at the first request to see if it is a requester batch or not
                batch.isRequester = patronRequests[0].isRequester;

                // Add each request
                patronRequests.each { PatronRequest patronRequest ->
                    batch.addToPatronRequests(patronRequest);
                }

                // Now we can save it
                batch.save(flush: true);

                // Return the batch id to the caller
                result.batchId = batch.id;

                // Do they want the batch passing back
                if (wantBatch) {
                    // They do, so set the batch
                    result.batch = batch;
                }
            }
        }
    }
}
