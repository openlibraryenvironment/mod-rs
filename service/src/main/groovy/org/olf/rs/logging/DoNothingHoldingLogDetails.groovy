package org.olf.rs.logging;

import org.olf.rs.lms.ItemLocation;

import groovy.util.slurpersupport.GPathResult;

/**
 * Implements the IHoldingLogDetails interface by not recording any details
 * @author Chas
 *
 */
public class DoNothingHoldingLogDetails extends BaseAuditDetails implements IHoldingLogDetails {

    @Override
    public void newSearch(String url) {
    }

    @Override
    public void numberOfRecords(Long numberOfRecords) {
    }

    @Override
    public void searchRequest(GPathResult searchRequest) {
    }

    @Override
    public void newRecord() {
    }

    @Override
    public void holdings(GPathResult holdings) {
    }

    @Override
    public void availableLocations(List<ItemLocation> availableLocations) {
    }

    @Override
    public void availableLocations(ItemLocation availableLocation) {
    }

    @Override
    public void bestAvailableLocation(ItemLocation bestAvailableLocation) {
    }

    @Override
    public Map toMap() {
        return(null);
    }

    @Override
    public String toString() {
        return(null);
    }
}
