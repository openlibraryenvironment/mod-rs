package org.olf.rs.routing;

import org.olf.rs.AvailabilityStatement;
import org.olf.rs.routing.RequestRouter;
import org.olf.rs.routing.RankedSupplier;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import org.olf.rs.DirectoryEntryService

public class StaticRouterService implements RequestRouter {
  DirectoryEntryService directoryEntryService

  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {

    log.debug("StaticRouterService::findMoreSuppliers(${description},${already_tried_symbols})");
    List<RankedSupplier> result = []

    AppSetting static_routing_cfg = AppSetting.findByKey('static_routes')
    String static_routing_str = static_routing_cfg?.value
    if ( static_routing_str ) {
      String[] components = static_routing_str.split(',')  // TYPE:NAMESPACE:SYMBOL,TYPE:NAMESPACE:SYMBOL
      log.debug("Found components ${components}")
      components.each { static_option ->
        String[] option_parts = static_option.split(':');
        Symbol s
        String symbolString = ''

        if ( option_parts.size() == 2 ) {
          // We assume SYMBOL if only 2 parts
          s = directoryEntryService.resolveSymbol(option_parts[0], option_parts[1]);
          symbolString = "${option_parts[0]}:${option_parts[1]}" 
        }
        else if ( option_parts.size() == 3 ) {
          if ( option_parts[0] == 'SYMBOL' ) {
            s = directoryEntryService.resolveSymbol(option_parts[1], option_parts[2]);
            symbolString = "${option_parts[1]}:${option_parts[2]}" 
          }
          else {
            log.warn("Unhandled static routing option: ${option_parts[0]} in ${static_option} of ${static_routing_str}");
          }
        }
        else {
          log.warn("Unable to parse TYPE:NAMESPACE:VALUE triple in ${static_option} of ${static_routing_str}");
        }

        // At this point we should have a Symbol s we can use to add entries to the rota
        if (s && directoryEntryService.directoryEntryIsLending(s.owner)) {
          result.add(new RankedSupplier(supplier_symbol:symbolString,
                                        copy_identifier:'', 
                                        instance_identifier:'',
                                        ill_policy: AvailabilityStatement.LENDABLE_POLICY,
                                        rank:0, 
                                        rankReason:'static rank'));

        }
      }
    }
    else {
      log.warn("Unable to locate static routing config option (static_routes) - should be formatted as a comma separated string of options");
    }

    log.debug("StaticRouterService::findMoreSuppliers returns ${result}");
    return result
  }

  public Map getRouterInfo() {
    return [
      'name':'StaticRouterService',
      'description': 'Static Routing Service'
    ]
  }

}
