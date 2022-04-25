package org.olf.rs

import static groovyx.net.http.ContentTypes.XML;

import java.time.Instant;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.grails.databinding.xml.GPathResultMap;
import org.olf.okapi.modules.directory.ServiceAccount;
import org.olf.rs.referenceData.Settings;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService;

import groovy.xml.StreamingMarkupBuilder;
import groovyx.net.http.*;

/**
 * Allow callers to request that a protocol message be sent to a remote (Or local) service. Callers
 * provide the requesting and responding symbol and the content of the message, this service works out
 * the most appropriate method/protocol. Initially this will always be loopback.
 *
 */
class ProtocolMessageService {

  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService
  EventPublicationService eventPublicationService
  SettingsService settingsService;
  def grailsApplication

  // Max milliseconds an apache httpd client request can take. initially for sendISO18626Message but may extend to other calls
  // later on
  private static int DEFAULT_TIMEOUT_PERIOD = 30;

    /** The separator for the protocol id to may include to separate the id from the rota position */
    private static String REQUESTER_ID_SEPARATOR = '~';
    private static int REQUESTER_ID_SEPARATOR_LENGTH = REQUESTER_ID_SEPARATOR.length();

    /**
     * Extracts the id of the request from the protocol id
     * @param protocolId The supplied protocol id
     * @return null if it could not extract the id otherwise the id
     */
    public String extractIdFromProtocolId(String protocolId) {
        String id = protocolId;
        if (id != null) {
            // The id may contains the id and rota position
            int separatorPosition = id.indexOf(REQUESTER_ID_SEPARATOR);
            if (separatorPosition > 0) {
                // We found a separator so remove it and everything after it
                id = id.substring(0, separatorPosition);
            }
        }
    }

    /**
     * Attempts to extract the rota position from the protocol id
     * @param protocolId The protocol id we have been supplied with
     * @return a value less than 0 if a rota position was not found, otherwise the rota position
     */
    public long extractRotaPositionFromProtocolId(String protocolId) {
        long rotaPosition = -1;
        if (protocolId != null) {
            // The id may contains the id and rota position
            int separatorPosition = protocolId.indexOf(REQUESTER_ID_SEPARATOR);
            if (separatorPosition > 0) {
                // We found a separator so just take everything after it
                String rotaPositionAsString = protocolId.substring(separatorPosition + REQUESTER_ID_SEPARATOR_LENGTH);

                // Now turn it into an int
                try {
                    rotaPosition = rotaPositionAsString.toLong();
                } catch (Exception e) {
                    // We will ignore all exceptions as it wasn't a string for some reason
                    log.error('Error converting ' + rotaPositionAsString + ' into a rota position from id ' + protocolId);
                }
            }
        }
        return(rotaPosition);
    }

    /**
     * Builds the protocol id from the request
     * @param request The request we want to build the protocol id from
     * @return The protocol id
     */
    public String buildProtocolId(PatronRequest request) {
        return((request.hrid ?: request.id) + REQUESTER_ID_SEPARATOR + request.rotaPosition.toString());
    }

