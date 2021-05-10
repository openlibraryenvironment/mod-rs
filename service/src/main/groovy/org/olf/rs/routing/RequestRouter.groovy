package org.olf.rs.routing;


/**
 * A request router does the job of finding and ranking possible suppliers for a given item.
 * different implementations can have different characteristics and different critera.
 */
public interface RequestRouter {

  /**
   * Locate more suppliers given the citation - exclude anyone in the list of already_tried_symbols
   */
  List<RankedSupplier> findMoreSuppliers(Map description, List<String> already_tried_symbols);

  Map getRouterInfo();
}

