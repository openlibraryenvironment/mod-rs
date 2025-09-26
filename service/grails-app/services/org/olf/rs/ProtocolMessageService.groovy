package org.olf.rs

import org.olf.rs.iso18626.Address
import org.olf.rs.iso18626.BibliographicInfo
import org.olf.rs.iso18626.BibliographicItemId
import org.olf.rs.iso18626.BibliographicRecordId
import org.olf.rs.iso18626.BillingInfo
import org.olf.rs.iso18626.DeliveryInfo
import org.olf.rs.iso18626.ElectronicAddress
import org.olf.rs.iso18626.Header
import org.olf.rs.iso18626.ISO18626Message
import org.olf.rs.iso18626.MessageInfo
import org.olf.rs.iso18626.ObjectFactory
import org.olf.rs.iso18626.PatronInfo
import org.olf.rs.iso18626.PhysicalAddress
import org.olf.rs.iso18626.PublicationInfo
import org.olf.rs.iso18626.Request
import org.olf.rs.iso18626.RequestedDeliveryInfo
import org.olf.rs.iso18626.RequestingAgencyMessage
import org.olf.rs.iso18626.ReturnInfo
import org.olf.rs.iso18626.ServiceInfo
import org.olf.rs.iso18626.StatusInfo
import org.olf.rs.iso18626.SupplyingAgencyMessage
import org.olf.rs.iso18626.TypeAction
import org.olf.rs.iso18626.TypeAgencyId
import org.olf.rs.iso18626.TypeCosts
import org.olf.rs.iso18626.TypeReasonForMessage
import org.olf.rs.iso18626.TypeSchemeValuePair
import org.olf.rs.iso18626.TypeServiceType
import org.olf.rs.iso18626.TypeStatus
import org.olf.rs.iso18626.TypeYesNo
import org.olf.rs.statemodel.StateModel
import org.xml.sax.SAXException

import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import static groovyx.net.http.ContentTypes.XML

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.grails.databinding.xml.GPathResultMap;
import org.olf.okapi.modules.directory.ServiceAccount;
import org.olf.rs.logging.IIso18626LogDetails;
import org.olf.rs.logging.ProtocolAuditService;
import org.olf.rs.referenceData.SettingsData;
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService
import groovyx.net.http.*;

/**
 * Allow callers to request that a protocol message be sent to a remote (Or local) service. Callers
 * provide the requesting and responding symbol and the content of the message, this service works out
 * the most appropriate method/protocol. Initially this will always be loopback.
 *
 */
class ProtocolMessageService {
  private static final Set<String> RECORD_ID_CODES = new HashSet<>(['amicus', 'bl', 'faust', 'jnb', 'la', 'lccn', 'medline', 'ncid', 'oclc', 'pid', 'pmid', 'tp'])

  ReshareApplicationEventHandlerService reshareApplicationEventHandlerService;
  EventPublicationService eventPublicationService;
  ProtocolAuditService protocolAuditService;
  SettingsService settingsService;
  Iso18626MessageValidationService iso18626MessageValidationService
  JAXBContext context = JAXBContext.newInstance(ObjectFactory.class)
  
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
        if (id.length() > 32) {
          if (id.contains("-")) {
            id = id.substring(0, id.lastIndexOf('-'))
          } else {
            id = id.substring(0, 32)
          }
        }
        return id
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
    public String buildProtocolId(PatronRequest request, String stateModel) {
        String id = (request.hrid ?: request.id)
        String order = request.rotaPosition?.toString() ?: "norota";
        String suffix = (stateModel == StateModel.MODEL_SLNP_REQUESTER ||
                stateModel == StateModel.MODEL_SLNP_RESPONDER) ? "" :
                (REQUESTER_ID_SEPARATOR + order)
        return id + suffix

    }