  /**
   * @param eventData : A map structured as followed
   *   event: {
   *     envelope:{
   *       sender:{
   *         symbol:''
   *       }
          recipient:{
   *         symbol:''
   *       }
   *       messageType:''
   *       messageBody:{
   *       }
   *     }
   *   }
   *
   * @return a map containing properties including any confirmationId the underlying protocol implementation provides us
   *
   */
  public Map sendProtocolMessage(String message_sender_symbol, String peer_symbol, Map eventData) {

    Map result = [:]
    Map auditMap = initialiseAuditMap(message_sender_symbol, peer_symbol, eventData);

    def responseConfirmed = messageConfirmation(eventData, "request")
    log.debug("sendProtocolMessage called for ${message_sender_symbol}, ${peer_symbol},${eventData}");
    //Make this create a new request in the responder's system
    String confirmation = null;

    assert eventData != null
    assert eventData.messageType != null;
    assert peer_symbol != null;

    List<ServiceAccount> ill_services_for_peer = findIllServices(peer_symbol)
    log.debug("ILL Services for peer: ${ill_services_for_peer}")

    log.debug("Will send an ISO18626 message to ILL service")

    log.debug("====================================================================")
    log.debug("Event Data: ${eventData}")
    // For now we want to be able to switch between local and actual addresses

    def serviceAddress = null;
    if ( ill_services_for_peer.size() > 0 ) {
      serviceAddress = ill_services_for_peer[0].service.address
    }
    else {
      log.warn("Unable to find ILL service address for ${peer_symbol}");
    }

    try {
      log.debug("Sending ISO18626 message to symbol ${peer_symbol} - resolved address ${serviceAddress}")
      def additional_headers = [:]
      if ( ill_services_for_peer[0].customProperties != null ) {
        log.debug("Service has custom properties: ${ill_services_for_peer[0].customProperties}");
        ill_services_for_peer[0].customProperties.value.each { cp ->
          if ( cp?.definition?.name=='AdditionalHeaders' ) {
            // We need to parse this properly
            cp.value.split(',').each { hdr ->
              def v = hdr.split(':');
              if ( v && v.length == 2 ) {
                additional_headers[v[0]]=v[1]
              }
            }
          }
        }
      }
      result.response = sendISO18626Message(eventData, serviceAddress, additional_headers, auditMap)
      result.status = (result.response.messageStatus == EventISO18626IncomingAbstractService.STATUS_PROTOCOL_ERROR) ? ProtocolResultStatus.ProtocolError : ProtocolResultStatus.Sent;
      log.debug("ISO18626 message sent")
    } catch(Exception e) {
        if ((e.cause != null) && (e.cause instanceof java.net.SocketTimeoutException)) {
            // We have hit a timeout
            result.status = ProtocolResultStatus.Timeout;
        } else {
            // Everything else treated as not sent
            result.status = ProtocolResultStatus.Error;
        }
      log.error("ISO18626 message failed to send. ${e}/${e?.class?.name}/${e.message}",e)
    }

    return result;
  }

  private String mapToEvent(String messageType) {
    String result = null;

    switch ( messageType ) {
      case 'REQUEST':
        result = 'MESSAGE_REQUEST_ind'
        break;
      case 'SUPPLYING_AGENCY_MESSAGE':
        result = 'SUPPLYING_AGENCY_MESSAGE_ind'
        break;
      default:
        log.error("Unhandled event type on incoming protocol message: ${messageType}");
        break;
    }

    assert result != null;

    return result;
  }

  /**
   * @param eventData Symmetrical with the section above. See para on sendProtocolMessage - Map should have exactly the same shape
   * Normally called because a message was received on the wire HOWEVER can be called in a loopback scenario where the peer instition
   * is another tenant in the same re:share installation.
   * eventData should contain a tenantId
   * @return a confirmationId
   */
  public Map handleIncomingMessage(Map eventData) {
    // Recipient must be a tenant in the SharedConfig
    log.debug("handleIncomingMessage called. (eventData.messageType:${eventData.messageType})")

    // Now we issue a protcolMessageIndication event so that any handlers written for the protocol message can be
    // called - this method should not do any work beyond understanding what event needs to be dispatched for the
    // particular message coming in.
    if (eventData.tenant != null) {
      switch ( eventData.messageType ) {
        case 'REQUEST' :
        case 'SUPPLYING_AGENCY_MESSAGE':
          String topic = "${eventData.tenant}_PatronRequestEvents"
          String key = UUID.randomUUID().toString();
          log.debug("publishEvent(${topic},${key},...");
          eventPublicationService.publishAsJSON(topic, key, eventData)
          break;
        default:
          log.warn("Unhandled message type in eventData : ${eventData}")
          break;
      }
    }
    else {
      log.warn("NO tenant in incoming protocol message - don't know how to route it")
    }



    return [
      confirmationId: UUID.randomUUID().toString()
    ]
  }

  public messageConfirmation(eventData, messageType) {
    //TODO make this able to return a confirmation message if request/supplying agency message/requesting agency message are successful,
    //and returning error messages if not
  }

  /**
   * Return a prioroty order list of service accounts this symbol can accept
   */
  public List<ServiceAccount> findIllServices(String symbol) {
    String[] symbol_components = symbol.split(':');

    log.debug("symbol: ${symbol}, symbol components: ${symbol_components}");
    List<ServiceAccount> result = ServiceAccount.executeQuery('''select sa from ServiceAccount as sa
join sa.accountHolder.symbols as symbol
where symbol.symbol=:sym
and symbol.authority.symbol=:auth
and sa.service.businessFunction.value=:ill
''', [ ill:'ill', sym:symbol_components[1], auth:symbol_components[0] ] );

    log.debug("Got service accounts: ${result}");

    return result;
  }


