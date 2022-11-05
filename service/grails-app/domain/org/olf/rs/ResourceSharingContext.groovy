package org.olf.rs

import grails.gorm.MultiTenant;

/**
 * Describe a resource sharing context, for example "The PALCI network", "MOBIUS", "British Library".
 *
 * Presently, ReShare can only express 1 shared index globally for the entire system. this is unsatisfactory.
 * What happens when we are unable to fulfil a request in our default initial context but want to fall back to
 * secondary and then "Last resort" lenders. Some of these secondary contexts may not support shared indexes,
 * some may require special workflows or procedures. This class describes a resource sharing context. It is
 * used in conjunction with the custom property "RSContextPreference" from the directory.
 *
 * When a new patron request arrives, the intent is to look up RSContextPreference for the patrons borrowing
 * institution. If RSContextPreference is not set, everything continues as before with ReShare and the system wide
 * shared index.
 *
 * If, however, RSContextPreference is present it is parsed with a ',' separator in order to give a priority list.
 * it is anticipated that a new procedure for processing new patron requests will be invoked for the RSContextPreference
 * use case. In this case, request processing can happen for multiple contexts each one falling back to
 * a less desirable one until a request is filled, or end of rota.
 *
 * context - string ID of the conext - e.g. "MOBIUS", "PALCI", "BL"
 * sharedIndexType - analagous to the existing string identifier for shared index implementation - used to derive a spring bean
 *                 - It may be possble/desireable to define and pass parameters to the SI Impl here
 *
 * protocol - OPTIONAL an indication (But not a mandate) that a context perfers a particular protocol.
 *          - not a mandate in case 2 institutions decide to prefer a higher fidelity protocol as a reciprocal agreement.
 *
 */
class ResourceSharingContext implements MultiTenant<ResourceSharingContext> {

  String id
  String context
  String sharedIndexType
  String protocol

  static constraints = {
            context (nullable : false, blank: false, unique: true)
    sharedIndexType (nullable : false, blank: false)
           protocol (nullable : true, blank:false)
  }

  static mapping = {
    id                     column : 'rsc_id', generator: 'uuid2', length:36
    version                column : 'rsc_version'
    context                column : 'rsc_context'
    sharedIndexType        column : 'rsc_shared_index_type'
    protocol               column : 'rsc_protocol'
  }

}
