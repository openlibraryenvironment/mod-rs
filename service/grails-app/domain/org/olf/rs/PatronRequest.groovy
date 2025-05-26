package org.olf.rs

import java.time.LocalDate;

import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.statemodel.StateModel;
import org.olf.rs.statemodel.Status;

import com.k_int.web.toolkit.custprops.CustomProperties;
import com.k_int.web.toolkit.refdata.CategoryId;
import com.k_int.web.toolkit.refdata.Defaults;
import com.k_int.web.toolkit.refdata.RefdataValue;
import com.k_int.web.toolkit.tags.Tag;

import grails.gorm.MultiTenant;
import grails.util.Holders;

/**
 * PatronRequest - Instances of this class represent an occurrence of a patron (Researcher, Undergrad, Faculty)
 * requesting that reshare locate and deliver a resource from a remote partner.
 */

class PatronRequest implements CustomProperties, MultiTenant<PatronRequest> {

  // internal ID of the patron request
  String id

  @Defaults(['ArchiveMaterial', 'Article', 'AudioBook', 'Book', 'Chapter', 'ConferenceProc', 'Game', 'GovernmentPubl', 'Image', 'Journal', 'Manuscript', 'Map', 'Movie', 'MusicRecording', 'MusicScore', 'Newspaper', 'Patent', 'Report', 'SoundRecording', 'Thesis'])
  @CategoryId(ProtocolReferenceDataValue.CATEGORY_PUBLICATION_TYPE)
  ProtocolReferenceDataValue publicationType

  @Defaults(['ArticleExchange', 'Courier', 'Email', 'FTP', 'Mail', 'Odyssey', 'URL'])
  @CategoryId(ProtocolReferenceDataValue.CATEGORY_DELIVERY_METHOD)
  ProtocolReferenceDataValue deliveryMethod


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
  String subtitle
  String author
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
  String pagesRequested
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
  String localNote
  String pickupLocation
  String pickupLocationCode
  String pickupLocationSlug
  DirectoryEntry resolvedPickupLocation
  String pickupURL
  String deliveryAddress
  String returnAddress

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

  @Defaults(['Loan', 'Copy', 'CopyOrLoan'])
  @CategoryId(ProtocolReferenceDataValue.CATEGORY_SERVICE_TYPE)
  ProtocolReferenceDataValue serviceType

  /** Is the this the requester or suppliers view of the request */
  Boolean isRequester;

  /** The state model this request is following */
  StateModel stateModel;

  /** The current status of the request */
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

  // A transient property to indicate a forced state change to a terminal state
  boolean manuallyClosed = false;

  // For a RESPONDER/SUPPLIER/LENDER - which local LMS location will the item be picked from, the shelving location and the call number
  HostLMSLocation pickLocation;
  HostLMSShelvingLocation pickShelvingLocation;

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

  /** Gives the count of unread messages, do not use this to test if there any unread messages */
  int unreadMessageCount;

  /** Is true if there are unread messages, use this instead of unreadMessageCount > 0  */
  Boolean hasUnreadMessages;

  String dueDateFromLMS
  Date parsedDueDateFromLMS

  String dueDateRS
  Date parsedDueDateRS

  Boolean overdue

  @CategoryId(RefdataValueData.VOCABULARY_CANCELLATION_REASONS)
  RefdataValue cancellationReason

  @CategoryId(RefdataValueData.VOCABULARY_COPYRIGHT_TYPE)
  RefdataValue copyrightType;

  /** The current status of any network messaging */
  NetworkStatus networkStatus;

  /** The date of when we last sent the request */
  Date lastSendAttempt;

  /** The date of when we plan to next attempt to send it */
  Date nextSendAttempt;

  /** The event data used to send the protocol message, this column will only be populated when there is an error sending the data, will be cleared afterwards */
  String lastProtocolData;

  /** The number of times we have attempted to send the last message, only set if we hit an error */
  Integer numberOfSendAttempts;

  /** So we can try and cater for timeout issues, we append a sequence number to the note */
  Integer lastSequenceSent;