  def makeISO18626Message(Map eventData) {

    // eventData is expected to have a header with structure:
    /*@param header:[
              supplyingAgencyId: [
                agencyIdType:RESHARE,
                agencyIdValue:VLA
              ],
              requestingAgencyId:[
                agencyIdType:OCLC,
                agencyIdValue:ZMU
              ],
              requestingAgencyRequestId:16,
              supplyingAgencyRequestId:8f41a3a4-daa5-4734-9f4f-32578838ff66]
    */

    log.debug("Creating ISO18626 Message")
    log.debug("Message Type: ${eventData.messageType}")
    return{
      ISO18626Message( 'ill:version':'1.0',
                       'xmlns':'http://illtransactions.org/2013/iso18626',
                       'xmlns:ill': 'http://illtransactions.org/2013/iso18626',
                       'xmlns:xsi': 'http://www.w3.org/2001/XMLSchema-instance',
                       'xsi:schemaLocation': 'http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_1.xsd' ) {
        switch (eventData.messageType) {
          case "REQUEST":
            makeRequestBody(delegate, eventData)
            break;
          case "SUPPLYING_AGENCY_MESSAGE":
            makeSupplyingAgencyMessageBody(delegate, eventData)
            break;
          case "REQUESTING_AGENCY_MESSAGE":
            makeRequestingAgencyMessageBody(delegate, eventData)
            break;
          default:
            log.error("UNHANDLED eventData.messageType : ${eventData.messageType}");
            throw new RuntimeException("UNHANDLED eventData.messageType : ${eventData.messageType}");
        }
      log.debug("ISO18626 message created")
      }
    }
  }

  Map sendISO18626Message(Map eventData, String address, Map additionalHeaders = [ : ], Map auditMap = [ : ]) {

    Map result = [ messageStatus: EventISO18626IncomingAbstractService.STATUS_ERROR ];
    StringWriter sw = new StringWriter();
    sw << new StreamingMarkupBuilder().bind (makeISO18626Message(eventData))
    String message = sw.toString();
    log.debug("ISO18626 Message: ${address} ${message} ${additionalHeaders}")

    if ( address != null ) {
      // It is stored as seconds in the settings, so need to multiply by 1000
      int timeoutPeriod = settingsService.getSettingAsInt(Settings.SETTING_NETWORK_TIMEOUT_PERIOD, DEFAULT_TIMEOUT_PERIOD, false) * 1000;

      HttpBuilder http_client = ApacheHttpBuilder.configure {
        // HttpBuilder http_client = configure {

        client.clientCustomizer { HttpClientBuilder builder ->
          RequestConfig.Builder requestBuilder = RequestConfig.custom()
          requestBuilder.connectTimeout = timeoutPeriod;
          requestBuilder.connectionRequestTimeout = timeoutPeriod;
          requestBuilder.socketTimeout = timeoutPeriod;
          builder.defaultRequestConfig = requestBuilder.build()
        }

        request.uri = address
        request.contentType = XML[0]
        request.headers['accept'] = 'application/xml, text/xml'
        additionalHeaders?.each { k,v ->
          request.headers[k] = v
        }
      }

      Date transactionStarted = new Date();
      def iso18626_response = http_client.post {
        request.body = message

        response.failure { FromServer fs ->
          logMessageAudit(transactionStarted, new Date(), address, fs.getStatusCode(), message, auditMap);
          log.error("Got failure response from remote ISO18626 site (${address}): ${fs.getStatusCode()} ${fs}");
          throw new RuntimeException("Failure response from remote ISO18626 service (${address}): ${fs.getStatusCode()} ${fs}");
        }

        response.success { FromServer fs, xml ->
          logMessageAudit(transactionStarted, new Date(), address, fs.getStatusCode(), message, auditMap);
          log.debug("Got OK response: ${fs}");
          if (xml == null) {
              // We did not get an xml response
              result.messageStatus = EventISO18626IncomingAbstractService.STATUS_PROTOCOL_ERROR;
              result.errorData = EventISO18626IncomingAbstractService.ERROR_TYPE_NO_XML_SUPPLIED;
              result.rawData = fs.toString();
          } else {
              GPathResultMap iso18626Response = new GPathResultMap(xml);
              GPathResultMap responseNode = null
              if (iso18626Response.requestConfirmation != null) {
                  // We have a response to a request
                  responseNode = iso18626Response.requestConfirmation;
              } else if (iso18626Response.supplyingAgencyMessageConfirmation != null) {
                  // We have response to a supplier message
                  responseNode = iso18626Response.supplyingAgencyMessageConfirmation;
              } else if (iso18626Response.requestingAgencyMessageConfirmation != null) {
                  // We have a response to a requester message
                  responseNode = iso18626Response.requestingAgencyMessageConfirmation;
              }

              // Did we find a response
              if ((responseNode == null) || (responseNode?.confirmationHeader?.messageStatus == null)) {
                  // We did mot, so mark it as an error
                  result.messageStatus = EventISO18626IncomingAbstractService.STATUS_PROTOCOL_ERROR;
                  result.errorData = EventISO18626IncomingAbstractService.ERROR_TYPE_NO_CONFIRMATION_ELEMENT_IN_RESPONSE;
              } else {
                  // We did so pull back the status and any error data
                  result.messageStatus = responseNode.confirmationHeader.messageStatus;
                  result.errorData = responseNode.confirmationHeader.errorData;
              }

              // Pass back the raw xml, just in case the caller wants to do anything with it
              result.rawData = groovy.xml.XmlUtil.serialize(xml);
          }
        }
      }


      log.debug("Got response message: ${iso18626_response}");
    }
    else {
      log.error("No address for message recipient");
      throw new RuntimeException("No address given for sendISO18626Message. messageData: ${eventData}");
    }
    return(result);
  }

