package org.olf.rs

import com.k_int.okapi.OkapiClient
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import groovy.json.JsonSlurper

/**
 * This service has responsibility for taking a patron request and creating a viable list of
 * possible responders. 
 * 
 * this service hides the specific implementations of the configured local request routing strategy
 * this may be a looking in a shared index, a static list of responders or some other strategy
 */
public class RequestRouterService {

  public void findMoreSuppliers() {
    // Initially this method needs to hide the details of what happens in the following calls
    // around line 222 of ReshareApplicationEventHandlerService.groovy. Our aim is to hide the 
    // details of finding a list of potential suppliers so different implementations can be used

    // List<AvailabilityStatement> sia = sharedIndexService.getSharedIndexActions().findAppropriateCopies(req.getDescriptiveMetadata());
    // log.debug("Result of shared index lookup : ${sia}");
    // int ctr = 0;
    // List<Map> enrichedRota = createRankedRota(sia)
    // log.debug("Created ranked rota: ${enrichedRota}")
  }


}
