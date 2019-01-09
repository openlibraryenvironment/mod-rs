package org.olf.rs

import javax.persistence.Transient
import grails.databinding.BindInitializer
import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.refdata.Defaults

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class PatronRequest implements MultiTenant<PatronRequest> {

  String id
  String title

  // serviceType - added here as an example refdata item - more to show how than
  // arising from analysis and design
  @Defaults(['Loan', 'Copy-non-returnable'])
  RefdataValue serviceType


  static constraints = {
  }

  static mapping = {
             id column: 'pr_id', generator: 'uuid', length:36
    serviceType column: 'pr_service_type_fk'
  }

}