    /**
     * @param eventData : A map structured as followed
     *   event: {
     *     envelope:{
     *       sender:{
     *         symbol:''
     *       }
     *       recipient:{
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
    public Map sendProtocolMessage(PatronRequest patronRequest, String message_sender_symbol, String peer_symbol, Map eventData) {
        IIso18626LogDetails iso18626LogDetails= protocolAuditService.getIso18626LogDetails();
        Map result = sendProtocolMessage(message_sender_symbol, peer_symbol, eventData, iso18626LogDetails);
        protocolAuditService.save(patronRequest, iso18626LogDetails);
        return(result);
    }

  public Map sendProtocolMessage(String message_sender_symbol, String peer_symbol, Map eventData, IIso18626LogDetails iso18626LogDetails) {

    Map result = [:]
    Map auditMap = initialiseAuditMap(message_sender_symbol, peer_symbol, eventData);

    def responseConfirmed = messageConfirmation(eventData, "request")
    log.debug("sendProtocolMessage called for ${message_sender_symbol}, ${peer_symbol},${eventData}");
    //Make this create a new request in the responder's system
    String confirmation = null;

    assert eventData != null
    assert eventData.messageType != null;

    List<ServiceAccount> ill_services_for_peer;

    if (peer_symbol != null) {
      ill_services_for_peer = findIllServices(peer_symbol);
    } else {
      ill_services_for_peer = [];
    }
    log.debug("ILL Services for peer: ${ill_services_for_peer}")

    log.debug("Will send an ISO18626 message to ILL service")

    log.debug("====================================================================")
    log.debug("Event Data: ${eventData}")
    // For now we want to be able to switch between local and actual addresses

    def serviceAddress = null;
    if ( ill_services_for_peer.size() > 0 ) {
      serviceAddress = ill_services_for_peer[0].service.address
    } else {
      serviceAddress = settingsService.getSettingValue(SettingsData.SETTING_NETWORK_ISO18626_GATEWAY_ADDRESS)
      log.info("Unable to find ILL service address for ${peer_symbol}. Use default ${serviceAddress}");
    }

    try {
      log.debug("Sending ISO18626 message to symbol ${peer_symbol} - resolved address ${serviceAddress}")
      def additional_headers = [:]
      if ( ill_services_for_peer.size() > 0 && ill_services_for_peer[0].customProperties != null ) {
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
      result.response = sendISO18626Message(eventData, serviceAddress, additional_headers, auditMap, iso18626LogDetails);
      result.status = (result.response.messageStatus == EventISO18626IncomingAbstractService.STATUS_PROTOCOL_ERROR) ? ProtocolResultStatus.ProtocolError : ProtocolResultStatus.Sent;
      log.debug("ISO18626 message sent")
    } catch (SAXException e){
      result.status = ProtocolResultStatus.ValidationError
      result.response = "Request validation failed: ${e.message}"
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
   * Return a list of service accounts this symbol can accept
   */
  public List<ServiceAccount> findIllServices(String symbolStr) {
    List<ServiceAccount> result = [];
    def sym = DirectoryEntryService.resolveCombinedSymbol(symbolStr)

    if (sym == null) {
      log.warn("Attempted to find ILL service accounts for unknown symbol ${symbolStr}");
    } else {

      log.debug("Finding ILL service accounts for ${sym?.symbol}")
      try {
        def criteria = ServiceAccount.where {
          accountHolder == sym.owner && service.businessFunction.value == 'ill'
        }
        result = criteria.list()
        log.debug("Got service accounts: ${result}");
      } catch (Exception e) {
        log.error("Unable to find service accounts for symbol ${sym?.symbol}: ${e.getLocalizedMessage()}");
      }
    }
    return result;
  }


