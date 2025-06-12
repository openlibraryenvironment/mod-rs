package org.olf.rs

import groovy.util.logging.Slf4j
import org.olf.rs.iso18626.TypeStatus
import org.olf.rs.referenceData.SettingsData
import org.olf.rs.statemodel.ActionEventResultQualifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.iso18626.NoteSpecials;

@Slf4j
class ProtocolMessageBuildingService {

    private static final String ALL_REGEX           = '(.*)';
    private static final String NUMBER_REGEX        = '(\\d+)';
    private static final String END_OF_STRING_REGEX = '$'
    private static final String SEQUENCE_REGEX      = ALL_REGEX + NoteSpecials.SEQUENCE_PREFIX + NUMBER_REGEX + NoteSpecials.SPECIAL_WRAPPER + END_OF_STRING_REGEX;
    private static final String LAST_SEQUENCE_REGEX = ALL_REGEX + NoteSpecials.LAST_SEQUENCE_PREFIX + NUMBER_REGEX + NoteSpecials.SPECIAL_WRAPPER + END_OF_STRING_REGEX;
    private static final String VOLUME_STATUS_ILS_REQUEST_CANCELLED = 'ils_request_cancelled'

  /*
   * This method is purely for building out the structure of protocol messages
   * into an eventdata map, before they're handed off to the protocol message
   * service for sending. This service shouldn't do any other work.
   *
  */

  NewDirectoryService newDirectoryService
  ProtocolMessageService protocolMessageService
  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  ReshareActionService reshareActionService
  SettingsService settingsService

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


  public Map buildRequestMessage(PatronRequest req, boolean appendSequence = true) {
      Map message = buildSkeletonMessage('REQUEST')
      String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);

      if (req.resolvedRequester != null) {
          message.header = buildHeader(req, 'REQUEST', req.resolvedRequester, null)
      } else {
          message.header = buildHeader(req, 'REQUEST', defaultRequestSymbolString, null);
      }

    //List bibliographicItemIdList = [ [ scheme:'oclc', identifierCode:'oclc', identifierValue: req.oclcNumber ] ];
      List bibliographicItemIdList = [ ];
      if (req.precededBy) {
          bibliographicItemIdList.add([scheme:'reshare', bibliographicItemIdentifierCode:'preceded-by', bibliographicItemIdentifier: req.precededBy.hrid])
      }
    /*
    if (req.succeededBy) {
        bibliographicItemIdList.add([scheme:'reshare', identifierCode:'succeeded-by', identifierValue: req.succeededBy.hrid])
    }

     */

