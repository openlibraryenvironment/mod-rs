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
      serviceType: req.serviceType?.value,

      // Note that the internal name differs from the protocol name
      needBeforeDate: req.neededBy

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
                                         String reasonUnfilled = null,
                                         String note = null) {
    Map message = buildSkeletonMessage('SUPPLYING_AGENCY_MESSAGE')

    message.header = buildHeader(pr, 'SUPPLYING_AGENCY_MESSAGE', pr.resolvedSupplier, pr.resolvedRequester)
    message.messageInfo = [
      reasonForMessage:reason_for_message,
      note: note
    ]
    message.statusInfo = [
      status:status
    ]

    if ( reasonUnfilled ) {
      message.messageInfo.reasonUnfilled = [ value: reasonUnfilled ]
    }

    // Whenever a note is attached to the message, create a notification with action.
    if (note != null) {
      def context = reason_for_message
      
      if (reason_for_message != 'Notification') {
        context = reason_for_message + status
      }
  
      reshareActionService.outgoingNotificationEntry(pr, note, context, pr.resolvedSupplier, pr.resolvedSupplier, false)
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
      reshareActionService.outgoingNotificationEntry(
        pr,
        note,
        action,
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