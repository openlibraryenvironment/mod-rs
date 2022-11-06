package org.olf.rs.sharedindex.jiscdiscover

/**
 * In order to make the JiscDiscoverSharedIndex service testable we need to be able to mock out
 * the connection to that remote web service. This interface is the contract that
 * grails-app/services/org/olf/rs/sharedindex/JiscDiscoverSharedIndexService.groovy
 * expects to be injected by spring. In a test environment a mocked implementation will be provided
 */
public interface JiscDiscoverApiConnection {
  
  public Object getSru(Map description);
  
}
