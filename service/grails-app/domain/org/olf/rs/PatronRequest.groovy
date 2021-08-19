package org.olf.rs

import javax.persistence.Transient
import grails.databinding.BindInitializer
import grails.gorm.multitenancy.Tenants;
import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import com.k_int.web.toolkit.custprops.CustomProperties
import com.k_int.web.toolkit.refdata.CategoryId
import com.k_int.web.toolkit.refdata.Defaults
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.AvailableAction;
import com.k_int.web.toolkit.tags.Tag
import org.olf.okapi.modules.directory.Symbol;
import java.time.LocalDate;
import org.olf.okapi.modules.directory.DirectoryEntry;
import groovy.sql.Sql;

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner. 
 */

class PatronRequest implements CustomProperties, MultiTenant<PatronRequest> {

  private static final String POSSIBLE_ACTIONS_QUERY='select distinct aa.actionCode from AvailableAction as aa where aa.fromState = :fromstate'

  // internal ID of the patron request
  String id

  @Defaults(['Book', 'Journal', 'Other'])
  @CategoryId(ProtocolReferenceDataValue.CATEGORY_PUBLICATION_TYPE)
  ProtocolReferenceDataValue publicationType


  // A string representing the institution of the requesting patron
  // resolvable in the directory.
  String requestingInstitutionSymbol
  Symbol resolvedRequester

  // These two properties are ONLY used when isRequester=false. For requester rows, the peer is be defined by the
  // rota entry for each potential supplier.
  String supplyingInstitutionSymbol
  Symbol resolvedSupplier

  // How our peer identifies this request
  String peerRequestIdentifier

  // Bibliographic descriptive fields
  // Title of the item requested
  String title
  String author
  String subtitle
  String sponsoringBody
  String publisher
  String placeOfPublication

  /*
   * This field represents the freeform text string we get in the request message
   * (on the supplier's side),
   * or from discovery (on the requester's side).
   * Once the supplier comes to scan and ship the items, at that point they can read this and make a decision about which
   * items to include in the request, before attaching them to the request as 
   */
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
  String oclcNumber // OCLC-specific identifier used for lookup in certain situations
  String part
  String artnum
  String ssn
  String quarter
  String bibliographicRecordId
  String supplierUniqueRecordId
  
  // These fields reflect local resources we have correlated with the fields from a protocol message above
  String systemInstanceIdentifier
  String selectedItemBarcode

  String titleOfComponent
  String authorOfComponent
  String sponsor
  String informationSource

  // The identifier of the patron
  String patronIdentifier
  // A reference the patron may wish this request to be known as
  String patronReference
  String patronSurname
  String patronGivenName
  String patronType
  Boolean sendToPatron
  String patronEmail
  String patronNote
  String pickupLocation
  String pickupLocationCode
  String pickupLocationSlug
  DirectoryEntry resolvedPickupLocation

  // A json blob containing the response to a lookup in the shared index.
  String bibRecord

  // These 2 dates are maintained by the framework for us
  Date dateCreated
  Date lastUpdated

  LocalDate neededBy

  // A property for responders - what we think the local call number is
  String localCallNumber

  String hrid;
  
  Set rota = []

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

  /** If we hit an error this was the status prior to the error occurring */
  Status preErrorStatus;

  /** Are we waiting for a protocol message to be sent  - If so, pendingAction will contain the action we are currently trying to perform */
  boolean awaitingProtocolResponse;

  /** Supplier side property : Is this PR an item currently on loan */
  Boolean activeLoan

  /** The position we are in the rota */
  Long rotaPosition;

  /** Lets us know the if the system has updated the record, as we do not want validation on the pendingAction field to happen as it has already happened */
  boolean systemUpdate = false;

  // This is a transient property used to communicate between the onUpdate handler in this class
  // and the GORM event handler applicationEventListenerService
  boolean stateHasChanged = false;

  // For a RESPONDER/SUPPLIER/LENDER - which local LMS location will the item be picked from, the shelving location and the call number
  HostLMSLocation pickLocation;
  String pickShelvingLocation;


  /* We want to be able to get in and out of 'cancellation' states from a number of states in the state model,
   * so instead of having a dedicated state on the supplier side, we use a boolean to track whether a requester
   * has requested a cancellation or not. On the requesting side, we DO have a state "REQ_CANCEL_PENDING".
   * We need to track whether this cancel is for this supplier only (in which case we then go to state CANCELLED_WITH_SUPPLIER,
   * which triggers sendToNextLender and continues the request) or whether this is actually a cancellation of the entire request,
   * in which case the request gets closed. 
   */ 