  /** The last number assigned to an audit record for this request */
  Integer lastAuditNo;

  /** This is the last sequence number we received */
  Integer lastSequenceReceived;

  /** As an ISO18626 supplier, we should only send the RequestResponse reason message, so this is to say we have sent it, so we do not send it again */
  Boolean sentISO18626RequestResponse;

  /** Request that this request was created from */
  PatronRequest precededBy;

  /** Request created from this request */
  PatronRequest succeededBy;

  /** If we create an external hold for this request, store the request id returned */
  String externalHoldRequestId;

  /** JSON object containing custom identifiers */
  String customIdentifiers

  BigDecimal maximumCostsMonetaryValue;

  @CategoryId(RefdataValueData.VOCABULARY_CURRENCY_CODES)
  RefdataValue maximumCostsCurrencyCode;

  @CategoryId(RefdataValueData.VOCABULARY_SERVICE_LEVELS)
  RefdataValue serviceLevel;

  static transients = ['systemUpdate', 'stateHasChanged', 'descriptiveMetadata', 'manuallyClosed', 'validActions'];

  // The audit of what has happened to this request and tags that are associated with the request, as well as the rota and notifications */
  static hasMany = [
    audit : PatronRequestAudit,
    batches : Batch,
    conditions: PatronRequestLoanCondition,
    notifications: PatronRequestNotification,
    protocolAudit: ProtocolAudit,
    requestIdentifiers: RequestIdentifier,
    rota  : PatronRequestRota,
    tags  : Tag,
    volumes: RequestVolume
  ];

  static mappedBy = [
    rota: 'patronRequest',
    audit: 'patronRequest',
    conditions: 'patronRequest',
    notifications: 'patronRequest',
    protocolAudit: 'patronRequest',
    requestIdentifiers: 'patronRequest',
    volumes: 'patronRequest'
  ]

  static fetchMode = [
    rota:"eager"
  ]

  // The request can belong to multiple batches
  static belongsTo = Batch;

  static constraints = {

    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    patronIdentifier (nullable: true)
    patronReference (nullable: true)
    serviceType (nullable: true)
    stateModel (nullable: true, bindable: false)
    state (nullable: true, bindable: false)
    isRequester (nullable: true, bindable: true)
    numberOfRetries (nullable: true, bindable: false)
    delayPerformingActionUntil (nullable: true, bindable: false)
    preErrorStatus (nullable: true, bindable: false)
    awaitingProtocolResponse (bindable: false)
    rotaPosition (nullable: true, bindable: false)
    publicationType (nullable: true)
    deliveryMethod (nullable: true)

    title (nullable: true, blank : false, maxSize:255)
    subtitle (nullable: true, blank : false)
    author (nullable: true, blank : false)
    sponsoringBody (nullable: true, blank : false)
    publisher (nullable: true, blank : false)
    placeOfPublication (nullable: true, blank : false)
    volume (nullable: true, blank : false)
    issue (nullable: true, blank : false)
    startPage (nullable: true, blank : false)
    numberOfPages (nullable: true, blank : false)
    pagesRequested (nullable: true, blank : false)
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
    copyrightType (nullable: true, blank : false)
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
    pickShelvingLocation(nullable: true)
    localCallNumber (nullable: true, blank:false)
    hrid (nullable: true, blank:false)

    patronEmail (nullable: true, blank:false)
    patronNote (nullable: true, blank:false)
    localNote (nullable: true, blank:false)
    pickupLocation (nullable: true, blank:false)
    pickupLocationCode (nullable: true)
    pickupLocationSlug (nullable: true)
    resolvedPickupLocation (nullable: true)
    pickupURL (nullable: true, blank:false)

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

    networkStatus (nullable: true)
    lastSendAttempt (nullable: true)
    nextSendAttempt (nullable: true)
    lastProtocolData (nullable: true)
    numberOfSendAttempts (nullable: true)
    lastSequenceSent (nullable: true)
    lastSequenceReceived (nullable: true)

    lastAuditNo (nullable: true)
    sentISO18626RequestResponse (nullable: true)

    precededBy (nullable: true)
    succeededBy (nullable: true)

    externalHoldRequestId (nullable: true)
    customIdentifiers (nullable: true)

    maximumCostsMonetaryValue(nullable: true)
    maximumCostsCurrencyCode(nullable: true)

    serviceLevel (nullable: true)

    deliveryAddress (nullable: true)
    returnAddress (nullable: true)
  }

