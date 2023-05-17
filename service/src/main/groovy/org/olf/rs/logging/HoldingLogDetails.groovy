package org.olf.rs.logging;

import org.olf.rs.ProtocolMethod;
import org.olf.rs.ProtocolType;
import org.olf.rs.lms.ItemLocation;

import groovy.json.JsonBuilder
import groovy.util.slurpersupport.GPathResult;
import groovy.xml.XmlUtil;

/**
 * Records the different stages of determining what is considered the best location
 * @author Chas
 *
 */
public class HoldingLogDetails extends BaseAuditDetails implements IHoldingLogDetails {

    private static final String KEY_BEST_AVAILABLE_LOCATION = "bestAvailableLocation";
    private static final String KEY_AVAILABLE_LOCATIONS     = "availableLocations";
    private static final String KEY_DURATION                = "duration";
    private static final String KEY_NUMBER_OF_RECORDS       = "numberOfRecords";
    private static final String KEY_RECORDS                 = "records";
    private static final String KEY_SEARCH_REQUEST          = "searchRequest";
    private static final String KEY_SEARCHES                = "searches";
    private static final String KEY_URL                     = "url";

    /** The map that holds the results of attempting to find the best item */
    private Map logDetails = [ : ];

    /** The searches that have been performed */
    private List searches = null;

    /** The search we are currently processing */
    private Map currentSearch = null;

    /** The record we are currently processing */
    private List records = null;

    /** Contains the holdings associated with the current record */
    private List recordHoldings = null;

    public HoldingLogDetails(ProtocolType protocolType, ProtocolMethod protocolMethod) {
        this.protocolType = protocolType;
        this.protocolMethod = protocolMethod;
        searches = [ ];
        logDetails.put(KEY_SEARCHES, searches);
    }

    @Override
    public void newSearch(String url) {
        currentSearch = [ : ];
        searches.add(currentSearch);
        currentSearch.put(KEY_URL, url);
        this.url = url;
    }

    @Override
    public void numberOfRecords(Long numberOfRecords) {
        // If we have been passed null treat it as 0
        if (numberOfRecords == null) {
            numberOfRecords = 0;
        }
        currentSearch.put(KEY_NUMBER_OF_RECORDS, numberOfRecords);
        if (numberOfRecords > 0) {
            records = [ ];
            currentSearch.put(KEY_RECORDS, records);
        }
    }

    @Override
    public void searchRequest(GPathResult searchRequest) {
        if (searchRequest != null) {
            // Convert to a string
            String searchRequestString = XmlUtil.serialize(searchRequest);
            currentSearch.put(KEY_SEARCH_REQUEST, searchRequestString);
        }
    }

    @Override
    public void newRecord() {
        recordHoldings = [ ];
        records.add(recordHoldings);
    }

    @Override
    public void holdings(GPathResult holdings) {
        if (holdings != null) {
            // Convert to a string
            String holdingsString = XmlUtil.serialize(holdings);
            recordHoldings.add(holdingsString);
        }
    }

    @Override
    public void availableLocations(List<ItemLocation> availableLocations) {
        if (availableLocations != null) {
            logDetails.put(KEY_AVAILABLE_LOCATIONS, availableLocations);
        }
    }

    @Override
    public void availableLocations(ItemLocation availableLocation) {
        if (availableLocation != null) {
            availableLocations([ availableLocation ]);
        }
    }

    @Override
    public void bestAvailableLocation(ItemLocation bestAvailableLocation) {
        if (bestAvailableLocation != null) {
            logDetails.put(KEY_BEST_AVAILABLE_LOCATION, bestAvailableLocation);
        }
    }

    @Override
    public Map toMap() {
        addDuration();
        return(logDetails);
    }

    @Override
    public String toString() {
        addDuration();
        return(new JsonBuilder(logDetails).toPrettyString());
    }

    private void addDuration() {
        logDetails.put(KEY_DURATION, duration());
    }
}