  private Map initialiseAuditMap(String senderSymbol, String receiverSymbol, Map eventData) {
      return([
          senderSymbol: senderSymbol,
          receiverSymbol: receiverSymbol,
          messageType: eventData.messageType,
          action: ((eventData.messageInfo?.reasonForMessage == null) ?
                      ((eventData.activeSection?.action == null) ? '' : eventData.activeSection.action) :
                      eventData.messageInfo.reasonForMessage)
      ]);
  }

  private void logMessageAudit(Date timeStarted, Date timeEnded, String address, Integer result, String message, Map auditMap) {
      String[] messageParts = [
          'ProtocolMessageAudit',
          auditMap.messageType,
          auditMap.action,
          auditMap.senderSymbol,
          auditMap.receiverSymbol,
          result.toString(),
          timeStarted.toString(),
          timeEnded.toString(),
          (timeEnded.getTime() - timeStarted.getTime()).toString(),
          address,
          message.length().toString()
      ]
      log.info(messageParts.join(','));
  }

  void exec ( def del, Closure c ) {
    c.rehydrate(del, c.owner, c.thisObject)()
  }

  void makeRequestBody(def del, eventData) {
    exec(del) {
      request {
        makeHeader(delegate, eventData)

        // Bib info and Service Info only apply to REQUESTS
        log.debug("This is a request message, so needs BibliographicInfo")
        if (eventData.bibliographicInfo != null) {
          makeBibliographicInfo(delegate, eventData)
        } else {
          log.warn("No bibliographicInfo found")
        }

        log.debug("This is a request message, so needs PublicationInfo")
        if (eventData.publicationInfo != null) {
          makePublicationInfo(delegate, eventData)
        } else {
          log.warn("No publicationInfo found")
        }

        log.debug("This is a request message, so needs ServiceInfo")
        if (eventData.serviceInfo != null) {
          makeServiceInfo(delegate, eventData)
        } else {
          log.warn("No serviceInfo found")
        }

        //TODO Wire in supplierInfo here

        //TODO Put this logic into a maker method like the others
        requestedDeliveryInfo {
          address {
            if ( ( eventData.requestedDeliveryInfo?.address != null ) &&
                 ( eventData.requestedDeliveryInfo?.address?.physicalAddress != null ) ) {
              physicalAddress {
                line1(eventData.requestedDeliveryInfo.address.physicalAddress.line1)
                line2(eventData.requestedDeliveryInfo.address.physicalAddress.line2)
                locality(eventData.requestedDeliveryInfo.address.physicalAddress.locality)
                postalCode(eventData.requestedDeliveryInfo.address.physicalAddress.postalCode)
                region(eventData.requestedDeliveryInfo.address.physicalAddress.region)
                county(eventData.requestedDeliveryInfo.address.physicalAddress.county)
              }
            }
          }
        }

        //TODO Wire in requestingAgencyInfo here

        log.debug("This is a requesting message, so needs PatronInfo")
        if (eventData.patronInfo != null) {
          makePatronInfo(delegate, eventData)
        } else {
          log.warn("No patronInfo found")
        }

        //TODO Wire in billingInfo here
      }
    }
  }