  static mapping = {
    unreadMessageCount formula: '(SELECT COUNT(*) FROM patron_request_notification AS prn INNER JOIN patron_request as pr ON prn.prn_patron_request_fk = pr.pr_id WHERE pr.pr_id = pr_id AND prn.prn_seen = false AND prn.prn_is_sender = false )'
    hasUnreadMessages formula: 'exists (SELECT 1 FROM patron_request_notification AS prn INNER JOIN patron_request as pr ON prn.prn_patron_request_fk = pr.pr_id WHERE pr.pr_id = pr_id AND prn.prn_seen = false AND prn.prn_is_sender = false )'

    id column : 'pr_id', generator: 'uuid2', length:36
    version column : 'pr_version'
    dateCreated column : 'pr_date_created'
    lastUpdated column : 'pr_last_updated'
    patronIdentifier column : 'pr_patron_identifier'
    patronReference column : 'pr_patron_reference'
    serviceType column : 'pr_service_type_fk'
    stateModel column : 'pr_state_model_fk'
    state column : 'pr_state_fk'
    isRequester column : "pr_is_requester"
    numberOfRetries column : 'pr_number_of_retries'
    delayPerformingActionUntil column : 'pr_delay_performing_action_until'
    preErrorStatus column : 'pr_pre_error_status_fk'
    awaitingProtocolResponse column : 'pr_awaiting_protocol_response'
    rotaPosition column : 'pr_rota_position'
    publicationType column : 'pr_pub_type_fk'
    deliveryMethod column : 'pr_delivery_method_fk'

    title column : 'pr_title'
    subtitle column : 'pr_sub_title'
    author column : 'pr_author'
    sponsoringBody column : 'pr_sponsoring_body'
    publisher column : 'pr_publisher'
    placeOfPublication column : 'pr_place_of_pub'
    volume column : 'pr_volume'
    issue column : 'pr_issue'
    startPage column : 'pr_start_page'
    numberOfPages column : 'pr_num_pages'
    pagesRequested column : 'pr_pages_requested'
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
    copyrightType column: 'pr_copyright_type_fk'
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
    pickShelvingLocation column: 'pr_pick_shelving_location_fk'
    localCallNumber column : 'pr_local_call_number'

    hrid column : 'pr_hrid'

    patronEmail column : 'pr_patron_email'
    patronNote column : 'pr_patron_note'
    localNote column : 'pr_local_note'
    pickupLocation column : 'pr_pref_service_point'
    pickupLocationCode column : 'pr_pref_service_point_code'
    pickupLocationSlug column : 'pr_pickup_location_slug'
    resolvedPickupLocation column : 'pr_resolved_pickup_location_fk'
    pickupURL column : 'pr_pickup_url'
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

    networkStatus column: 'pr_network_status', length:32
    lastSendAttempt column: 'pr_last_send_attempt'
    nextSendAttempt column: 'pr_next_send_attempt'
    lastProtocolData column: 'pr_last_protocol_data', length:20000
    numberOfSendAttempts column: 'pr_number_of_send_attempts'
    lastSequenceSent column: 'pr_last_sequence_sent'
    lastSequenceReceived column: 'pr_last_sequence_received'

    lastAuditNo column: 'pr_last_audit_no'
    sentISO18626RequestResponse column: 'pr_sent_iso18626_request_response'

    precededBy column: 'pr_preceded_by_fk'
    succeededBy column: 'pr_succeeded_by_fk'

    externalHoldRequestId column: 'pr_external_hold_request_id'

    audit(sort:'dateCreated', order:'desc')
    protocolAudit(sort:'dateCreated', order:'desc')

    requestIdentifiers cascade: 'all-delete-orphan'
    volumes cascade: 'all-delete-orphan'

    batches column : 'bpr_patron_request_id', joinTable : 'batch_patron_request'

    customIdentifiers column : 'pr_custom_identifiers'

    maximumCostsMonetaryValue column : 'pr_maximum_costs_value'
    maximumCostsCurrencyCode column : 'pr_maximum_costs_code_fk'

    serviceLevel column: 'pr_service_level_fk'

    deliveryAddress column : 'pr_delivery_address'
    returnAddress column : 'pr_return_address'
  }