    message.bibliographicInfo = [
      title: req.title,
      author: req.author,
      subtitle: req.subtitle,
      volume: req.volume,
      issue: req.issue,
      pagesRequested: req.pagesRequested,
      edition: req.edition,
      bibliographicRecordId:[
              [ bibliographicRecordIdentifierCode:'bibliographicRecordId', bibliographicRecordIdentifier: req.bibliographicRecordId ?: req.systemInstanceIdentifier ],
              [ bibliographicRecordIdentifierCode:'requestingInstitutionSymbol', bibliographicRecordIdentifier: req.requestingInstitutionSymbol ],
              [ bibliographicRecordIdentifierCode:'sponsoringBody', bibliographicRecordIdentifier: req.sponsoringBody ],
              [ bibliographicRecordIdentifierCode:'startPage', bibliographicRecordIdentifier: req.startPage ],
              [ bibliographicRecordIdentifierCode:'numberOfPages', bibliographicRecordIdentifier: req.numberOfPages ],
              [ bibliographicRecordIdentifierCode:'doi', bibliographicRecordIdentifier: req.doi ],
              [ bibliographicRecordIdentifierCode:'coden', bibliographicRecordIdentifier: req.coden ],
              [ bibliographicRecordIdentifierCode:'sici', bibliographicRecordIdentifier: req.sici ],
              [ bibliographicRecordIdentifierCode:'bici', bibliographicRecordIdentifier: req.bici ],
              [ bibliographicRecordIdentifierCode:'eissn', bibliographicRecordIdentifier: req.eissn ],
              [ bibliographicRecordIdentifierCode:'stitle', bibliographicRecordIdentifier: req.stitle ],
              [ bibliographicRecordIdentifierCode:'part', bibliographicRecordIdentifier: req.part ],
              [ bibliographicRecordIdentifierCode:'artnum', bibliographicRecordIdentifier: req.artnum ],
              [ bibliographicRecordIdentifierCode:'ssn', bibliographicRecordIdentifier: req.ssn ],
              [ bibliographicRecordIdentifierCode:'quarter', bibliographicRecordIdentifier: req.quarter ],
              [ bibliographicRecordIdentifierCode:'systemInstanceIdentifier', bibliographicRecordIdentifier: req.systemInstanceIdentifier ],
              [ bibliographicRecordIdentifierCode:'oclcNumber', bibliographicRecordIdentifier: req.oclcNumber ],
              [ bibliographicRecordIdentifierCode:'patronReference', bibliographicRecordIdentifier: req.patronReference ]
      ],  // Shared index bib record ID (Instance identifier)
      bibliographicItemId: [[ bibliographicItemIdentifierCode:'issn', bibliographicItemIdentifier: req.issn ],
                            [ bibliographicItemIdentifierCode:'isbn', bibliographicItemIdentifier: req.isbn ]] + bibliographicItemIdList,
      titleOfComponent: req.titleOfComponent,
      authorOfComponent: req.authorOfComponent,
      sponsor: req.sponsor,
      informationSource: req.informationSource,
      //supplierUniqueRecordId: null,   // Set later on from rota where we store the supplier id
      supplierUniqueRecordId: req.isRequester ? req.supplierUniqueRecordId : null

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
            
      copyrightCompliance: req.copyrightType?.value,

      serviceType: req.serviceType?.value,

      serviceLevel: req.serviceLevel?.value,

      anyEdition: 'Y',

      // Note that the internal names sometimes differ from the protocol names--pay attention with these fields
      needBeforeDate: req.neededBy,
      note: buildNote(req, req.patronNote, appendSequence)

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

    String requestRouterSetting = settingsService.getSettingValue(SettingsData.SETTING_ROUTING_ADAPTER);

    // Since ISO18626-2017 doesn't yet offer DeliveryMethod here we encode it as an ElectronicAddressType
    if (req.deliveryMethod?.value == 'url') {
      message.requestedDeliveryInfo = [
        address: [
          electronicAddress: [
            electronicAddressType: req.deliveryMethod?.value,
            electronicAddressData: req.pickupURL
          ]
        ]
      ]
    } else if (requestRouterSetting == "disabled") {
        def pickupEntry = newDirectoryService.branchEntryByNameAndParentSymbol(req.pickupLocation, req.requestingInstitutionSymbol);
        def physicalAddress = newDirectoryService.shippingAddressMapForEntry(pickupEntry, req.pickupLocation);
        if (!physicalAddress) {
            def parentPickupEntry = newDirectoryService.institutionEntryBySymbol(req.requestingInstitutionSymbol);
            physicalAddress = newDirectoryService.shippingAddressMapForEntry(parentPickupEntry, req.pickupLocation);
        }
        if (physicalAddress) {
            message.requestedDeliveryInfo = [
                address: [
                    physicalAddress: physicalAddress
                ]
            ]
        }
    } else {
      message.requestedDeliveryInfo = [
        // SortOrder
        address:[
          physicalAddress:[
            line1:req.pickupLocation,
            line2:null,
            locality:null,
            postalCode:null,
            region:null,
            country:null
          ]
        ]
      ]
    }


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
      /* Also permitted:
       * SendToPatron
       * Address
      */
     ]

     Map maximumCosts = null;
      if ( req.maximumCostsCurrencyCode?.value && req.maximumCostsMonetaryValue) {
          maximumCosts = [:];
          maximumCosts.monetaryValue = req.maximumCostsMonetaryValue;
          maximumCosts.currencyCode = req.maximumCostsCurrencyCode?.value;
      }
     message.billingInfo = [
      /*
      * Permitted fields:
      * PaymentMethod
      * MaximumCosts
      * BillingMethod
      * BillingName
      * Address
      */
        maximumCosts : maximumCosts

      ]

