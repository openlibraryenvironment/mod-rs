package org.olf.rs;

import groovy.lang.Closure
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestNotification
import org.olf.okapi.modules.directory.Symbol;



class ProtocolMessageBuildingService {

  /*
   * This method is purely for building out the structure of protocol messages
   * into an eventdata map, before they're handed off to the protocol message
   * service for sending. This service shouldn't do any other work.
   *
  */

  ProtocolMessageService protocolMessageService
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  ReshareActionService reshareActionService

  public Map buildSkeletonMessage(String messageType) {
    Map message = [
      messageType: messageType,
      header:[
        requestingAgencyId:[:],
        supplyingAgencyId:[:]
      ],
    ]

    return message;
  }


  public Map buildRequestMessage(PatronRequest req) {
    Map message = buildSkeletonMessage('REQUEST')

    message.header = buildHeader(req, 'REQUEST', req.resolvedRequester, null)

    message.bibliographicInfo = [
      title: req.title,
      requestingInstitutionSymbol: req.requestingInstitutionSymbol,
      author: req.author,
      subtitle: req.subtitle,
      sponsoringBody: req.sponsoringBody,
      volume: req.volume,
      issue: req.issue,
      startPage: req.startPage,
      numberOfPages: req.numberOfPages,
      edition: req.edition,
      issn: req.issn,
      isbn: req.isbn,
      doi: req.doi,
      coden: req.coden,
      sici: req.sici,
      bici: req.bici,
      eissn: req.eissn,
      stitle : req.stitle ,
      part: req.part,
      artnum: req.artnum,
      ssn: req.ssn,
      quarter: req.quarter,
      systemInstanceIdentifier: req.systemInstanceIdentifier,
      titleOfComponent: req.titleOfComponent,
      authorOfComponent: req.authorOfComponent,
      sponsor: req.sponsor,
      informationSource: req.informationSource,
    ]
    message.publicationInfo = [
      publisher: req.publisher,
      publicationType: req.publicationType?.value,
      publicationDate: req.publicationDate,

      //TODO what is this publicationDateOfComponent?
      publicationDateOfComponent: req.publicationDateOfComponent,
      placeOfPublication: req.placeOfPublication
    ]
    message.serviceInfo = [
      //TODO the following fields are permitted here but not currently included:
      /*
       * RequestType
       * RequestSubtype
       * RequestingAgencyPreviousRequestId
       * ServiceLevel
       * PreferredFormat
       * CopyrightCompliance
       * AnyEdition
       * StartDate
       * EndDate
       * Note
      */

      //ToDo the below line currently does nothing since we never actually set serviceType rn
      //serviceType: req.serviceType?.value,

      // ToDo wire in some proper information here instead of this hardcoded stuff
      serviceType: 'Loan',
      serviceLevel: 'Loan',
      anyEdition: 'Y',

      // Note that the internal names sometimes differ from the protocol names--pay attention with these fields
      needBeforeDate: req.neededBy,
      note: req.patronNote

    ]
    // TODO SupplierInfo Section
    
    /* message.supplierInfo = [
     * SortOrder
     * SupplierCode
     * SupplierDescription
     * BibliographicRecordId
     * CallNumber
     * SummaryHoldings
     * AvailabilityNote
    ]
     */
    message.requestedDeliveryInfo = [
      // SortOrder
      address:[
        physicalAddress:[
          line1:req.pickupLocation,
          line2:null,
          locality:null,
          postalCode:null,
          region:null,
          county:null
        ]
      ]
    ]
    // TODO Will this information be taken from the directory entry?
    /* message.requestingAgencyInfo = [
     * Name
     * ContactName
     * Address
    ]
     */
     message.patronInfo = [
      // Note that the internal names differ from the protocol name
      patronId: req.patronIdentifier,
      surname: req.patronSurname,
      givenName: req.patronGivenName,

      patronType: req.patronType,
      //TODO what is this field: patronReference?
      patronReference: req.patronReference,
      /* Also permitted:
       * SendToPatron
       * Address
      */
     ]

     /*
      * message.billingInfo = [
      * Permitted fields:
      * PaymentMethod
      * MaximumCosts
      * BillingMethod
      * BillingName
      * Address
      ]
     */

    return message;
  }

