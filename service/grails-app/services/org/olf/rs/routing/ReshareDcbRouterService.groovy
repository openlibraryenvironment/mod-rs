package org.olf.rs.routing;

import org.olf.rs.routing.RequestRouter;
import org.olf.rs.routing.RankedSupplier;
import com.k_int.web.toolkit.settings.AppSetting;
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import org.olf.rs.DirectoryEntryService
import org.olf.rs.SharedIndexActions;
import org.olf.rs.AvailabilityStatement;


public class ReshareDcbRouterService implements RequestRouter {

  DirectoryEntryService directoryEntryService

  // Grab the right implementation - in test return the Mock, in live return a real service
  @Autowired
  SharedIndexActions reshareDcbSharedIndexService;


  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {

    log.debug("ReshareDcbRouterService::findMoreSuppliers(${description},${already_tried_symbols})");
    List<RankedSupplier> result = []


    log.debug("reshareDcbSharedIndexService : ${reshareDcbSharedIndexService} ${reshareDcbSharedIndexService?.class?.name}");

    // Probably we should iterate around the list of resource sharing contexts for the requesting location.... until then
    List<AvailabilityStatement> availability = reshareDcbSharedIndexService.findAppropriateCopies(description);

    log.debug("Got DCB availability: ${availability}");

    // our pretend rota consists of 2 locations 
    ['ISIL:DST2', 'ISIL:DST3'].each { option ->
      String[] option_parts = option.split(':');
      Symbol s = null;
      String symbolString = null;

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
        result.add(new RankedSupplier(
                                      supplier_symbol:symbolString,
                                      copy_identifier:'', 
                                      instance_identifier:'',
                                      ill_policy:'Will lend',
                                      rank:0, 
                                      rankReason:'static rank'));

      }
    }

    log.debug("ReshareDcbRouterService::findMoreSuppliers returns ${result}");
    return result
  }

  public Map getRouterInfo() {
    return [
      'name':'ReshareDcbRouterService',
      'description': 'Reshare DCB Routing Service'
    ]
  }

}
