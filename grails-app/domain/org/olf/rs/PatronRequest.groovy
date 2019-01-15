package org.olf.rs

import javax.persistence.Transient
import grails.databinding.BindInitializer
import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.tags.Taggable
import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.refdata.Defaults

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

// class PatronRequest implements CustomProperties,Taggable,MultiTenant<PatronRequest> {
class PatronRequest implements CustomProperties, Taggable, MultiTenant<PatronRequest> {

  // internal ID of the patron request
  String id

  // Title of the item requested
  String title

  // Patron reference (EG Barcode)
  String patronReference

  // These 2 dates are maintained by the framework for us
  Date dateCreated
  Date lastUpdated

  // serviceType - added here as an example refdata item - more to show how than
  // arising from analysis and design
  @Defaults(['Loan', 'Copy-non-returnable'])
  RefdataValue serviceType

  // Status
  @Defaults(['Idle', 'Approved', 'Pending', 'Cancelled', 'Shipped', 'Awaiting Collection', 'Filfilled', 'Unfilled'])
  RefdataValue state

  static constraints = {
  }

  static mapping = {
                 id column: 'pr_id', generator: 'uuid', length:36
              title column: 'pr_title'
            version column: 'pr_version'
        dateCreated column: 'pr_date_created'
        lastUpdated column: 'pr_last_updated'
    patronReference column: 'pr_patron_reference'
        serviceType column: 'pr_service_type_fk'
              state column: 'pr_state_fk'
  }

}
