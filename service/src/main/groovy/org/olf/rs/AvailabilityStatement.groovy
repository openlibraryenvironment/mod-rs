package org.olf.rs

public class AvailabilityStatement {

  public String symbol
  public String instanceIdentifier
  public String copyIdentifier
  public String illPolicy
  public Long totalCopies
  public Long availableCopies


  public String toString() {
    return "AvailabilityStatement(${symbol},${instanceIdentifier},${copyIdentifier},${illPolicy})".toString()
  }
}
