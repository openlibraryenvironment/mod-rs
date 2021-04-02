package org.olf.rs.routing;

import org.olf.rs.routing.RequestRouter;
import org.olf.rs.routing.RankedSupplier;

public class StaticRouterService implements RequestRouter {

  public List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols) {
    String parsed_list = 'ISIL:RST1,ISIL:RST2,ISIL:RST3'
    List<RankedSupplier> result = []
    parsed_list.split(',').each {
      result.add(new RankedSupplier(supplier_symbol:it.trim(), copy_identifier:'1234', ill_policy:'loan', rank:0, rankReason:'static rank'));
    }
    return result
  }

}
