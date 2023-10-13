package org.olf.rs

public class AvailabilityStatement {

  public String symbol
  public String instanceIdentifier
  public String copyIdentifier
  public String illPolicy
  public Long totalCopies
  public Long availableCopies

  public static final String LENDABLE_POLICY = 'Will lend';

  public String toString() {
    return "AvailabilityStatement(${symbol},${instanceIdentifier},${copyIdentifier},${illPolicy})".toString()
  }
}
