package org.olf.rs.shared

import javax.persistence.Transient
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class TenantSymbolMapping implements MultiTenant<TenantSymbolMapping> {

  String id
  String symbol
  String tenant

  static mapping = {
                  id column : 'tsm_id', generator: 'uuid2', length:36
             version column : 'tsm_version'
              symbol column : 'tsm_symbol'
              tenant column : 'tsm_tenant'
  }
}

