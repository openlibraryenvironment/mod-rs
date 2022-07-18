package org.olf.rs.lms

public class ItemLocation {

  public String reason
  public String location
  public String shelvingLocation
  public String itemLoanPolicy
  public String callNumber
  public String itemId
  public Long preference
  public Long shelvingPreference

  public String toString() {
    return "ItemLocation(${location} (${preference}),${shelvingLocation} (${shelvingPreference}), ${itemLoanPolicy}, ${callNumber}, ${itemId}, ${reason})".toString()
  }
}
