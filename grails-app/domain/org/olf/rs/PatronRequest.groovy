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

class PatronRequest implements CustomProperties, Taggable, MultiTenant<PatronRequest> {

  // internal ID of the patron request
  String id

  @Defaults(['Book', 'Journal', 'Other'])
  RefdataValue publicationType

  // Title of the item requested
  String title
  String subtitle
  String sponsoringBody
  String publisher
  String placeOfPublication
  String volume
  String issue
  String startPage
  String numberOfPages
  String publicationDate
  String publicationDateOfComponent
  String edition

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
                     
                   dateCreated(nullable:true, blank:false)
                   lastUpdated(nullable:true, blank:false)
               patronReference(nullable:true, blank:false)
                   serviceType(nullable:true, blank:false)
                         state(nullable:true, blank:false)
               publicationType(nullable:true, blank:false)

                         title(nullable:true, blank:false)
                      subtitle(nullable:true, blank:false)
                sponsoringBody(nullable:true, blank:false)
                     publisher(nullable:true, blank:false)
            placeOfPublication(nullable:true, blank:false)
                        volume(nullable:true, blank:false)
                         issue(nullable:true, blank:false)
                     startPage(nullable:true, blank:false)
                 numberOfPages(nullable:true, blank:false)
               publicationDate(nullable:true, blank:false)
    publicationDateOfComponent(nullable:true, blank:false)
                       edition(nullable:true, blank:false)

  }

  static mapping = {
                            id column: 'pr_id', generator: 'uuid', length:36
                       version column: 'pr_version'
                   dateCreated column: 'pr_date_created'
                   lastUpdated column: 'pr_last_updated'
               patronReference column: 'pr_patron_reference'
                   serviceType column: 'pr_service_type_fk'
                         state column: 'pr_state_fk'
               publicationType column: 'pr_pub_type_fk'

                         title column: 'pr_title'
                      subtitle column: 'pr_sub_title'
                sponsoringBody column: 'pr_sponsoring_body'
                     publisher column: 'pr_publisher'
            placeOfPublication column: 'pr_place_of_pub'
                        volume column: 'pr_volume'
                         issue column: 'pr_issue'
                     startPage column: 'pr_start_page'
                 numberOfPages column: 'pr_num_pages'
               publicationDate column: 'pr_pub_date'
    publicationDateOfComponent column: 'pr_pubdate_of_component'
                       edition column: 'pr_edition'

  }

}
