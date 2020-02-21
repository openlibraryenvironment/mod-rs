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

    //message.header.supplyingAgencyId is filled out later by sendToNextLender
    message.header.requestingAgencyId = [
      agencyIdType:req.resolvedRequester?.authority?.symbol,
      agencyIdValue:req.resolvedRequester?.symbol
    ]
    message.header.requestingAgencyRequestId = req.hrid ?: req.id
    message.header.supplyingAgencyRequestId = null
    message.bibliographicInfo = [
      publicationType: req.publicationType?.value,
      title: req.title,
      requestingInstitutionSymbol: req.requestingInstitutionSymbol,
      author: req.author,
      subtitle: req.subtitle,
      sponsoringBody: req.sponsoringBody,
      publisher: req.publisher,
      placeOfPublication: req.placeOfPublication,
      volume: req.volume,
      issue: req.issue,
      startPage: req.startPage,
      numberOfPages: req.numberOfPages,
      publicationDate: req.publicationDate,
      publicationDateOfComponent: req.publicationDateOfComponent,
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
      patronIdentifier: req.patronIdentifier,
      patronReference: req.patronReference,
      patronSurname: req.patronSurname,
      patronGivenName: req.patronGivenName,
      patronType: req.patronType,
      serviceType: req.serviceType?.value
    ]
    message.requestedDeliveryInfo = [
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
    return message;
  }

  public Map buildSupplyingAgencyMessage(PatronRequest pr, 
                                         String reason_for_message,
                                         String status, 
                                         String reasonUnfilled = null,
                                         String note = null) {
    Map message = buildSkeletonMessage('SUPPLYING_AGENCY_MESSAGE')
    message.header = [
      supplyingAgencyId:[
        agencyIdType:pr.resolvedSupplier?.authority?.symbol,
        agencyIdValue:pr.resolvedSupplier?.symbol,
      ],
      requestingAgencyId:[
        agencyIdType:pr.resolvedRequester?.authority?.symbol,
        agencyIdValue:pr.resolvedRequester?.symbol,
      ],
      requestingAgencyRequestId:pr.peerRequestIdentifier,
      supplyingAgencyRequestId:pr.id
    ]
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
      def context = reason_for_message + status
      reshareApplicationEventHandlerService.outgoingNotificationEntry(pr, note, context, pr.resolvedSupplier, pr.resolvedSupplier, false)
    }

    return message
  }


  public Map buildRequestingAgencyMessage(PatronRequest pr, Symbol message_sender_symbol, Symbol peer_symbol, String action, String note = null) {
    Map message = buildSkeletonMessage('REQUESTING_AGENCY_MESSAGE')

    message.header = [
      supplyingAgencyId: [
        agencyIdType:peer_symbol.split(":")[0],
        agencyIdValue:peer_symbol.split(":")[1],
      ],
      requestingAgencyId:[
        agencyIdType:message_sender_symbol.split(":")[0],
        agencyIdValue:message_sender_symbol.split(":")[1],
      ],
      requestingAgencyRequestId:pr.hrid ?: pr.id,
      supplyingAgencyRequestId:pr.peerRequestIdentifier,
    ]
    message.activeSection = [
      action: action,
      note: note
    ]

    // Whenever a note is attached to the message, create a notification with action.
    if (note != null) {
      reshareApplicationEventHandlerService.outgoingNotificationEntry(
        pr,
        note,
        action,
        reshareApplicationEventHandlerService.resolveCombinedSymbol(message_sender_symbol),
        reshareApplicationEventHandlerService.resolveCombinedSymbol(peer_symbol),
        true
      )
    }


  }



}