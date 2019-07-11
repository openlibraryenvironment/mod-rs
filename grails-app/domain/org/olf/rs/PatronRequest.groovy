package org.olf.rs

import javax.persistence.Transient
import grails.databinding.BindInitializer
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.refdata.CategoryId
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
  @CategoryId(ProtocolReferenceDataValue.CATEGORY_PUBLICATION_TYPE)
  ProtocolReferenceDataValue publicationType

  // A string representing the institution of the requesting patron
  // resolvable in the directory.
  String requestingInstitutionSymbol

  // Bibliographic descriptive fields
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
  String issn
  String isbn
  String doi
  String coden
  String sici
  String bici
  String eissn // Shudder!
  String stitle // Series Title
  String part
  String artnum
  String ssn
  String quarter
  
  String titleOfComponent
  String authorOfComponent
  String sponsor
  String informationSource

  // Patron reference (EG Barcode)
  String patronReference
  String patronSurname
  String patronGivenName
  String patronType
  Boolean sendToPatron

  // These 2 dates are maintained by the framework for us
  Date dateCreated
  Date lastUpdated

  Date neededBy

  // serviceType - added here as an example refdata item - more to show how than
  // arising from analysis and design
  @Defaults(['Loan', 'Copy-non-returnable'])
  @CategoryId(ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE)
  ProtocolReferenceDataValue serviceType

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

  /** Are we waiting for a protocol message to be sent  - If so, pendingAction will contain the action we are currently trying to perform */
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
                     
                   dateCreated (nullable: true, bindable: false)
                   lastUpdated (nullable: true, bindable: false)
               patronReference (nullable: true)
                   serviceType (nullable: true)
                         state (nullable: true, bindable: false)
                   isRequester (nullable: true, bindable: true)  // Should be false, only set to true for testing, shouldn't be sent in by client it should be set by the code
               numberOfRetries (nullable: true, bindable: false)
    delayPerformingActionUntil (nullable: true, bindable: false)
                 pendingAction (nullable: true, pendingAction : true) // The "pendingAction" attribute validates that the pending action is valid
                   errorAction (nullable: true, bindable: false)
                preErrorStatus (nullable: true, bindable: false)
      awaitingProtocolResponse (bindable: false)
                  rotaPosition (nullable: true, bindable: false)
               publicationType (nullable: true)

                         title (nullable: true, blank : false)
                        author (nullable: true, blank : false)
                      subtitle (nullable: true, blank : false)
                sponsoringBody (nullable: true, blank : false)
                     publisher (nullable: true, blank : false)
            placeOfPublication (nullable: true, blank : false)
                        volume (nullable: true, blank : false)
                         issue (nullable: true, blank : false)
                     startPage (nullable: true, blank : false)
                 numberOfPages (nullable: true, blank : false)
               publicationDate (nullable: true, blank : false)
    publicationDateOfComponent (nullable: true, blank : false)
                       edition (nullable: true, blank : false)
                          issn (nullable: true, blank : false)
                          isbn (nullable: true, blank : false)
                           doi (nullable: true, blank : false)
                         coden (nullable: true, blank : false)
                          sici (nullable: true, blank : false)
                          bici (nullable: true, blank : false)
                         eissn (nullable: true, blank : false)
                        stitle (nullable: true, blank : false)
                          part (nullable: true, blank : false)
                        artnum (nullable: true, blank : false)
                           ssn (nullable: true, blank : false)
                       quarter (nullable: true, blank : false)
 
              titleOfComponent (nullable: true, blank : false)
             authorOfComponent (nullable: true, blank : false)
                       sponsor (nullable: true, blank : false)
             informationSource (nullable: true, blank : false)
                 patronSurname (nullable: true, blank : false)
               patronGivenName (nullable: true, blank : false)
                    patronType (nullable: true, blank : false)
                  sendToPatron (nullable: true )
                      neededBy (nullable: true )
   requestingInstitutionSymbol (nullable: true )
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
                          issn column : 'pr_issn'
                          isbn column : 'pr_isbn'
                           doi column : 'pr_doi'
                         coden column : 'pr_coden'
                          sici column : 'pr_sici'
                          bici column : 'pr_bici'
                         eissn column : 'pr_eissn'
                        stitle column : 'pr_stitle'
                          part column : 'pr_part'
                        artnum column : 'pr_artnum'
                           ssn column : 'pr_ssn'
                       quarter column : 'pr_quarter'

   requestingInstitutionSymbol column : 'pr_req_inst_symbol'

              titleOfComponent column : 'pr_title_of_component'
             authorOfComponent column : 'pr_author_of_component'
                       sponsor column : 'pr_sponsor'
             informationSource column : 'pr_information_source'

                 patronSurname column : 'pr_patron_surname'
               patronGivenName column : 'pr_patron_name'
                    patronType column : 'pr_patron_type'
                  sendToPatron column : 'pr_send_to_patron'
                      neededBy column : 'pr_needed_by'
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

    // Set the rota position to 0, so that it is always set
    rotaPosition = 0;

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