  void makeSupplyingAgencyMessageBody(def del, eventData) {
    exec(del) {
      supplyingAgencyMessage {
        makeHeader(delegate, eventData)

        log.debug("This is a supplying agency message, so we need MessageInfo, StatusInfo, DeliveryInfo")
        if (eventData.messageInfo != null) {
          makeMessageInfo(delegate, eventData)
        } else {
          log.warn("No messageInfo found")
        }
        if (eventData.statusInfo != null) {
          makeStatusInfo(delegate, eventData)
        } else {
          log.warn("No statusInfo found")
        }
        if (eventData.deliveryInfo != null) {
          makeDeliveryInfo(delegate, eventData)
        } else {
          log.warn("No deliveryInfo found")
        }
        if (eventData.returnInfo != null) {
          makeReturnInfo(delegate, eventData)
        } else {
          log.warn("No returnInfo found")
        }
      }
    }
  }

  void makeRequestingAgencyMessageBody(def del, eventData) {
    exec(del) {
      requestingAgencyMessage {
        makeHeader(delegate, eventData)

        log.debug("This is a requesting agency message, so we need ActiveSection")
        if (eventData.activeSection != null) {
          makeActiveSection(delegate, eventData)
        } else {
          log.warn("No activeSection found")
        }
      }
    }
  }

  void makeHeader(def del, eventData) {
    exec(del) {
      header {
        supplyingAgencyId {
          agencyIdType(eventData.header.supplyingAgencyId.agencyIdType)
          agencyIdValue(eventData.header.supplyingAgencyId.agencyIdValue)
        }
        requestingAgencyId {
          agencyIdType(eventData.header.requestingAgencyId.agencyIdType)
          agencyIdValue(eventData.header.requestingAgencyId.agencyIdValue)
        }
        timestamp(Instant.now()) // Current time
        requestingAgencyRequestId(eventData.header.requestingAgencyRequestId)
        if (eventData.messageType == "SUPPLYING_AGENCY_MESSAGE" || eventData.messageType == "REQUESTING_AGENCY_MESSAGE") {
          supplyingAgencyRequestId(eventData.header.supplyingAgencyRequestId)
        }
        if (eventData.messageType == "REQUESTING_AGENCY_MESSAGE") {
          requestingAgencyAuthentication(eventData.header.requestingAgencyAuthentication)
        }
      }
    }
  }

  void makeBibliographicInfo(def del, eventData) {
    exec(del) {
      bibliographicInfo {
        title(eventData.bibliographicInfo.title)
        subtitle(eventData.bibliographicInfo.subtitle)
        author(eventData.bibliographicInfo.author)
        sponsoringBody(eventData.bibliographicInfo.sponsoringBody)
        volume(eventData.bibliographicInfo.volume)
        issue(eventData.bibliographicInfo.issue)
        startPage(eventData.bibliographicInfo.startPage)
        numberOfPages(eventData.bibliographicInfo.numberOfPages)
        edition(eventData.bibliographicInfo.edition)
        issn(eventData.bibliographicInfo.issn)
        isbn(eventData.bibliographicInfo.isbn)
        doi(eventData.bibliographicInfo.doi)
        coden(eventData.bibliographicInfo.coden)
        sici(eventData.bibliographicInfo.sici)
        bici(eventData.bibliographicInfo.bici)
        eissn(eventData.bibliographicInfo.eissn)
        stitle(eventData.bibliographicInfo.stitle)
        part(eventData.bibliographicInfo.part)
        artnum(eventData.bibliographicInfo.artnum)
        ssn(eventData.bibliographicInfo.ssn)
        quarter(eventData.bibliographicInfo.quarter)
        titleOfComponent(eventData.bibliographicInfo.titleOfComponent)
        authorOfComponent(eventData.bibliographicInfo.authorOfComponent)
        sponsor(eventData.bibliographicInfo.sponsor)
        informationSource(eventData.bibliographicInfo.informationSource)

        //
        supplierUniqueRecordId(eventData.bibliographicInfo.supplierUniqueRecordId)
        bibliographicRecordId(eventData.bibliographicInfo.bibliographicRecordId)


        // Pretty sure this shouldn't be here
        systemInstanceIdentifier(eventData.bibliographicInfo.systemInstanceIdentifier)
        requestingAgencyRequestId(eventData.header.requestingAgencyRequestId)
        supplier(eventData.bibliographicInfo.supplyingInstitutionSymbol)
        requester(eventData.bibliographicInfo.requestingInstitutionSymbol)

        // Should this be here?
        patronNote(eventData.bibliographicInfo.patronNote)

        oclcNumber(eventData.bibliographicInfo.oclcNumber)
      }
    }
  }