  public Map buildSupplyingAgencyMessage(PatronRequest pr, 
                                         String reason_for_message,
                                         String status, 
                                         Map messageParams) {

    Map message = buildSkeletonMessage('SUPPLYING_AGENCY_MESSAGE')

    message.header = buildHeader(pr, 'SUPPLYING_AGENCY_MESSAGE', pr.resolvedSupplier, pr.resolvedRequester)
    message.messageInfo = [
      reasonForMessage:reason_for_message,
      note: messageParams?.note
    ]
    message.statusInfo = [
      status:status
    ]

    if ( messageParams.reason ) {
      message.messageInfo.reasonUnfilled = messageParams?.reason
    }

    if ( messageParams.cancelResponse ) {
      if (messageParams.cancelResponse == "yes") {
        message.messageInfo.answerYesNo = "Y"
      } else {
        message.messageInfo.answerYesNo = "N"
      }
    }

    // We need to check in a couple of places whether the note is null/whether to add a note
    String note = messageParams?.note
    message.deliveryInfo = [:]
    if ( messageParams.loanCondition ) {
      message.deliveryInfo['loanCondition'] = messageParams?.loanCondition
      reshareApplicationEventHandlerService.addLoanConditionToRequest(pr, messageParams.loanCondition, pr.resolvedSupplier, note)
    }

    // Whenever a note is attached to the message, create a notification with action.
    if (note != null) {
      Map actionMap = [action: reason_for_message]
      actionMap.status = status

      if (messageParams.loanCondition) {
        actionMap.status = "Conditional"
        actionMap.data = messageParams.loanCondition
      }
      if (messageParams.reason) {
        actionMap.data = messageParams.reason
      }
  
      reshareActionService.outgoingNotificationEntry(pr, messageParams.note, actionMap, pr.resolvedSupplier, pr.resolvedSupplier, false)
    }

    if( pr.selectedItemBarcode ) {
      message.deliveryInfo['itemId'] = pr.selectedItemBarcode
    }
    
    if( pr?.dueDateRS ) {
      message.statusInfo['dueDate'] = pr.dueDateRS
    }
    
    return message
  }


  public Map buildRequestingAgencyMessage(PatronRequest pr, String message_sender, String peer, String action, String note = null) {
    Map message = buildSkeletonMessage('REQUESTING_AGENCY_MESSAGE')

    Symbol message_sender_symbol = reshareApplicationEventHandlerService.resolveCombinedSymbol(message_sender)
    Symbol peer_symbol = reshareApplicationEventHandlerService.resolveCombinedSymbol(peer)

    message.header = buildHeader(pr, 'REQUESTING_AGENCY_MESSAGE', message_sender_symbol, peer_symbol)
    message.activeSection = [
      action: action,
      note: note
    ]

    // Whenever a note is attached to the message, create a notification with action.
    if (note != null) {
      Map actionMap = [action: action]
      reshareActionService.outgoingNotificationEntry(
        pr,
        note,
        actionMap,
        message_sender_symbol,
        peer_symbol,
        true
      )
    }
    return message
  }

  private Map buildHeader(PatronRequest pr, String messageType, Symbol message_sender_symbol, Symbol peer_symbol) {
    Map supplyingAgencyId
    Map requestingAgencyId
    String requestingAgencyRequestId
    String supplyingAgencyRequestId
    
    log.debug("ProtocolMessageBuildingService::buildHeader(${pr}, ${messageType}, ${message_sender_symbol}, ${peer_symbol})");

    
    if (messageType == 'REQUEST' || messageType == 'REQUESTING_AGENCY_MESSAGE') {

      // Set the requestingAgencyId and the requestingAgencyRequestId
      requestingAgencyId = buildHeaderRequestingAgencyId(message_sender_symbol)
      requestingAgencyRequestId = pr.hrid ?: pr.id

      if (messageType == 'REQUEST') {
        // If this message is a request then the supplying Agency details get filled out later and the supplying request id is null
        supplyingAgencyRequestId = null
      } else {
        supplyingAgencyId = buildHeaderSupplyingAgencyId(peer_symbol)
        supplyingAgencyRequestId = pr.peerRequestIdentifier
      }

    } else {
      // Set the AgencyIds
      supplyingAgencyId = buildHeaderSupplyingAgencyId(message_sender_symbol)
      requestingAgencyId = buildHeaderRequestingAgencyId(peer_symbol)

      // Set the RequestIds
      requestingAgencyRequestId = pr.peerRequestIdentifier
      supplyingAgencyRequestId = pr.id
    }

    Map header = [
      supplyingAgencyId: supplyingAgencyId,
      requestingAgencyId: requestingAgencyId,

      requestingAgencyRequestId:requestingAgencyRequestId,
      supplyingAgencyRequestId:supplyingAgencyRequestId
    ]

    return header;
  }

  private Map buildHeaderSupplyingAgencyId(Symbol supplier) {
    Map supplyingAgencyId = [
      agencyIdType: supplier?.authority?.symbol,
      agencyIdValue: supplier?.symbol
    ]
    return supplyingAgencyId;
  }

  private Map buildHeaderRequestingAgencyId(Symbol requester) {
    Map requestingAgencyId = [
      agencyIdType: requester?.authority?.symbol,
      agencyIdValue: requester?.symbol
    ]
    return requestingAgencyId;
  }

}
