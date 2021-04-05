package org.olf.rs.routing;

import org.olf.rs.routing.RequestRouter;
import org.olf.rs.routing.RankedSupplier;
import com.k_int.web.toolkit.settings.AppSetting

public class StaticRouterService implements RequestRouter {

  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {

    log.debug("StaticRouterService::findMoreSuppliers(${description},${already_tried_symbols})");
    List<RankedSupplier> result = []

    AppSetting static_routing_cfg = AppSetting.findByKey('static_routes')
    String static_routing_str = static_routing_cfg?.value
    if ( static_routing_str ) {
      String[] components = static_routing_str.split(',')  // TYPE:NAMESPACE:SYMBOL,TYPE:NAMESPACE:SYMBOL
      components.each { static_option ->
        String[] option_parts = static_option.split(':');
        if ( option_parts.size() == 3 ) {
          if ( option_parts[0] == 'SYMBOL' ) {
            result.add(new RankedSupplier(supplier_symbol:"${option_parts[1]}:${option_parts[2]}", 
                                          copy_identifier:'', 
                                          instance_identifier:'', 
                                          ill_policy:'loan', 
                                          rank:0, 
                                          rankReason:'static rank'));
          }
          else {
            log.warn("Unhandled static routing option: ${option_parts[0]} in ${static_option} of ${static_routing_str}");
          }
        }
        else {
          log.warn("Unable to parse TYPE:NAMESPACE:VALUE triple in ${static_option} of ${static_routing_str}");
        }
      }
    }
    else {
      log.warn("Unable to locate static routing config option (static_routes) - should be formatted as a comma separated string of options");
    }

    log.debug("StaticRouterService::findMoreSuppliers returns ${result}");
    return result
  }

}
