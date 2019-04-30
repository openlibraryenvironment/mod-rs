package org.olf.rs

import javax.persistence.Transient
import grails.databinding.BindInitializer
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.refdata.Defaults
import org.olf.rs.workflow.Action;
import org.olf.rs.workflow.ReShareMessageService
import org.olf.rs.workflow.Status;
import com.k_int.web.toolkit.tags.Tag

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class PatronRequest implements CustomProperties, MultiTenant<PatronRequest> {

  // internal ID of the patron request
  String id

  @Defaults(['Book', 'Journal', 'Other'])
  RefdataValue publicationType

  // Title of the item requested
  String title
  String author
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

  /** Is the this the requester or suppliers view of the request */
  Boolean isRequester;
  
  // Status
  Status state

  /** The number of retries that have occurred, */
  Integer numberOfRetries;

  /** Delay performing the action until this date / time arrives */
  Date delayPerformingActionUntil;

  /** The action waiting to be performed on this request */
  Action pendingAction;

  /** If we hit an error this is the action we were trying to perform */
  Action errorAction;

  /** If we hit an error this was the status prior to the error occurring */
  Status preErrorStatus;

  /** Are we waiting for a protocol message to be sent */
  boolean awaitingProtocolResponse;

  /** The position we are in the rota */
  Long rotaPosition;

  /** Lets us know the if the system has updated the record, as we do not want validation on the pendingAction field to happen as it has already happened */
  boolean systemUpdate = false;
  static transients = ['systemUpdate'];

  // The audit of what has happened to this request and tags that are associated with the request */
  static hasMany = [
          audit : PatronRequestAudit,
          rota  : PatronRequestRota,
          tags  : Tag];

  static mappedBy = [
    rota: 'patronRequest',        
    audit: 'patronRequest'
  ]

  static constraints = {
                     
                   dateCreated (nullable : true, bindable: false)
                   lastUpdated (nullable : true, bindable: false)
               patronReference (nullable : true)
                   serviceType (nullable : true)
                         state (nullable : true, bindable: false)
                   isRequester (nullable : true, bindable: false)
               numberOfRetries (nullable : true, bindable: false)
    delayPerformingActionUntil (nullable : true, bindable: false)
                 pendingAction (nullable : true, pendingAction : true)
                   errorAction (nullable : true, bindable: false)
                preErrorStatus (nullable : true, bindable: false)
      awaitingProtocolResponse (bindable: false)
                  rotaPosition (bindable: false)
               publicationType (nullable : true)

                         title (nullable : true, blank : false)
                        author (nullable : true, blank : false)
                      subtitle (nullable : true, blank : false)
                sponsoringBody (nullable : true, blank : false)
                     publisher (nullable : true, blank : false)
            placeOfPublication (nullable : true, blank : false)
                        volume (nullable : true, blank : false)
                         issue (nullable : true, blank : false)
                     startPage (nullable : true, blank : false)
                 numberOfPages (nullable : true, blank : false)
               publicationDate (nullable : true, blank : false)
    publicationDateOfComponent (nullable : true, blank : false)
                       edition (nullable : true, blank : false)

  }

  static mapping = {
                            id column : 'pr_id', generator: 'uuid2', length:36
                       version column : 'pr_version'
                   dateCreated column : 'pr_date_created'
                   lastUpdated column : 'pr_last_updated'
               patronReference column : 'pr_patron_reference'
                   serviceType column : 'pr_service_type_fk'
                         state column : 'pr_state_fk'
                 isRequester column : "pr_is_requester"
             numberOfRetries column : 'pr_number_of_retries'
  delayPerformingActionUntil column : 'pr_delay_performing_action_until'
          pendingAction column : 'pr_pending_action_fk'
           errorAction column : 'pr_error_action_fk'
          preErrorStatus column : 'pr_pre_error_status_fk'
      awaitingProtocolResponse column : 'pr_awaiting_protocol_response'
                  rotaPosition column : 'pr_rota_position'
               publicationType column : 'pr_pub_type_fk'

                         title column : 'pr_title'
                        author column : 'pr_author'
                      subtitle column : 'pr_sub_title'
                sponsoringBody column : 'pr_sponsoring_body'
                     publisher column : 'pr_publisher'
            placeOfPublication column : 'pr_place_of_pub'
                        volume column : 'pr_volume'
                         issue column : 'pr_issue'
                     startPage column : 'pr_start_page'
                 numberOfPages column : 'pr_num_pages'
               publicationDate column : 'pr_pub_date'
    publicationDateOfComponent column : 'pr_pubdate_of_component'
                       edition column : 'pr_edition'

  }

  /**
   * If this is a requester request we are inserting then set the pending action to be VALIDATE
   */
  def beforeInsert() {
    // Are we a requester
    if (isRequester) {
      // Set the pending action to be validate
      pendingAction = Action.get(Action.VALIDATE);
    }

    // Status needs to be set to idle
    state = Status.get(Status.IDLE);    
  }

  

  /**
   * II :: This has been replaced by ReShareMessageService::afterInsert GORM event to aleviate the need for
   *       statics in service classes
   * Perform checks to see if needs adding to the reshare queue  
   * def afterInsert() {
   *   ReShareMessageService.instance.checkAddToQueue(this);
   * }
   */

  /**
   * Perform checks to see if this request needs adding to the ReShare queue  
   * II :: This has been replaced by ReShareMessageService::afterInsert GORM event to aleviate the need for
   *       statics in service classes
   * def afterUpdate() {
   *   ReShareMessageService.instance.checkAddToQueue(this);
   * }
   */
}
