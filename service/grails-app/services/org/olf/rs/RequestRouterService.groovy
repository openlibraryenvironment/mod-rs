package org.olf.rs

import com.k_int.okapi.OkapiClient
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import groovy.json.JsonSlurper
import org.olf.rs.routing.RequestRouter;
import com.k_int.web.toolkit.settings.AppSetting
import org.olf.rs.routing.RankedSupplier

/**
 * Has responsibility for taking a citation and creating a viable list of possible responders given the requesting library preferences. 
 * 
 * This service hides the specific implementations of the configured local request routing strategy
 * this may be a looking in a shared index, a static list of responders or some other strategy. It is REALLY
 * important to keep in mind that we are multi-tenant here. Ordinarily with spring we would configure an implementation
 * and use it directly - here the implementation varies with the tenant, so this class is essentially
 * the pivot which allows one tenant to use the StaticRouter whilst another uses the FOLIO Shared Index Router
 *
 */
public class RequestRouterService {

  GrailsApplication grailsApplication

  public RequestRouter getRequestRouterFor(String impl) {
    log.debug("RequestRouterService::getRequestRouterFor(${impl})");
    RequestRouter result = grailsApplication.mainContext."${impl}RouterService"

    if ( result == null ) {
      log.warn("Unable to locate RequestRouter for ${impl}. Did you fail to configure the app_setting \"routing_adapter\". Current options are FOLIOSharedIndex|Static");
    }

    return result;
  }

  public RequestRouter getRequestRouter() {
    RequestRouter result = null;
    AppSetting request_router_setting = AppSetting.findByKey('routing_adapter');
    String v = request_router_setting?.value
    log.debug("Return host router for : ${v} - query application context for bean named ${v}RouterService");
    result = getRequestRouterFor(v);
    return result;
  }

  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {
    // Initially this method needs to hide the details of what happens in the following calls
    // around line 222 of ReshareApplicationEventHandlerService.groovy. Our aim is to hide the 
    // details of finding a list of potential suppliers so different implementations can be used
    List<RankedSupplier> supplier_list = getRequestRouter().findMoreSuppliers(description,already_tried_symbols);

    // Currently, ranking is done in the FolioRouter really the ranking algorithm should also be seperated out and called
    // here so that different supplier list generators can be ranked by different ranking algorithms.

    return supplier_list
  }


}