  void makePublicationInfo(def del, eventData) {
    exec(del) {
      publicationInfo {
        publisher(eventData.publicationInfo.publisher)
        publicationDate(eventData.publicationInfo.publicationDate)
        publicationDateOfComponent(eventData.publicationInfo.publicationDateOfComponent)
        publicationType(eventData.publicationInfo.publicationType)
        placeOfPublication(eventData.publicationInfo.placeOfPublication)
      }
    }
  }

    void makeServiceInfo(def del, eventData) {
    exec(del) {
      serviceInfo {
        serviceType(eventData.serviceInfo.serviceType)
        needBeforeDate(eventData.serviceInfo.needBeforeDate)
        serviceLevel(eventData.serviceInfo.serviceLevel)
        anyEdition(eventData.serviceInfo.anyEdition)
        note(eventData.serviceInfo.note)
      }
    }
  }

  void makePatronInfo(def del, eventData) {
    exec(del) {
      patronInfo {
        patronId(eventData.patronInfo.patronId)
        surname(eventData.patronInfo.surname)
        givenName(eventData.patronInfo.givenName)
        patronType(eventData.patronInfo.patronType)
        patronReference(eventData.patronInfo.patronReference)
      }
    }
  }

  void makeMessageInfo(def del, eventData) {
    exec(del) {
      messageInfo {
        reasonForMessage(eventData.messageInfo.reasonForMessage)
        answerYesNo(eventData.messageInfo.answerYesNo)
        note(eventData.messageInfo.note)
        reasonUnfilled(eventData.messageInfo.reasonUnfilled)
        reasonRetry(eventData.messageInfo.reasonRetry)
        offeredCosts(eventData.messageInfo.offeredCosts)
        retryAfter(eventData.messageInfo.retryAfter)
        retryBefore(eventData.messageInfo.retryBefore)
      }
    }
  }

  void makeActiveSection(def del, eventData) {
    exec(del) {
      activeSection {
        action(eventData.activeSection.action)
        note(eventData.activeSection.note)
      }
    }
  }

  void makeStatusInfo(def del, eventData) {
    exec(del) {
      statusInfo {
        status(eventData.statusInfo.status)
        expectedDeliveryDate(eventData.statusInfo.expectedDeliverydate)
        dueDate(eventData.statusInfo.dueDate)
        lastChange(eventData.statusInfo.lastChange)
      }
    }
  }

  void makeDeliveryInfo(def del, eventData) {
    exec(del) {
      deliveryInfo {
        dateSent(eventData.deliveryInfo.dateSent)
        if (eventData.deliveryInfo.itemId instanceof Collection) {
          // Build multiple ItemIds
          eventData.deliveryInfo.itemId.collect {iid ->
            itemId(iid)
          }
        } else {
          // Build single ItemId
          itemId(eventData.deliveryInfo.itemId)
        }
        sentVia(eventData.deliveryInfo.sentVia)
        sentToPatron(eventData.deliveryInfo.sentToPatron)
        loanCondition(eventData.deliveryInfo.loanCondition)
        deliveredFormat(eventData.deliveryInfo.deliveredFormat)
        deliveryCosts(eventData.deliveryInfo.deliveryCosts)
      }
    }
  }

  void makeReturnInfo(def del, eventData) {
    exec(del) {
      returnInfo {
        returnAgencyId(eventData.returnInfo.returnAgencyId)
        name(eventData.returnInfo.name)
        physicalAddress(eventData.returnInfo.physicalAddress)
      }
    }
  }
}