  //Boolean to flag whether a request continues after the current cancellation with supplier
  boolean requestToContinue = true;

  // We also sometimes need to know the state(s) that existed before heading into a pending state--in the correct order
  Map previousStates = [:]

  Patron resolvedPatron

  Boolean needsAttention

  int unreadMessageCount

  String dueDateFromLMS
  Date parsedDueDateFromLMS
  
  String dueDateRS
  Date parsedDueDateRS
  
  Boolean overdue

  RefdataValue cancellationReason
  

  static transients = ['systemUpdate', 'stateHasChanged', 'descriptiveMetadata'];

  // The audit of what has happened to this request and tags that are associated with the request, as well as the rota and notifications */
  static hasMany = [
    audit : PatronRequestAudit,
    conditions: PatronRequestLoanCondition,
    notifications: PatronRequestNotification,
    rota  : PatronRequestRota,
    tags  : Tag,
    volumes: RequestVolume
  ];

  static mappedBy = [
    rota: 'patronRequest',
    audit: 'patronRequest',
    conditions: 'patronRequest',
    notifications: 'patronRequest',
    volumes: 'patronRequest'
  ]

  static fetchMode = [
    rota:"eager"
  ]

  static constraints = {

    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronIdentifier (nullable: true)
    patronReference (nullable: true)
    serviceType (nullable: true)
    state (nullable: true, bindable: false)
    isRequester (nullable: true, bindable: true)
    numberOfRetries (nullable: true, bindable: false)
    delayPerformingActionUntil (nullable: true, bindable: false)
    preErrorStatus (nullable: true, bindable: false)
    awaitingProtocolResponse (bindable: false)
    rotaPosition (nullable: true, bindable: false)
    publicationType (nullable: true)

    title (nullable: true, blank : false, maxSize:255)
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
    oclcNumber (nullable: true, blank : false)
    stitle (nullable: true, blank : false)
    part (nullable: true, blank : false)
    artnum (nullable: true, blank : false)
    ssn (nullable: true, blank : false)
    quarter (nullable: true, blank : false)
    systemInstanceIdentifier (nullable: true, blank : false)
    selectedItemBarcode (nullable: true, blank : false)
    bibliographicRecordId( nullable: true, blank : false)
    supplierUniqueRecordId( nullable: true, blank : false)

    titleOfComponent (nullable: true, blank : false)
    authorOfComponent (nullable: true, blank : false)
    sponsor (nullable: true, blank : false)
    informationSource (nullable: true, blank : false)
    patronSurname (nullable: true, blank : false)
    patronGivenName (nullable: true, blank : false)
    patronType (nullable: true, blank : false)
    sendToPatron (nullable: true )
    bibRecord (nullable: true )
    neededBy (nullable: true )

    requestingInstitutionSymbol (nullable: true)
    resolvedRequester(nullable: true)

    supplyingInstitutionSymbol (nullable: true)
    resolvedSupplier(nullable: true)

    peerRequestIdentifier (nullable: true)

    pickLocation(nullable: true)
    pickShelvingLocation(nullable: true, blank:false)
    localCallNumber (nullable: true, blank:false)
    hrid (nullable: true, blank:false)

    patronEmail (nullable: true, blank:false)
    patronNote (nullable: true, blank:false)
    pickupLocation (nullable: true, blank:false)
    pickupLocationCode (nullable: true)
    pickupLocationSlug (nullable: true)
    resolvedPickupLocation (nullable: true)

    resolvedPatron (nullable: true)
    // this is Boolean - does not support nullable - must be true or false - comment out to remove warning
    // requestToContinue (nullable: false)

    activeLoan(nullable: true)
    dueDateFromLMS(nullable: true)
    parsedDueDateFromLMS(nullable: true)
    dueDateRS(nullable: true)
    parsedDueDateRS(nullable: true)

    cancellationReason(nullable: true)
    needsAttention(nullable: true)
    
    overdue(nullable: true)
  }