  def makeISO18626Message(Map eventData) {
    log.debug("Creating ISO18626 Message")
    log.debug("Message Type: ${eventData.messageType}")
    ISO18626Message message = new ISO18626Message()
    message.setVersion(Iso18626Constants.VERSION)
    switch (eventData.messageType) {
      case Iso18626Constants.REQUEST:
        message.setRequest(makeRequest(eventData))
        break
      case Iso18626Constants.SUPPLYING_AGENCY_MESSAGE:
        message.setSupplyingAgencyMessage(makeSupplyingAgencyMessageBody(eventData))
        break
      case Iso18626Constants.REQUESTING_AGENCY_MESSAGE:
        message.setRequestingAgencyMessage(makeRequestingAgencyMessageBody(eventData))
        break
      default:
        log.error("UNHANDLED eventData.messageType : ${eventData.messageType}")
        throw new RuntimeException("UNHANDLED eventData.messageType : ${eventData.messageType}")
    }
    log.debug("ISO18626 message created")
    return message
  }


  void marshalIsoMessage(ISO18626Message isoMessage, StringWriter stringWriter) {
    log.debug("Attempting to marshal ISO message");
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, Iso18626Constants.SCHEMA_LOCATION);
    marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new IllNamespacePrefixMapper());

    marshaller.marshal(isoMessage, stringWriter);
  }

  protected Map sendISO18626Message(Map eventData, String address, Map additionalHeaders, Map auditMap, IIso18626LogDetails iso18626LogDetails) {

    Map result = [ messageStatus: EventISO18626IncomingAbstractService.STATUS_ERROR ]
    StringWriter sw = new StringWriter()
    ISO18626Message isoMessage = makeISO18626Message(eventData);
    marshalIsoMessage(isoMessage, sw);
    String message = sw.toString();
    log.debug("ISO18626 Message: ${address} ${message} ${additionalHeaders}")
    iso18626MessageValidationService.validateAgainstXSD(message);

    if ( address != null ) {
      // It is stored as seconds in the settings, so need to multiply by 1000
      int timeoutPeriod = settingsService.getSettingAsInt(SettingsData.SETTING_NETWORK_TIMEOUT_PERIOD, DEFAULT_TIMEOUT_PERIOD, false) * 1000;

      // Audit this message
      iso18626LogDetails.request(address, message);

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
          String respomseStatus = fs.getStatusCode().toString() + " " + fs.getMessage();
          iso18626LogDetails.response(respomseStatus, fs.hasBody ? fs.toString() : null);
          throw new RuntimeException("Failure response from remote ISO18626 service (${address}): ${fs.getStatusCode()} ${fs}");
        }

        response.success { FromServer fs, xml ->
          String respomseStatus = fs.getStatusCode().toString() + " " + fs.getMessage();
          logMessageAudit(transactionStarted, new Date(), address, fs.getStatusCode(), message, auditMap);
          log.debug("Got OK response: ${fs}");
          if (xml == null) {
              // We did not get an xml response
              result.messageStatus = EventISO18626IncomingAbstractService.STATUS_PROTOCOL_ERROR;
              result.errorData = EventISO18626IncomingAbstractService.ERROR_TYPE_NO_XML_SUPPLIED;
              result.rawData = fs.toString();
              iso18626LogDetails.response(respomseStatus, fs.hasBody ? fs.toString() : null);
          } else {
              // Pass back the raw xml, just in case the caller wants to do anything with it
              result.rawData = groovy.xml.XmlUtil.serialize(xml);

              // Add an audit record
              iso18626LogDetails.response(respomseStatus, result.rawData);

              // Now attempt to interpret the result
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
                  result.errorData = responseNode.errorData;
              }
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
                      ((eventData?.action == null) ? '' : eventData.action) :
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

  Request makeRequest(def eventData){
    Request request = new Request()
    request.setHeader(makeHeader(eventData))

    // Bib info and Service Info only apply to REQUESTS
    log.debug("This is a request message, so needs BibliographicInfo")
    if (eventData.bibliographicInfo != null) {
      request.setBibliographicInfo(makeBibliographicInfo(eventData))
    } else {
      log.warn("No bibliographicInfo found")
    }

    log.debug("This is a request message, so needs PublicationInfo")
    if (eventData.publicationInfo != null) {
      request.setPublicationInfo(makePublicationInfo(eventData))
    } else {
      log.warn("No publicationInfo found")
    }

    log.debug("This is a request message, so needs ServiceInfo")
    if (eventData.serviceInfo != null) {
      request.setServiceInfo(makeServiceInfo(eventData))
    } else {
      log.warn("No serviceInfo found")
    }

    //TODO Wire in supplierInfo here
    RequestedDeliveryInfo requestedDeliveryInfo = makeRequestedDeliveryInfo(eventData)
    if (requestedDeliveryInfo) {
      request.getRequestedDeliveryInfo().add(requestedDeliveryInfo)
    } else {
      log.info("No requestedDeliveryInfo")
    }

    //TODO Wire in requestingAgencyInfo here

    log.debug("This is a requesting message, so needs PatronInfo")
    if (eventData.patronInfo != null) {
      request.setPatronInfo(makePatronInfo(eventData))
    } else {
      log.warn("No patronInfo found")
    }

    log.debug("This is a requesting message, so needs BillingInfo")
    if (eventData.billingInfo != null && eventData.billingInfo.maximumCosts != null) {
      request.setBillingInfo(makeBillingInfo(eventData))
    } else {
      log.warn("No billingInfo found")
    }

    return request
  }

  RequestedDeliveryInfo makeRequestedDeliveryInfo(eventData) {
    RequestedDeliveryInfo requestedDeliveryInfo = new  RequestedDeliveryInfo()
    Address address = new Address()
    if ( ( eventData.requestedDeliveryInfo?.address != null ) &&
            ( eventData.requestedDeliveryInfo?.address?.physicalAddress != null ) ) {
      PhysicalAddress physicalAddress = new PhysicalAddress()
      physicalAddress.setLine1(eventData.requestedDeliveryInfo.address.physicalAddress.line1)
      physicalAddress.setLine2(eventData.requestedDeliveryInfo.address.physicalAddress.line2)
      physicalAddress.setLocality(eventData.requestedDeliveryInfo.address.physicalAddress.locality)
      physicalAddress.setPostalCode(eventData.requestedDeliveryInfo.address.physicalAddress.postalCode)
      physicalAddress.setRegion(toTypeSchemeValuePair(eventData.requestedDeliveryInfo.address.physicalAddress.region))
      physicalAddress.setCountry(toTypeSchemeValuePair(eventData.requestedDeliveryInfo.address.physicalAddress.county))
      address.setPhysicalAddress(physicalAddress)
    }
    if ( ( eventData.requestedDeliveryInfo?.address != null ) &&
            ( eventData.requestedDeliveryInfo?.address?.electronicAddress?.electronicAddressType != null ) ) {
      ElectronicAddress electronicAddress = new ElectronicAddress()
      electronicAddress.setElectronicAddressType(toTypeSchemeValuePair(eventData.requestedDeliveryInfo.address.electronicAddress.electronicAddressType))
      electronicAddress.setElectronicAddressData(eventData.requestedDeliveryInfo.address.electronicAddress.electronicAddressData ? eventData.requestedDeliveryInfo.address.electronicAddress.electronicAddressData : "")
      address.setElectronicAddress(electronicAddress)
    }
    requestedDeliveryInfo.setAddress(address)
    return requestedDeliveryInfo.address.electronicAddress || requestedDeliveryInfo.address.physicalAddress ? requestedDeliveryInfo : null
  }



  SupplyingAgencyMessage makeSupplyingAgencyMessageBody(eventData) {
    SupplyingAgencyMessage supplyingAgencyMessage = new SupplyingAgencyMessage()
    supplyingAgencyMessage.setHeader(makeHeader(eventData))

    log.debug("This is a supplying agency message, so we need MessageInfo, StatusInfo, DeliveryInfo")
    if (eventData.messageInfo != null) {
      supplyingAgencyMessage.setMessageInfo(makeMessageInfo(eventData))
    } else {
      log.warn("No messageInfo found")
    }

    if (eventData.statusInfo != null) {
      supplyingAgencyMessage.setStatusInfo(makeStatusInfo(eventData))
    } else {
      log.warn("No statusInfo found")
    }

    if (eventData.deliveryInfo != null) {
      supplyingAgencyMessage.setDeliveryInfo(makeDeliveryInfo(eventData))
    } else {
      log.warn("No deliveryInfo found")
    }

    if (eventData.returnInfo != null) {
      supplyingAgencyMessage.setReturnInfo(makeReturnInfo(eventData))
    } else {
      log.warn("No returnInfo found")
    }

    return supplyingAgencyMessage
  }

  RequestingAgencyMessage makeRequestingAgencyMessageBody(eventData) {
    RequestingAgencyMessage requestingAgencyMessage = new RequestingAgencyMessage()
    requestingAgencyMessage.setHeader(makeHeader(eventData))
    log.debug("This is a requesting agency message, so we need to add action and note")
    if (eventData.action != null) {
      requestingAgencyMessage.setAction(TypeAction.fromValue(eventData.action))
      requestingAgencyMessage.setNote(eventData.note)
    } else {
      log.warn("No action found")
    }
    return requestingAgencyMessage
  }

  Header makeHeader(eventData) {
    Header header = new Header()
    TypeAgencyId supplyingAgencyId = new TypeAgencyId()
    supplyingAgencyId.setAgencyIdType(toTypeSchemeValuePair(eventData.header.supplyingAgencyId.agencyIdType))
    supplyingAgencyId.setAgencyIdValue(eventData.header.supplyingAgencyId.agencyIdValue)
    header.setSupplyingAgencyId(supplyingAgencyId)

    TypeAgencyId requestingAgencyId = new TypeAgencyId()
    requestingAgencyId.setAgencyIdType(toTypeSchemeValuePair(eventData.header.requestingAgencyId.agencyIdType))
    requestingAgencyId.setAgencyIdValue(eventData.header.requestingAgencyId.agencyIdValue)
    header.setRequestingAgencyId(requestingAgencyId)

    header.setMultipleItemRequestId('')
    header.setTimestamp(currentZonedDateTime())
    header.setRequestingAgencyRequestId(eventData.header.requestingAgencyRequestId ? eventData.header.requestingAgencyRequestId : '')
    if (eventData.messageType == Iso18626Constants.SUPPLYING_AGENCY_MESSAGE || eventData.messageType == Iso18626Constants.REQUESTING_AGENCY_MESSAGE) {
      header.setSupplyingAgencyRequestId(eventData.header.supplyingAgencyRequestId ? eventData.header.supplyingAgencyRequestId : '')
    }
    if (eventData.messageType == Iso18626Constants.REQUESTING_AGENCY_MESSAGE) {
      header.setRequestingAgencyAuthentication(eventData.header.requestingAgencyAuthentication)
    }
    return header
  }


  BibliographicInfo makeBibliographicInfo(eventData) {
    BibliographicInfo bibliographicInfo = new BibliographicInfo()
    bibliographicInfo.setSupplierUniqueRecordId(eventData.bibliographicInfo.supplierUniqueRecordId)
    bibliographicInfo.setTitle(eventData.bibliographicInfo.title)
    bibliographicInfo.setAuthor(eventData.bibliographicInfo.author)
    bibliographicInfo.setSubtitle(eventData.bibliographicInfo.subtitle)
    bibliographicInfo.setEdition(eventData.bibliographicInfo.edition)
    bibliographicInfo.setTitleOfComponent(eventData.bibliographicInfo.titleOfComponent)
    bibliographicInfo.setAuthorOfComponent(eventData.bibliographicInfo.authorOfComponent)
    bibliographicInfo.setVolume(eventData.bibliographicInfo.volume)
    bibliographicInfo.setIssue(eventData.bibliographicInfo.issue)
    bibliographicInfo.setPagesRequested(eventData.bibliographicInfo.pagesRequested)
    bibliographicInfo.setSponsor(eventData.bibliographicInfo.sponsor)
    bibliographicInfo.setInformationSource(eventData.bibliographicInfo.informationSource)
    for (final def map in eventData.bibliographicInfo.bibliographicRecordId) {
      if (map.bibliographicRecordIdentifier) {
        BibliographicRecordId recordId = new BibliographicRecordId()
        String code = map.bibliographicRecordIdentifierCode
        recordId.setBibliographicRecordIdentifierCode(toTypeSchemeValuePair(code,
                RECORD_ID_CODES.contains(code.toLowerCase()) ? null : 'RESHARE'))
        recordId.setBibliographicRecordIdentifier(map.bibliographicRecordIdentifier)
        bibliographicInfo.getBibliographicRecordId().add(recordId)
      }
    }
    for (final def map in eventData.bibliographicInfo.bibliographicItemId) {
      if (map.bibliographicItemIdentifier) {
        BibliographicItemId itemId = new BibliographicItemId()
        itemId.setBibliographicItemIdentifierCode(toTypeSchemeValuePair(map.bibliographicItemIdentifierCode))
        itemId.setBibliographicItemIdentifier(map.bibliographicItemIdentifier)
        bibliographicInfo.getBibliographicItemId().add(itemId)
      }
    }

    return bibliographicInfo
  }

  TypeSchemeValuePair toTypeSchemeValuePair(def text, def scheme = null){
    TypeSchemeValuePair valuePair = new TypeSchemeValuePair()
    valuePair.setValue(text)
    if (scheme) {
      valuePair.setScheme(scheme)
    }
    return valuePair
  }

  PublicationInfo makePublicationInfo(eventData) {
    PublicationInfo publicationInfo = new PublicationInfo()
    publicationInfo.setPublisher(eventData.publicationInfo.publisher)
    publicationInfo.setPublicationType(toTypeSchemeValuePair(eventData.publicationInfo.publicationType))
    publicationInfo.setPublicationDate(eventData.publicationInfo.publicationDate)
    publicationInfo.setPlaceOfPublication(eventData.publicationInfo.placeOfPublication)
    return publicationInfo
  }

  ServiceInfo makeServiceInfo(eventData) {
    ServiceInfo serviceInfo = new ServiceInfo()
    serviceInfo.setCopyrightCompliance(toTypeSchemeValuePair(eventData.serviceInfo.copyrightCompliance))
    serviceInfo.setServiceType(toServiceType(eventData.serviceInfo.serviceType))
    //serviceInfo.setServiceLevel()
    if (eventData.serviceInfo.needBeforeDate) {
      serviceInfo.setNeedBeforeDate(toZonedDateTime(eventData.serviceInfo.needBeforeDate))
    }
    serviceInfo.setServiceLevel(toTypeSchemeValuePair(eventData.serviceInfo.serviceLevel))
    serviceInfo.setAnyEdition(toYesNo(eventData.serviceInfo.anyEdition))
    serviceInfo.setNote(eventData.serviceInfo.note)
    return serviceInfo
  }

  BillingInfo makeBillingInfo(eventData) {
    if (eventData?.billingInfo instanceof Map && !eventData.billingInfo.isEmpty()) {
      BillingInfo billingInfo = new BillingInfo()
      TypeCosts maxCost = new TypeCosts();
      maxCost.monetaryValue = eventData.billingInfo.maximumCosts?.monetaryValue;
      maxCost.currencyCode = toTypeSchemeValuePair(eventData.billingInfo.maximumCosts?.currencyCode);
      billingInfo.setMaximumCosts(maxCost);
      return billingInfo;
    }
    return null;
  }

  ZonedDateTime toZonedDateTime(String dateString) {
    return ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
  }

  ZonedDateTime toZonedDateTime(LocalDate localDate) {
    return localDate.atStartOfDay(ZoneId.of("UTC"));
  }

  ZonedDateTime currentZonedDateTime(){
    return ZonedDateTime.now(ZoneId.of("UTC"))
  }

  TypeYesNo toYesNo(def input){
    if("yes".equalsIgnoreCase(input) || TypeYesNo.Y.value().equalsIgnoreCase(input)){
      return TypeYesNo.Y
    } else if("no".equalsIgnoreCase(input) || TypeYesNo.N.value().equalsIgnoreCase(input)){
      return TypeYesNo.N
    } else {
      log.warn("Invalid TypeYesNo ${input}")
      return null;
    }
  }

  TypeServiceType toServiceType(String input) {
    if (TypeServiceType.LOAN.value().equalsIgnoreCase(input)) {
      return TypeServiceType.LOAN
    } else if (TypeServiceType.COPY.value().equalsIgnoreCase(input)) {
      return TypeServiceType.COPY
    } else if (TypeServiceType.COPY_OR_LOAN.value().equalsIgnoreCase(input)) {
      return TypeServiceType.COPY_OR_LOAN
    } else {
      log.warn("Invalid service type ${input}")
      return null;
    }
  }

  PatronInfo makePatronInfo(eventData) {
    PatronInfo patronInfo = new PatronInfo()
    patronInfo.setPatronId(eventData.patronInfo.patronId)
    patronInfo.setSurname(eventData.patronInfo.surname)
    patronInfo.setGivenName(eventData.patronInfo.givenName)
    patronInfo.setPatronType(toTypeSchemeValuePair(eventData.patronInfo.patronType))
    return patronInfo
  }

  MessageInfo makeMessageInfo(eventData) {
    MessageInfo messageInfo = new MessageInfo()
    messageInfo.setReasonForMessage(toReasonForMessage(eventData.messageInfo.reasonForMessage))
    if (eventData.messageInfo.answerYesNo) {
      messageInfo.setAnswerYesNo(toYesNo(eventData.messageInfo.answerYesNo))
    }
    messageInfo.setNote(eventData.messageInfo.note)
    messageInfo.setReasonUnfilled(toTypeSchemeValuePair(eventData.messageInfo.reasonUnfilled))
    messageInfo.setReasonRetry(toTypeSchemeValuePair(eventData.messageInfo.reasonRetry))
    if (eventData.messageInfo?.offeredCosts?.monetaryValue && eventData?.messageInfo?.offeredCosts?.currencyCode) {
      TypeCosts offeredCosts = new TypeCosts()
      offeredCosts.setCurrencyCode(toTypeSchemeValuePair(eventData.messageInfo.offeredCosts.currencyCode))
      offeredCosts.setMonetaryValue(new BigDecimal(eventData.messageInfo.offeredCosts.monetaryValue))
      messageInfo.setOfferedCosts(offeredCosts)
    }
    if (eventData.messageInfo.retryAfter) {
      messageInfo.setRetryAfter(toZonedDateTime(eventData.messageInfo.retryAfter))
    }
    if (eventData.messageInfo.retryBefore) {
      messageInfo.setRetryBefore(toZonedDateTime(eventData.messageInfo.retryBefore))
    }
    return messageInfo
  }

  TypeReasonForMessage toReasonForMessage(def input){
    return TypeReasonForMessage.fromValue(input)
  }

  StatusInfo makeStatusInfo(eventData) {
    StatusInfo statusInfo = new StatusInfo()
    statusInfo.setStatus(toStatus(eventData.statusInfo.status))
    if (eventData.statusInfo.expectedDeliverydate) {
      statusInfo.setExpectedDeliveryDate(toZonedDateTime(eventData.statusInfo.expectedDeliverydate))
    }
    if (eventData.statusInfo.dueDate) {
      statusInfo.setDueDate(toZonedDateTime(eventData.statusInfo.dueDate))
    }
    if (eventData.statusInfo.lastChange) {
      statusInfo.setLastChange(toZonedDateTime(eventData.statusInfo.lastChange))
    } else {
      statusInfo.setLastChange(currentZonedDateTime())
    }
    return statusInfo
  }

  TypeStatus toStatus(def input) {
    if (input) {
      return TypeStatus.fromValue(input)
    } else {
      return TypeStatus.REQUEST_RECEIVED
    }
  }

  DeliveryInfo makeDeliveryInfo(eventData) {
    DeliveryInfo deliveryInfo = new DeliveryInfo()
    if (eventData.deliveryInfo.dateSent) {
      deliveryInfo.setDateSent(toZonedDateTime(eventData.deliveryInfo.dateSent))
    } else {
      deliveryInfo.setDateSent(currentZonedDateTime())
    }
    if (eventData.deliveryInfo.itemId instanceof Collection) {
      // Build multiple ItemIds
      // TODO How to handle
      deliveryInfo.setItemId(eventData.deliveryInfo.itemId.join(","))
    } else {
      // Build single ItemId
      deliveryInfo.setItemId(eventData.deliveryInfo.itemId)
    }
    if (eventData.deliveryInfo.sentVia) {
      deliveryInfo.setSentVia(toTypeSchemeValuePair(eventData.deliveryInfo.sentVia))
    }
    if (!eventData.deliveryInfo.sentVia && eventData.deliveryInfo.url) {
      TypeSchemeValuePair pair = new TypeSchemeValuePair()
      pair.setValue('URL')
      deliveryInfo.setSentVia(pair)
    }
    deliveryInfo.setSentToPatron(eventData.deliveryInfo.sentToPatron ? true : false)
    deliveryInfo.setLoanCondition(toTypeSchemeValuePair(eventData.deliveryInfo.loanCondition))
    deliveryInfo.setDeliveredFormat(toTypeSchemeValuePair(eventData.deliveryInfo.deliveredFormat))
    if (eventData.deliveryInfo.deliveryCosts) {
      TypeCosts costs = new TypeCosts()
      costs.setCurrencyCode(toTypeSchemeValuePair(eventData.deliveryInfo.deliveryCosts.currencyCode))
      costs.setMonetaryValue(new BigDecimal(eventData.deliveryInfo.deliveryCosts.monetaryValue))
      deliveryInfo.setDeliveryCosts(costs)
    }
    return deliveryInfo
  }


  ReturnInfo makeReturnInfo(eventData) {
    ReturnInfo returnInfo = new ReturnInfo()
    if (eventData.returnInfo.returnAgencyId) {
      def values = eventData.returnInfo.returnAgencyId.split(':')
      TypeAgencyId returnAgencyId = new TypeAgencyId()
      returnAgencyId.setAgencyIdType(toTypeSchemeValuePair(values[0]))
      returnAgencyId.setAgencyIdValue(values[1])
      returnInfo.setReturnAgencyId(returnAgencyId)

    }
    returnInfo.setName(eventData.returnInfo.name)
    if (eventData.returnInfo?.physicalAddress) {
      def physicalAddressMap = eventData.returnInfo.physicalAddress;
      PhysicalAddress physicalAddress = new PhysicalAddress()
      //physicalAddress.setLine1(eventData.returnInfo.address.physicalAddress)
      if (physicalAddressMap.line1) {
        physicalAddress.setLine1(physicalAddressMap.line1)
      }
      if (physicalAddressMap.line2) {
        physicalAddress.setLine2(physicalAddressMap.line2)
      }
      if (physicalAddressMap.locality) {
        physicalAddress.setLocality(physicalAddressMap.locality)
      }
      if (physicalAddressMap.postalCode) {
        physicalAddress.setPostalCode(physicalAddressMap.postalCode)
      }
      if (physicalAddressMap.region instanceof List && physicalAddressMap.region.size() == 2) {
        physicalAddress.setRegion(toTypeSchemeValuePair(physicalAddressMap.region[1]))
      }
      if (physicalAddressMap.country instanceof List && physicalAddressMap.country.size() == 2) {
        physicalAddress.setCountry(toTypeSchemeValuePair(physicalAddressMap.country[1]))
      }
      returnInfo.setPhysicalAddress(physicalAddress)
    }
    return returnInfo
  }
}
