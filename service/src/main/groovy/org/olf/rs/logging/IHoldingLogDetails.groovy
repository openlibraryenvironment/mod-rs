package org.olf.rs.logging;

import org.olf.rs.lms.ItemLocation;

import groovy.util.slurpersupport.GPathResult;

/**
 * Interface for recording what we do when we search for item availability, by default we do nothing
 * @author Chas
 *
 */
public interface IHoldingLogDetails extends IBaseAuditDetails {

    /**
     * A new search is being performed to the supplied url
     * @param url The url used to perform the search
     */
    void newSearch(String url);

    /**
     * Records the number of records returned by the search
     * @param numberOfRecords The number of records found by the search
     */
    void numberOfRecords(Long numberOfRecords);

    /**
     * Records the search request returned in the search result
     * @param searchRequest The search request that contains the details of the search as understood by the server
     */
    void searchRequest(GPathResult searchRequest);

    /**
     * A new record in the search results is being processed
     */
    void newRecord();

    /**
     * A new holdings record is being processed
     * @param holdings The holdings now being processed
     */
    void holdings(GPathResult holdings);

    /**
     * Records the list of available locations that were found from the holdings
     * @param availableLocations The available locations that were found
     */
    void availableLocations(List<ItemLocation> availableLocations);

    /**
     * Records an available location that was found
     * @param availableLocation The available location that was found
     */
    void availableLocations(ItemLocation availableLocation);

    /**
     * Records the best available location that was found
     * @param bestAvailableLocation The best available location
     */
    void bestAvailableLocation(ItemLocation bestAvailableLocation);
}