    return message;
  }

  public Map buildSupplyingAgencyMessage(PatronRequest pr,
                                         String reason_for_message,
                                         String status,
                                         Map messageParams,
                                         boolean appendSequence) {

    Map message = buildSkeletonMessage('SUPPLYING_AGENCY_MESSAGE')

      String requestRouterSetting = settingsService.getSettingValue('routing_adapter');
      String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);
      String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);

      boolean routingDisabled = (requestRouterSetting == 'disabled');

      if (routingDisabled) {
          message.header = buildHeader(pr, 'SUPPLYING_AGENCY_MESSAGE', defaultRequestSymbolString, defaultPeerSymbolString)
      } else {
          message.header = buildHeader(pr, 'SUPPLYING_AGENCY_MESSAGE', pr.resolvedSupplier, pr.resolvedRequester)
      }
    message.messageInfo = [
      reasonForMessage:reason_for_message,
      note: buildNote(pr, messageParams?.note, appendSequence)
    ]
    message.statusInfo = [
      status:status
    ]

    if ( messageParams?.reason ) {
      message.messageInfo.reasonUnfilled = messageParams?.reason
    }

    if ( messageParams?.cancelResponse ) {
      if (messageParams.cancelResponse == "yes") {
        message.messageInfo.answerYesNo = "Y"
      } else {
        message.messageInfo.answerYesNo = "N"
      }
    }

    // We need to check in a couple of places whether the note is null/whether to add a note
    String note = messageParams?.note
    message.deliveryInfo = [:]
    if ( messageParams?.loanCondition ) {
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

    boolean isUrlDelivery = false;
    if (messageParams?.deliveredFormat) {
      message.deliveryInfo['deliveredFormat'] = messageParams.deliveredFormat
      if (messageParams.url) {
          //this needs to go into itemId instead
          message.deliveryInfo['url'] = messageParams.url;
          message.deliveryInfo['itemId'] = messageParams.url;
          isUrlDelivery = true;
      }
    }

    if (!TypeStatus.CANCELLED.value().equalsIgnoreCase(status) &&
            !TypeStatus.UNFILLED.value().equalsIgnoreCase(status)) {
        Set<RequestVolume> filteredVolumes = pr.volumes.findAll { rv ->
            rv.status.value != VOLUME_STATUS_ILS_REQUEST_CANCELLED
        }
        if (!isUrlDelivery) {
            switch (filteredVolumes.size()) {
                case 0:
                    break;
                case 1:
                    // We have a single volume, send as a single itemId string
                    message.deliveryInfo['itemId'] = "${filteredVolumes[0].name},${filteredVolumes[0].callNumber ? filteredVolumes[0].callNumber : ""},${filteredVolumes[0].itemId}"
                    break;
                default:
                    // We have many volumes, send as an array of multiVol itemIds
                    message.deliveryInfo['itemId'] = filteredVolumes.collect { vol -> "multivol:${vol.name},${vol.callNumber ? vol.callNumber : ""},${vol.itemId}" }
                    break;
            }
        }
    }

    if( pr?.dueDateRS ) {
      message.statusInfo['dueDate'] = pr.dueDateRS
    }

    return message
  }


  public Map buildRequestingAgencyMessage(PatronRequest pr, String message_sender, String peer, String action, String note, boolean appendSequence = true) {
      Map message = buildSkeletonMessage('REQUESTING_AGENCY_MESSAGE')

      String requestRouterSetting = settingsService.getSettingValue('routing_adapter');
      String defaultPeerSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_PEER_SYMBOL);
      String defaultRequestSymbolString = settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL);

      Symbol message_sender_symbol = DirectoryEntryService.resolveCombinedSymbol(message_sender)
      Symbol peer_symbol = DirectoryEntryService.resolveCombinedSymbol(peer)

      if (requestRouterSetting != 'disabled') {
          message.header = buildHeader(pr, 'REQUESTING_AGENCY_MESSAGE', message_sender_symbol, peer_symbol)
      } else {
          message.header = buildHeader(pr, 'REQUESTING_AGENCY_MESSAGE', defaultRequestSymbolString, defaultPeerSymbolString)
      }

      message.action = action
      message.note = buildNote(pr, note, appendSequence)

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

  /**
   * Extracts the last sequence number from the note field
   * @param note The note that may contain the last sequence
   * @return A map containing the following fields
   *    1. note without the sequence
   *    2. sequence the found sequence
   * if no sequence is found then the sequence will be null
   */
  public Map extractLastSequenceFromNote(String note) {
      return(extractSequence(note, LAST_SEQUENCE_REGEX))
  }

  /**
   * Extracts the sequence number from the note field
   * @param note The note that may contain the last sequence
   * @return A map containing the following fields
   *    1. note without the sequence
   *    2. sequence the found sequence
   * if no sequence is found then the sequence will be null
   */
  public Map extractSequenceFromNote(String note) {
      return(extractSequence(note, SEQUENCE_REGEX))
  }

  /**
   * Builds the last sequence string for our hack to determine if the message was received or not
   * @param request The request we want the last sequence we sent from
   * @return The last sequence sent wrapped in the appropriate format to be set in the note field
   */
  public String buildLastSequence(PatronRequest request) {
      String lastSequenceSent = request.lastSequenceSent == null ? "-1" : request.lastSequenceSent.toString();
      return(NoteSpecials.LAST_SEQUENCE_PREFIX + lastSequenceSent + NoteSpecials.SPECIAL_WRAPPER);
  }

  /**
   * Extracts the sequence number from the note field
   * @param note The note that may contain the last sequence
   * @param sequenceRegex The regex used to obtain the sequence (group 2) and the note (group 1)
   * @return A map containing the following fields
   *    1. note without the sequence
   *    2. sequence the found sequence
   * if no sequence is found then the sequence will be null
   */
  private Map extractSequence(String note, String sequenceRegex) {
      Map result = [ note: note];

      // If we havn't been supplied a note then there is nothing to extract
      if (note != null) {
          // We use Pattern.DOTALL in case there are newlines in the string
          Pattern pattern = Pattern.compile(sequenceRegex, Pattern.DOTALL);
          Matcher matcher = pattern.matcher(note);
          if (matcher.find())
          {
              try {
                  // The sequence matches on the 2nd group
                  String sequenceAsString = matcher.group(2);
                  if (sequenceAsString != null) {
                      // Convert to an integer
                      result.sequence = sequenceAsString.toInteger();

                      // Grab the actual note from the first group as the sequence is always at the end of the note
                      result.note = matcher.group(1);

                      // Need to ensure the note is not blank
                      if (result.note.length() == 0) {
                          // We need to make it null
                          result.note = null;
                      }
                  }
              } catch (Exception ) {
                  // We ignore any exception thrown, as it means it wasn't what we were expecting
              }
          }
      }

      // Return the note and sequence to the caller
      return(result);
  }

  private String buildNote(PatronRequest request, String note, boolean appendSequence) {
      String constructedNote = note;

      // Now do we need to append the sequence
      if (appendSequence) {
          String lastSequence = NoteSpecials.SEQUENCE_PREFIX + request.incrementLastSequence().toString() + NoteSpecials.SPECIAL_WRAPPER;
          if (constructedNote == null) {
              constructedNote = lastSequence;
          } else {
              constructedNote += lastSequence;
          }
      }
      return(constructedNote);
  }


    private Map buildHeader(PatronRequest pr, String messageType, String message_sender_symbol, String peer_symbol) {
        Map supplyingAgencyId
        Map requestingAgencyId
        String requestingAgencyRequestId
        String supplyingAgencyRequestId

        log.debug("ProtocolMessageBuildingService::buildHeader(${pr}, ${messageType}, ${message_sender_symbol}, ${peer_symbol})");


        if (messageType == 'REQUEST' || messageType == 'REQUESTING_AGENCY_MESSAGE') {

            // Set the requestingAgencyId and the requestingAgencyRequestId
            requestingAgencyId = buildHeaderRequestingAgencyId(message_sender_symbol)
            requestingAgencyRequestId = protocolMessageService.buildProtocolId(pr, pr.stateModel?.shortcode);

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
            requestingAgencyRequestId = pr.peerRequestIdentifier ?: protocolMessageService.buildProtocolId(pr, pr.stateModel?.shortcode)
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

  private Map buildHeader(PatronRequest pr, String messageType, Symbol message_sender_symbol, Symbol peer_symbol) {
    Map supplyingAgencyId
    Map requestingAgencyId
    String requestingAgencyRequestId
    String supplyingAgencyRequestId

    log.debug("ProtocolMessageBuildingService::buildHeader(${pr}, ${messageType}, ${message_sender_symbol}, ${peer_symbol})");


    if (messageType == 'REQUEST' || messageType == 'REQUESTING_AGENCY_MESSAGE') {

      // Set the requestingAgencyId and the requestingAgencyRequestId
      requestingAgencyId = buildHeaderRequestingAgencyId(message_sender_symbol)
      requestingAgencyRequestId = protocolMessageService.buildProtocolId(pr, pr.stateModel?.shortcode);

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

    private Map buildHeaderSupplyingAgencyId(String supplier) {
        def (auth,sym) = supplier.split(":",2);
        Map supplyingAgencyId = [
                agencyIdType: auth,
                agencyIdValue: sym
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

    private Map buildHeaderRequestingAgencyId(String requester) {
        def (auth,sym) = requester.split(":",2);
        Map requestingAgencyId = [
                agencyIdType: auth,
                agencyIdValue: sym
        ]
        return requestingAgencyId;
    }

}