  /**
   * The beforeInsert method is triggered prior to an insert
   */
  def beforeInsert() {
    // If the state model is null then set it
    if (this.stateModel == null) {
        this.stateModel = Holders.grailsApplication.mainContext.getBean('statusService').getStateModel(this);
    }

    // If the state is null, then we need to set it to the initial state for the model in play
    if (this.state == null) {
      this.state = stateModel.initialState;
    }
  }

  def beforeUpdate() {
    if ( this.state != this.getPersistentValue('state') ) {
      stateHasChanged=true
    }
  }

  def beforeValidate() {
    // Truncate all necessary fields which could be "over"filled from citation and log the truncation
    truncateAndLog("title", title)
    truncateAndLog("author", author)
    truncateAndLog("subtitle", subtitle)
    truncateAndLog("sponsoringBody", sponsoringBody)
    truncateAndLog("publisher", publisher)
    truncateAndLog("placeOfPublication", placeOfPublication)

    truncateAndLog("titleOfComponent", titleOfComponent)
    truncateAndLog("authorOfComponent", authorOfComponent)
    truncateAndLog("sponsor", sponsor)
    truncateAndLog("informationSource", informationSource)
  }

  boolean isNetworkActivityIdle() {
      // We don't want to perform new actions while it's currently attempting a previoús one
      return((networkStatus != NetworkStatus.Waiting) && (networkStatus != NetworkStatus.Retry));
  }

  public Map getDescriptiveMetadata() {
    return [
      'title': this.title,
      'systemInstanceIdentifier': this.systemInstanceIdentifier
    ]
  }

  public int incrementLastSequence() {
      // Increase the sequence number on the request
      if (lastSequenceSent == null) {
          // First time it has been sent
          lastSequenceSent = 0;
      } else {
          // Has been sent before
          lastSequenceSent++;
      }
      return(lastSequenceSent);
  }

  public int incrementLastAuditNo() {
      // Increase the last audit number on the request
      if (lastAuditNo == null) {
          // First time we have created an audit record
          lastAuditNo = 1;
      } else {
          // Previously created an audit record, so just increment
          lastAuditNo++;
      }

      // Return the audit number that can now be used
      return(lastAuditNo);
  }

  List getValidActions() {
      // Valid actions are determined via ActionService.getValidActions() however this seemingly cannot be called from a view.
      // Now with Grails 5 there is no hibernate session when it's called that way so one needs to be explicitly created here.
      // Prefer using the ActionService method where possible.
      withNewSession { session ->
        return(Holders.grailsApplication.mainContext.getBean('actionService').getValidActions(this));
      }
  }

  public void updateRotaState(Status status) {
      PatronRequestRota patronRequestRota = rota.find({ rota -> rota.rotaPosition == rotaPosition });
      if (patronRequestRota) {
          patronRequestRota.state = status;
          patronRequestRota.save(flush:true, failOnError: true);
      }
  }

  private String truncateAndLog(String fieldName, String field, int truncateLength = 255) {
    if ( field?.length() > truncateLength ) {
      String truncatedField = "${field.take(truncateLength - 3)}..."
      log.warn("${fieldName} ${field} more than ${truncateLength} characters. Truncated to ${truncatedField}")
      this[fieldName] = truncatedField;
    }
  }

  DirectoryEntry getResolvedRequesterDirectoryEntry() {
    resolvedRequester?.owner?.name
    return resolvedRequester?.owner
  }

  DirectoryEntry getResolvedSupplierDirectoryEntry() {
    resolvedSupplier?.owner?.name
    return resolvedSupplier?.owner
  }
}
