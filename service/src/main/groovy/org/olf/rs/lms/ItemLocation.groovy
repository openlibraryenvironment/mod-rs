package org.olf.rs.lms

public class ItemLocation {

  public String reason
  public String location
  public String shelvingLocation
  public String callNumber
  public Long preference

  public String toString() {
    return "ItemLocation(${location},${shelvingLocation},${callNumber},${preference},${reason})".toString()
  }
}