  static mapping = {
    unreadMessageCount formula: '(SELECT COUNT(*) FROM patron_request_notification AS prn INNER JOIN patron_request as pr ON prn.prn_patron_request_fk = pr.pr_id WHERE pr.pr_id = pr_id AND prn.prn_seen = false AND prn.prn_is_sender = false )'

    id column : 'pr_id', generator: 'uuid2', length:36
    version column : 'pr_version'
    dateCreated column : 'pr_date_created'
    lastUpdated column : 'pr_last_updated'
    patronIdentifier column : 'pr_patron_identifier'
    patronReference column : 'pr_patron_reference'
    serviceType column : 'pr_service_type_fk'
    state column : 'pr_state_fk'
    isRequester column : "pr_is_requester"
    numberOfRetries column : 'pr_number_of_retries'
    delayPerformingActionUntil column : 'pr_delay_performing_action_until'
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
    oclcNumber column : 'pr_oclc_number'
    stitle column : 'pr_stitle'
    part column : 'pr_part'
    artnum column : 'pr_artnum'
    ssn column : 'pr_ssn'
    quarter column : 'pr_quarter'
    bibliographicRecordId column : 'pr_bib_record_id'
    supplierUniqueRecordId column : 'pr_supplier_unique_record_id'

    systemInstanceIdentifier column: 'pr_system_instance_id'
    selectedItemBarcode column: 'pr_selected_item_barcode'

    requestingInstitutionSymbol column : 'pr_req_inst_symbol'
    resolvedRequester column : 'pr_resolved_req_inst_symbol_fk'

    supplyingInstitutionSymbol column : 'pr_sup_inst_symbol'
    resolvedSupplier column : 'pr_resolved_sup_inst_symbol_fk'

    peerRequestIdentifier column : 'pr_peer_request_identifier'

    titleOfComponent column : 'pr_title_of_component'
    authorOfComponent column : 'pr_author_of_component'
    sponsor column : 'pr_sponsor'
    informationSource column : 'pr_information_source'

    patronSurname column : 'pr_patron_surname'
    patronGivenName column : 'pr_patron_name'
    patronType column : 'pr_patron_type'
    sendToPatron column : 'pr_send_to_patron'
    bibRecord column: 'pr_bib_record'
    neededBy column : 'pr_needed_by'

    pickLocation column: 'pr_pick_location_fk'
    pickShelvingLocation column: 'pr_pick_shelving_location'
    localCallNumber column : 'pr_local_call_number'

    hrid column : 'pr_hrid'

    patronEmail column : 'pr_patron_email'
    patronNote column : 'pr_patron_note'
    pickupLocation column : 'pr_pref_service_point'
    pickupLocationCode column : 'pr_pref_service_point_code'
    pickupLocationSlug column : 'pr_pickup_location_slug'
    resolvedPickupLocation column : 'pr_resolved_pickup_location_fk'
    resolvedPatron column : 'pr_resolved_patron_fk'
    requestToContinue column: 'pr_request_to_continue'
    previousStates column: 'pr_previous_states'
    activeLoan column: 'pr_active_loan'
    needsAttention column: 'pr_needs_attention'
    cancellationReason column: 'pr_cancellation_reason_fk'

    dueDateFromLMS column: 'pr_due_date_from_lms'
    parsedDueDateFromLMS column: 'pr_parsed_due_date_lms'
    dueDateRS column: 'pr_due_date_rs'
    parsedDueDateRS column: 'pr_parsed_due_date_rs'
    
    overdue column: 'pr_overdue'

    audit(sort:'dateCreated', order:'desc')

    volumes cascade: 'all-delete-orphan'
  }

  /**
   * If this is a requester request we are inserting then set the pending action to be VALIDATE
   */
  def beforeInsert() {
    if ( this.state == null ) {
      if ( this.isRequester ) {
        this.state = Status.lookup('PatronRequest', 'REQ_IDLE');
      }
      else {
        this.state = Status.lookup('Responder', 'RES_IDLE');
      }
    }
  }

  def beforeUpdate() {
    if ( this.state != this.getPersistentValue('state') ) {
      stateHasChanged=true
    }
  }

  def getValidActions() {
    return AvailableAction.executeQuery(POSSIBLE_ACTIONS_QUERY,[fromstate:this.state])
  }

  public Map getDescriptiveMetadata() {
    return [
      'title': this.title,
      'systemInstanceIdentifier': this.systemInstanceIdentifier
    ]
  }
}
