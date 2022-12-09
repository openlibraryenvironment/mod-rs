package org.olf.rs.lms

class ItemLocation {

    String reason
    String location
    String shelvingLocation
    String temporaryLocation
    String temporaryShelvingLocation
    String itemLoanPolicy
    String callNumber
    String itemId
    Long preference
    Long shelvingPreference

    String toString() {
        return "ItemLocation(${location} (${preference}),${shelvingLocation} (${shelvingPreference}), ${itemLoanPolicy}, ${callNumber}, ${itemId}, ${reason})".toString();
    }
}
