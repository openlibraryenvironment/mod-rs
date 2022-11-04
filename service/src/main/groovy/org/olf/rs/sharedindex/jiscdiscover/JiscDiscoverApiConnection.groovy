package org.olf.rs.sharedindex.jiscdiscover

// N.B. Repackaging in upcoming groovy update
// import groovy.xml.slurpersupport.GPathResult;

import groovy.util.slurpersupport.GPathResult;


/**
 * In order to make the JiscDiscoverSharedIndex service testable we need to be able to mock out
 * the connection to that remote web service. This interface is the contract that
 * grails-app/services/org/olf/rs/sharedindex/JiscDiscoverSharedIndexService.groovy
 * expects to be injected by spring. In a test environment a mocked implementation will be provided
 */
public interface JiscDiscoverApiConnection {
  
  public GPathResult getSru(Map description);
  
}
