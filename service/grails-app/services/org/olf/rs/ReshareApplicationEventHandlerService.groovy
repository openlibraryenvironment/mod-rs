package org.olf.rs;


import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestNotification
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StateModel
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import java.time.LocalDateTime;
import groovy.sql.Sql
import com.k_int.web.toolkit.settings.AppSetting
import static groovyx.net.http.HttpBuilder.configure
import org.olf.rs.lms.ItemLocation;


/**
 * Handle application events.
 *
 * This class is all about high level reshare events - the kind of things users want to customise and modify. Application functionality
 * that might change between implementations can be added here.
 * REMEMBER: Notifications are not automatically within a tenant scope - handlers will need to resolve that.
 */
public class ReshareApplicationEventHandlerService {

  private static final int MAX_RETRIES = 10;
  
  ProtocolMessageService protocolMessageService
  GlobalConfigService globalConfigService
  SharedIndexService sharedIndexService
  HostLMSService hostLMSService

  // This map maps events to handlers - it is essentially an indirection mecahnism that will eventually allow
  // RE:Share users to add custom event handlers and override the system defaults. For now, we provide static
  // implementations of the different indications.
  private static Map<String,Closure> handlers = [
    'NewPatronRequest_ind':{ service, eventData ->
      service.handleNewPatronRequestIndication(eventData);
    },
    'STATUS_REQ_VALIDATED_ind': { service, eventData ->
      // This isn't really right - if we are a responder, we don't ever want to locate other responders.
      // This should be refactored to handleValidated which checks to see if we are a responder and then
      // acts accordingly. This close to all hands demo, I've changed service.sourcePatronRequest itself
      // to check for isRequester and do nothing in that case.
      service.sourcePatronRequest(eventData);
    },
    'STATUS_REQ_SOURCING_ITEM_ind': { service, eventData ->
      service.log.debug("REQ_SOURCING_ITEM state should now be REQ_SUPPLIER_IDENTIFIED");
    },
    'STATUS_REQ_UNFILLED_ind': { service, eventData ->
      service.sendToNextLender(eventData);
    },
    'STATUS_REQ_SUPPLIER_IDENTIFIED_ind': { service, eventData ->
      service.sendToNextLender(eventData);
    },
    'STATUS_RESPONDER_ERROR_ind': { service, eventData ->
      // There was an error from the responder - log and move to next lending string 
      // or end of rota.
    },
    'STATUS_RESPONDER_NOT_SUPPLIED_ind': { service, eventData ->
      // Responder sent back a negative response - next lending string entry or end of rota.
    },
    'STATUS_REQ_REQUEST_SENT_TO_SUPPLIER_ind': { service, eventData ->
    },
    'STATUS_REQ_ITEM_SHIPPED_ind': { service, eventData ->
    },
    'STATUS_REQ_BORROWING_LIBRARY_RECEIVED_ind': { service, eventData ->
    },
    'STATUS_REQ_AWAITING_RETURN_SHIPPING_ind': { service, eventData ->
    },
    'STATUS_REQ_BORROWER_RETURNED_ind': { service, eventData ->
    },
    'MESSAGE_REQUEST_ind': { service, eventData ->
      // This is called on an incoming REQUEST - can be loopback, ISO18626, ISO10161, etc.
      service.handleRequestMessage(eventData);
    },
    'SUPPLYING_AGENCY_MESSAGE_ind': { service, eventData ->
      service.handleSupplyingAgencyMessage(eventData);
    }

  ]

  @Subscriber('PREventIndication')
  public handleApplicationEvent(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleApplicationEvent(${eventData})");
    Closure c = handlers[eventData.event]
    if ( c != null ) {
      // System has a closure registered for event, call it
      log.debug("Found closure for event ${eventData.event}. Calling");
      if ( eventData.tenant ) {
        try {
          Tenants.withId(eventData.tenant) {
            c.call(this, eventData);
          }
        }
        catch ( Exception e ) {
          log.error("Problem trying to invoke event handler for ${eventData.event}",e)
          throw e;
        }
      }
    }
    else {
      log.warn("Event ${eventData.event} no handler found");
    }
  }

  // Notify us of a new patron request in the database - regardless of role
  //
  // Requests are created with a STATE of IDLE, this handler validates the request and sets the state to VALIDATED, or ERROR
  // Called when a new patron request indication happens - usually
  // New patron requests must have a  req.requestingInstitutionSymbol
  public void handleNewPatronRequestIndication(eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleNewPatronRequestIndication(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def req = delayedGet(eventData.payload.id, true);

      // If the role is requester then validate the request and set the state to validated
      if ( ( req != null ) && 
           ( req.state?.code == 'REQ_IDLE' ) && 
           ( req.isRequester == true) ) {

        // If valid - generate a human readabe ID to use
        req.hrid=generateHrid()
        log.debug("Updated req.hrid to ${req.hrid}");

        def patron_details = hostLMSService.getHostLMSActions().lookupPatron(req.patronIdentifier)

        if ( isValidPatron(patron_details) ) {

          if ( ( patron_details != null ) && ( patron_details.userid != null ) ) {
            req.resolvedPatron = lookupOrCreatePatronProxy(patron_details);
            if ( req.patronSurname == null )
              req.patronSurname = patron_details.surname;
            if ( req.patronGivenName == null )
              req.patronGivenName = patron_details.givenName;
          }

          if ( req.requestingInstitutionSymbol != null ) {
            // We need to validate the requsting location - and check that we can act as requester for that symbol
            Symbol s = resolveCombinedSymbol(req.requestingInstitutionSymbol);
        
            if ( s != null ) {
              req.resolvedRequester = s
              log.debug("Got request ${req}");
              log.debug(" -> Request is currently REQ_IDLE - transition to REQ_VALIDATED");
              req.state = lookupStatus('PatronRequest', 'REQ_VALIDATED');
              auditEntry(req, lookupStatus('PatronRequest', 'REQ_IDLE'), lookupStatus('PatronRequest', 'REQ_VALIDATED'), 'Request Validated', null);
            }
            else {
              log.warn("Unkown requesting institution symbol : ${req.requestingInstitutionSymbol}");
              req.state = lookupStatus('PatronRequest', 'REQ_ERROR');
              auditEntry(req, 
                         lookupStatus('PatronRequest', 'REQ_IDLE'), 
                         lookupStatus('PatronRequest', 'REQ_ERROR'), 
                         'Unknown Requesting Institution Symbol: '+req.requestingInstitutionSymbol, null);
            }
          }
          else {
            req.state = lookupStatus('PatronRequest', 'REQ_ERROR');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_IDLE'), lookupStatus('PatronRequest', 'REQ_ERROR'), 'No Requesting Institution Symbol', null);
          }
        }
        else {
          req.state = lookupStatus('PatronRequest', 'REQ_INVALID_PATRON');
          auditEntry(req, lookupStatus('PatronRequest', 'REQ_IDLE'), lookupStatus('PatronRequest', 'REQ_INVALID_PATRON'), "Invalid Patron Id: \"${req.patronIdentifier}\"".toString(), null);
        }

        if ( ( req.systemInstanceIdentifier != null ) && ( req.systemInstanceIdentifier.length() > 0 ) ) {
          log.debug("calling fetchSharedIndexRecord");
          req.bibRecord = sharedIndexService.fetchSharedIndexRecord(req.systemInstanceIdentifier)
        }
        else {
          log.debug("No req.systemInstanceIdentifier : ${req.systemInstanceIdentifier}");
        }

        req.save(flush:true, failOnError:true)
      }
      else if ( ( req != null ) && ( req.state?.code == 'RES_IDLE' ) && ( req.isRequester == false ) ) {
        try {
          log.debug("Launch auto responder for request");
          String auto_respond = AppSetting.findByKey('auto_responder_status')?.value
          if ( auto_respond?.startsWith('On') ) {
            autoRespond(req)
          }
          else {
            auditEntry(req, req.state, req.state, "Auto responder is ${auto_respond} - manual checking needed", null);
          }
          req.save(flush:true, failOnError:true);
        }
        catch ( Exception e ) {
          log.error("Problem in auto respond",e);
        }
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != REQ_IDLE (${req?.state?.code})");
      }
    }
  }

  public boolean isValidPatron(Map patron_record) {
    boolean result = true;
    log.debug("Check isValidPatron: ${patron_record}");
    if ( patron_record != null ) {
      if ( patron_record.status != 'OK' ) {
        result = false;
      }
    }
    return true;
  }


  // This takes a request with the state of VALIDATED and changes the state to REQ_SOURCING_ITEM, 
  // and then on to REQ_SUPPLIER_IDENTIFIED if a rota could be established
  public void sourcePatronRequest(eventData) {
    log.debug("ReshareApplicationEventHandlerService::sourcePatronRequest(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      PatronRequest req = delayedGet(eventData.payload.id, true);
      if ( ( req.isRequester == true ) && ( req != null ) && ( req.state?.code == 'REQ_VALIDATED' ) ) {

        log.debug("Got request ${req}");
        log.debug(" -> Request is currently VALIDATED - transition to REQ_SOURCING_ITEM");
        req.state = lookupStatus('PatronRequest', 'REQ_SOURCING_ITEM');
        req.save(flush:true, failOnError:true)


        if(req.rota?.size() != 0) {
          log.debug("Found a potential supplier for ${req}");
          log.debug(" -> Request is currently REQ_SOURCING_ITEM - transition to REQ_SUPPLIER_IDENTIFIED");
          req.state = lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED');
          auditEntry(req, lookupStatus('PatronRequest', 'REQ_VALIDATED'), lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), 'Request supplied with Lending String', null);
          req.save(flush:true, failOnError:true)
        } else {
          log.debug("No rota supplied - call sharedIndexService.findAppropriateCopies to find appropriate copies");
          // NO rota supplied - see if we can use the shared index service to locate appropriate copies
          // N.B. grails-app/conf/spring/resources.groovy causes a different implementation to be injected
          // here in the test environments.
          List<AvailabilityStatement> sia = sharedIndexService.findAppropriateCopies(req.getDescriptiveMetadata());
          log.debug("Result of shared index lookup : ${sia}");
          int ctr = 0;

          if (  sia.size() > 0 ) {
            sia?.each { av_stmt ->
              if ( av_stmt.symbol != null ) {
                req.addToRota (new PatronRequestRota(
                                                     patronRequest:req,
                                                     rotaPosition:ctr++, 
                                                     directoryId:av_stmt.symbol,
                                                     instanceIdentifier:av_stmt.instanceIdentifier,
                                                     copyIdentifier:av_stmt.copyIdentifier,
                                                     state: lookupStatus('PatronRequest', 'REQ_IDLE')))
              }
            }
            req.state = lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_VALIDATED'), lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), 
                       'Lending String calculated from shared index', null);
            req.save(flush:true, failOnError:true)
          }
          else {
            // ToDo: Ethan: if LastResort app setting is set, add lenders to the request.

            log.error("Unable to identify any suppliers for patron request ID ${eventData.payload.id}")
            req.state = lookupStatus('PatronRequest', 'REQ_END_OF_ROTA');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_VALIDATED'), lookupStatus('PatronRequest', 'REQ_END_OF_ROTA'), 
                       'Unable to locate lenders', null);
            req.save(flush:true, failOnError:true)
          }
        }
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != REQ_VALIDATED (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
  }

  // This takes a request with the state of REQ_SUPPLIER_IDENTIFIED and changes the state to REQUEST_SENT_TO_SUPPLIER
  public void sendToNextLender(eventData) {
    log.debug("ReshareApplicationEventHandlerService::sendToNextLender(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id, true);
      // We must have found the request, and it as to be in a state of supplier identifier or unfilled
      if ( ( req != null ) && 
           ( ( req.state?.code == 'REQ_SUPPLIER_IDENTIFIED' ) ||
             ( req.state?.code == 'REQ_UNFILLED' ) ) ) {
        log.debug("Got request ${req} (HRID Is ${req.hrid})");
        
        //TODO - sendRequest called here, make it do stuff - A request to send a protocol level resource sharing request message
        Map request_message_request = [
          messageType:'REQUEST',
          header:[
              // Filled out later
              // supplyingAgencyId:[
              //   agencyIdType:,
              //   agencyIdValue:,
              // ],
              requestingAgencyId:[
                agencyIdType:req.resolvedRequester?.authority?.symbol,
                agencyIdValue:req.resolvedRequester?.symbol
              ],
              // requestingAgencyRequestId:req.id,
              requestingAgencyRequestId:req.hrid ?: req.id,
              supplyingAgencyRequestId:null
          ],
          bibliographicInfo:[
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
        ]

        if ( req.rota.size() > 0 ) {
          boolean request_sent = false;

          // There may be problems with entries in the lending string, so we loop through the rota
          // until we reach the end, or we find a potential lender we can talk to. The request must
          // also explicitly state a requestingInstitutionSymbol
          while ( ( !request_sent ) && 
                  ( req.rota.size() > 0 ) &&
                  ( ( req.rotaPosition?:-1 ) < req.rota.size() ) && 
                  ( req.requestingInstitutionSymbol != null ) ) {

            // We have rota entries left, work out the next one
            req.rotaPosition = (req.rotaPosition!=null ? req.rotaPosition+1 : 0 )

            // get the responder
            PatronRequestRota prr = req.rota.find( { it.rotaPosition == req.rotaPosition } )
            if ( prr != null ) {
              String next_responder = prr.directoryId

              Symbol s = ( next_responder != null ) ? resolveCombinedSymbol(next_responder) : null;

              // Fill out the directory entry reference if it's not currently set, and try to send.
              if ( ( next_responder != null ) && 
                   ( s != null ) &&
                   ( prr.peerSymbol == null ) ) {

                log.debug("Attempt to resolve symbol \"${next_responder}\"");
                log.debug("Resolved to symbol ${s}");

                if ( s != null ) {
                  prr.lock();
                  prr.peerSymbol = s
                  prr.save(flush:true, failOnError:true)

                  request_message_request.header.supplyingAgencyId = [
                    agencyIdType:s.authority?.symbol,
                    agencyIdValue:s.symbol,
                  ]

                }
                else {
                  log.warn("Cannot understand or resolve symbol ${next_responder}");
                }

                // update request_message_request.systemInstanceIdentifier to the system number specified in the rota
                request_message_request.bibliographicInfo.systemInstanceIdentifier = prr.instanceIdentifier;
                request_message_request.bibliographicInfo.supplyingInstitutionSymbol = next_responder;


                // Probably need a lender_is_valid check here
                def send_result = protocolMessageService.sendProtocolMessage(req.requestingInstitutionSymbol, next_responder, request_message_request)
                if ( send_result.status=='SENT' ) {
                  prr.state = lookupStatus('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER');
                  request_sent = true;
                }
                else {
                  prr.state = lookupStatus('PatronRequest', 'REQ_UNABLE_TO_CONTACT_SUPPLIER');
                  prr.note = "Result of send : ${send_result.status}"
                }
              }
              else {
                log.warn("Lender at position ${req.rotaPosition} invalid, skipping");
                prr.state = lookupStatus('PatronRequest', 'REQ_UNABLE_TO_CONTACT_SUPPLIER');
                prr.note = "Send not attempted: Unable to resolve symbol for : ${next_responder}";
              }

              prr.save(flush:true, failOnError:true);
            }
            else {
              log.error("Unable to find rota entry at position ${req.rotaPosition} (Size=${req.rota.size()}) ${( req.rotaPosition?:-1 < req.rota.size() )}. Try next");
            }
          }

          // ToDo - there are three possible states here,not two - Send, End of Rota, Error
          // Did we send a request?
          if ( request_sent ) {
            log.debug("sendToNextLender sent to next lender.....");
            req.state = lookupStatus('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), lookupStatus('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER'), 
                       'Sent to next lender', null);
            req.save(flush:true, failOnError:true)
          }
          else {
            // END OF ROTA
            log.warn("sendToNextLender reached the end of the lending string.....");
            req.state = lookupStatus('PatronRequest', 'REQ_END_OF_ROTA');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), lookupStatus('PatronRequest', 'REQ_END_OF_ROTA'), 'End of rota', null);
            req.save(flush:true, failOnError:true)
          }
        }
        else {
          log.warn("Annot send to next lender - rota is empty");
          req.state = lookupStatus('PatronRequest', 'REQ_END_OF_ROTA');
          req.save(flush:true, failOnError:true)
        }
        
        log.debug(" -> Request is currently REQ_SUPPLIER_IDENTIFIED - transition to REQUEST_SENT_TO_SUPPLIER");
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != REQ_SUPPLIER_IDENTIFIED (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
  }


  /**
   * A new request has been received from a peer institution. We will need to create a request where isRequester==false
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleRequestMessage(Map eventData) {

    def result = [:]

    log.debug("ReshareApplicationEventHandlerService::handleRequestMessage(${eventData})");

    // Check that we understand both the requestingAgencyId (our peer)and the SupplyingAgencyId (us)
    if ( ( eventData.bibliographicInfo != null ) &&
         ( eventData.header != null ) ) {

      Map header = eventData.header;

      Symbol resolvedSupplyingAgency = resolveSymbol(header.supplyingAgencyId?.agencyIdType, header.supplyingAgencyId?.agencyIdValue)
      Symbol resolvedRequestingAgency = resolveSymbol(header.requestingAgencyId?.agencyIdType, header.requestingAgencyId?.agencyIdValue)

      log.debug("*** Create new request***");
      PatronRequest pr = new PatronRequest(eventData.bibliographicInfo)
      pr.supplyingInstitutionSymbol = "${header.supplyingAgencyId?.agencyIdType}:${header.supplyingAgencyId?.agencyIdValue}"
      pr.requestingInstitutionSymbol = "${header.requestingAgencyId?.agencyIdType}:${header.requestingAgencyId?.agencyIdValue}"

      //  ToDo - is this right?
      pr.resolvedRequester = resolvedRequestingAgency;
      pr.resolvedSupplier = resolvedSupplyingAgency;
      pr.peerRequestIdentifier = header.requestingAgencyRequestId

      log.debug("new request from ${pr.requestingInstitutionSymbol} to ${pr.supplyingInstitutionSymbol}");

      pr.state = lookupStatus('Responder', 'RES_IDLE')
      pr.isRequester=false;
      auditEntry(pr, null, pr.state, 'New request (Lender role) created as a result of protocol interaction', null);

      log.debug("Saving new PatronRequest(SupplyingAgency) - Req:${pr.resolvedRequester} Res:${pr.resolvedSupplier} PeerId:${pr.peerRequestIdentifier}");
      pr.save(flush:true, failOnError:true)

      result.messageType = "REQUEST"
      result.supIdType = header.supplyingAgencyId.agencyIdType
      result.supId = header.supplyingAgencyId.agencyIdValue
      result.reqAgencyIdType = header.requestingAgencyId.agencyIdType
      result.reqAgencyId = header.requestingAgencyId.agencyIdValue
      result.reqId = header.requestingAgencyRequestId
      result.timeRec = header.timestamp

      result.status = 'OK'
      result.newRequestId = pr.id;
    }
    else {
      log.error("A REQUEST indication must contain a request key with properties defining the sought item - eg request.title - GOT ${eventData}");
    }

    return result;
  }

  /** We aren't yet sure how human readable IDs will pan out in the system and there is a desire to use
   * HRIDs as the requesting agency ID instead of a UUID. For now, isolating all the request lookup functionality
   * in this method - which will try both approaches to give us some flexibility in adapting to different schemes.
   * @Param  id - a UUID OR a HRID String
   */
  PatronRequest lookupPatronRequest(String id) {
    return PatronRequest.findByIdOrHrid(id,id);
  }

  PatronRequest lookupPatronRequestByPeerId(String id) {
    return PatronRequest.findByPeerRequestIdentifier(id);
  }

  /**
   * An incoming message to the requesting agency FROM the supplying agency - so we look in 
   * eventData.header?.requestingAgencyRequestId to find our own ID for the request.
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleSupplyingAgencyMessage(Map eventData) {

    def result = [:]

    log.debug("ReshareApplicationEventHandlerService::handleSupplyingAgencyMessage(${eventData})");

    try {
      if ( eventData.header?.requestingAgencyRequestId == null ) {
        result.status = "ERROR"
        result.errorType = "BadlyFormedMessage"
        throw new Exception("requestingAgencyRequestId missing");
      }
        

      PatronRequest pr = lookupPatronRequest(eventData.header.requestingAgencyRequestId)
      if ( pr == null )
        throw new Exception("Unable to locate PatronRequest corresponding to ID or hrid in requestingAgencyRequestId \"${eventData.header.requestingAgencyRequestId}\"");

      // Awesome - managed to look up patron request - see if we can action
      if ( eventData.messageInfo?.reasonForMessage != null ) {
        switch ( eventData.messageInfo?.reasonForMessage ) {
          case 'RequestResponse':
            break;
          case 'StatusRequestResponse':
            break;
          case 'RenewResponse':
            break;
          case 'CancelResponse':
            break;
          case 'StatusChange':
            break;
          case 'Notification':
            Map messageData = eventData.messageInfo
            auditEntry(pr, pr.state, pr.state, "Notification message received from supplying agency: ${messageData.note}", null)
            notificationEntry(pr, eventData, true)
            break;
          default:
            result.status = "ERROR"
            result.errorType = "UnsupportedReasonForMessageType"
            result.errorValue = eventData.messageInfo.reasonForMessage
            throw new Exception("Unhandled reasonForMessage: ${eventData.messageInfo.reasonForMessage}");
            break;
        }
      }
      else {
        result.status = "ERROR"
        result.errorType = "BadlyFormedMessage"
        throw new Exception("No reason for message");
      }

      if ( eventData.statusInfo != null ) {
        handleStatusChange(pr, eventData.statusInfo, eventData.header.supplyingAgencyRequestId);
      }

      pr.save(flush:true, failOnError:true);
    }
    catch ( Exception e ) {
      log.error("Problem processing SupplyingAgencyMessage: ${e.message}", e);
    }

    if (result.status != "ERROR") {
      result.status = "OK"
    }

    result.messageType = "SUPPLYING_AGENCY_MESSAGE"
    result.supIdType = eventData.header.supplyingAgencyId.agencyIdType
    result.supId = eventData.header.supplyingAgencyId.agencyIdValue
    result.reqAgencyIdType = eventData.header.requestingAgencyId.agencyIdType
    result.reqAgencyId = eventData.header.requestingAgencyId.agencyIdValue
    result.reqId = eventData.header.requestingAgencyRequestId
    result.timeRec = eventData.header.timestamp
    result.reasonForMessage = eventData.messageInfo.reasonForMessage

    return result;
  }


/**
   * An incoming message to the supplying agency from the requesting agency - so we look in 
   * eventData.header?.supplyingAgencyRequestId to find our own ID for the request.
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleRequestingAgencyMessage(Map eventData) {

    def result = [:]

    log.debug("ReshareApplicationEventHandlerService::handleRequestingAgencyMessage(${eventData})")

    try {
      if ( eventData.header?.supplyingAgencyRequestId == null ) {
        result.status = "ERROR"
        result.errorType = "BadlyFormedMessage"
        throw new Exception("supplyingAgencyRequestId missing");
      }

      PatronRequest pr = lookupPatronRequest(eventData.header.supplyingAgencyRequestId)
      if ( pr == null ) {
        log.warn("Unable to locate PatronRequest corresponding to ID or Hrid in supplyingAgencyRequestId \"${eventData.header.supplyingAgencyRequestId}\", trying to locate by peerId.")
        pr = lookupPatronRequestByPeerId(eventData.header.requestingAgencyRequestId)
      }
      if (pr == null) {
        throw new Exception("Unable to locate PatronRequest corresponding to peerRequestIdentifier in requestingAgencyRequestId \"${eventData.header.requestingAgencyRequestId}\"");
      } else {
        log.debug("Lookup by peerID successful.")
      }

      // TODO Handle incoming reasons other than notification for RequestingAgencyMessage
      // Needs to look for action and try to do something with that.

      if ( eventData.activeSection?.action != null ) {
        switch ( eventData.activeSection?.action ) {
          case 'Received':
            auditEntry(pr, pr.state, pr.state, "Shipment received by requester", null)
            pr.save(flush: true, failOnError: true)
            break;
          case 'Notification':
            Map messageData = eventData.activeSection
            auditEntry(pr, pr.state, pr.state, "Notification message received from requesting agency: ${messageData.note}", null)
            notificationEntry(pr, eventData, false)
            break;
          default:
            result.status = "ERROR"
            result.errorType = "UnsupportedActionType"
            result.errorValue = eventData.activeSection.action
            throw new Exception("Unhandled action: ${eventData.activeSection.action}");
            break;
        }
      }
      else {
        result.status = "ERROR"
        result.errorType = "BadlyFormedMessage"
        throw new Exception("No action in active section");
      }
    } catch ( Exception e ) {
      log.error("Problem processing RequestingAgencyMessage: ${e.message}", e);
    }

    if (result.status != "ERROR") {
      result.status = "OK"
    }

    result.messageType = "REQUESTING_AGENCY_MESSAGE"
    result.supIdType = eventData.header.supplyingAgencyId.agencyIdType
    result.supId = eventData.header.supplyingAgencyId.agencyIdValue
    result.reqAgencyIdType = eventData.header.requestingAgencyId.agencyIdType
    result.reqAgencyId = eventData.header.requestingAgencyId.agencyIdValue
    result.reqId = eventData.header.requestingAgencyRequestId
    result.timeRec = eventData.header.timestamp
    result.action = eventData.activeSection?.action

    return result;
  }


  // ISO18626 states are RequestReceived ExpectToSupply WillSupply Loaned Overdue Recalled RetryPossible Unfilled CopyCompleted LoanCompleted CompletedWithoutReturn Cancelled

  private void handleStatusChange(PatronRequest pr, Map statusInfo, String supplyingAgencyRequestId) {
    log.debug("handleStatusChange(${pr.id},${statusInfo})");

    // Get the rota entry for the current peer
    PatronRequestRota prr = pr.rota.find( { it.rotaPosition == pr.rotaPosition } )

    if ( statusInfo.status ) {
      switch ( statusInfo.status ) {
        case 'ExpectToSupply':
          def new_state = lookupStatus('PatronRequest', 'REQ_EXPECTS_TO_SUPPLY')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state
          break;
        case 'Unfilled':
          def new_state = lookupStatus('PatronRequest', 'REQ_UNFILLED')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state;
          break;
        case 'Loaned':
          def new_state = lookupStatus('PatronRequest', 'REQ_SHIPPED')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state
          break;
        case 'Overdue':
          def new_state = lookupStatus('PatronRequest', 'REQ_OVERDUE')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state;
          break;
        case 'Recalled':
          def new_state = lookupStatus('PatronRequest', 'REQ_RECALLED')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state;
          break;
        case 'Cancelled':
          def new_state = lookupStatus('PatronRequest', 'REQ_CANCELLED', null)
          auditEntry(pr, pr.state, new_state, 'Protocol message');
          pr.state=new_state
          if ( prr != null ) prr.state = new_state
          break;
        default:
          log.error("Unhandled statusInfo.status ${statusInfo.status}");
          break;
      }
    }
  }

  /**
   * Sometimes, we might receive a notification before the source transaction has committed. THats rubbish - so here we retry
   * up to 5 times.
   */
  public PatronRequest delayedGet(String pr_id, boolean wth_lock=false) {
    log.debug("PatronRequest called")
    PatronRequest result = null;
    int retries = 0;

    try {
      while ( ( result == null ) && (retries < MAX_RETRIES) ) {
        if ( wth_lock ) {
          result = PatronRequest.lock(pr_id)
        }
        else {
          result = PatronRequest.get(pr_id)
        }
        if ( result == null ) {
          log.debug("Waiting to see if request has become available: Try ${retries}")
          //Thread.sleep(2000);
          Thread.sleep(900);
          retries++;
        } else {
          log.debug("Result found for ${pr_id}. Refresh")
        }
      }
    }
    catch(Exception e){
      log.error("Problem", e)
    }
    return result;
  }

  private void error(PatronRequest pr, String message) {
    Status old_state = pr.state;
    Status new_state = pr.isRequester ? lookupStatus('PatronRequest', 'REQ_ERROR') : lookupStatus('Responder', 'RES_ERROR');
    pr.state = new_state;
    auditEntry(pr, old_state, new_state, message, null);
  }

  public void auditEntry(PatronRequest pr, Status from, Status to, String message, Map data) {

    String json_data = ( data != null ) ? JsonOutput.toJson(data).toString() : null;
    LocalDateTime ts = LocalDateTime.now();
    log.debug("add audit entry at ${ts}");

    try {
      pr.addToAudit( new PatronRequestAudit(
        patronRequest: pr,
        dateCreated:ts,
        fromStatus:from,
        toStatus:to,
        duration:null,
        message: message,
        auditData: json_data
      ))
    } catch(Exception e) {
      log.error("Problem saving audit entry", e)
    }
    log.debug("Evrything worked ok in auditEntry")
  }

  private void autoRespond(PatronRequest pr) {
    log.debug("autoRespond....");

    // Use the hostLMSService to determine the best location to send a pull-slip to
    ItemLocation location = hostLMSService.getHostLMSActions().determineBestLocation(pr)

    log.debug("result of determineBestLocation = ${location}");

    if ( location != null ) {

      // set localCallNumber to whatever we managed to look up
      if ( routeRequestToLocation(pr, location) ) {
        auditEntry(pr, lookupStatus('Responder', 'RES_IDLE'), lookupStatus('Responder', 'RES_NEW_AWAIT_PULL_SLIP'), 'autoRespond will-supply, determine location='+location, null);
        log.debug("Send ExpectToSupply response to ${pr.requestingInstitutionSymbol}");
        sendResponse(pr, 'ExpectToSupply')
      }
      else {
        auditEntry(pr, lookupStatus('Responder', 'RES_IDLE'), lookupStatus('Responder', 'RES_UNFILLED'), 'AutoResponder Failed to route to location '+location, null);
        log.debug("Send unfilled(No Copy) response to ${pr.requestingInstitutionSymbol}");
        sendResponse(pr, 'Unfilled', 'No copy');
        pr.state=lookupStatus('Responder', 'RES_UNFILLED')
      }
    }
    else {
      log.debug("Send unfilled(No copy) response to ${pr.requestingInstitutionSymbol}");
      sendResponse(pr, 'Unfilled', 'No copy');
      auditEntry(pr, lookupStatus('Responder', 'RES_IDLE'), lookupStatus('Responder', 'RES_UNFILLED'), 'AutoResponder cannot locate a copy.', null);
      pr.state=lookupStatus('Responder', 'RES_UNFILLED')
    }
  }

  public boolean routeRequestToLocation(PatronRequest pr, ItemLocation location) {
    log.debug("routeRequestToLocation(${pr},${location})");
    boolean result = false;

    // Only proceed if there is location
    if ( location && location.location ) {
      // We've been given a specific location, make sure we have a record for that location
      HostLMSLocation loc = HostLMSLocation.findByCodeOrName(location.location,location.location) ?: new HostLMSLocation(
                                                                        code:location.location,
                                                                        name:location.location,
                                                                        icalRrule:'RRULE:FREQ=MINUTELY;INTERVAL=10;WKST=MO').save(flush:true, failOnError:true);
      pr.state=lookupStatus('Responder', 'RES_NEW_AWAIT_PULL_SLIP')
      pr.localCallNumber = location.callNumber
      pr.pickLocation = loc
      pr.pickShelvingLocation = location.shelvingLocation
      pr.save(flush:true, failOnError:true);

      result = true;
    }
    else {
      log.debug("unable to reoute request as local responding location absent");
    }

    return result;
  }


  /**
   *  Send an ILL Request to a possible lender
   */
  private Map sendRequest(PatronRequest req, Symbol requester, Symbol responder) {
    Map send_result = null;

        //TODO - sendRequest called here, make it do stuff - A request to send a protocol level resource sharing request message
        Map request_message_request = [
          messageType:'REQUEST',
          header:[
              supplyingAgencyId:[
                agencyIdType:responder?.authority?.symbol,
                agencyIdValue:responder?.symbol,
              ],
              requestingAgencyId:[
                agencyIdType:requester?.authority?.symbol,
                agencyIdValue:requester?.symbol,
              ],
              requestingAgencyRequestId:req.id,
              supplyingAgencyRequestId:null
          ],
          bibliographicInfo:[
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
        ]

    send_result = null; // protocolMessageService.sendProtocolMessage(req.requestingInstitutionSymbol, next_responder, request_message_request)

    return send_result;
  }


  public void sendResponse(PatronRequest pr, 
                            String status, 
                            String reasonUnfilled = null) {
    sendSupplyingAgencyMessage(pr, 'RequestResponse', status, reasonUnfilled);
  }

  public void sendStatusChange(PatronRequest pr,
                            String status) {
    sendSupplyingAgencyMessage(pr, 'StatusChange', status, null);
  }

  // see http://biblstandard.dk/ill/dk/examples/request-without-additional-information.xml
  // http://biblstandard.dk/ill/dk/examples/supplying-agency-message-delivery-next-day.xml
  // RequestReceived, ExpectToSupply, WillSupply, Loaned, Overdue, Recalled, RetryPossible,
  // Unfilled, CopyCompleted, LoanCompleted, CompletedWithoutReturn, Cancelled
  public void sendSupplyingAgencyMessage(PatronRequest pr, 
                                         String reason_for_message,
                                         String status, 
                                         String reasonUnfilled = null) {

    log.debug("sendResponse(....)");

    // pr.supplyingInstitutionSymbol
    // pr.peerRequestIdentifier
    if ( ( pr.resolvedSupplier != null ) && 
         ( pr.resolvedRequester != null ) ) {

      Map unfilled_message_request = [
          messageType:'SUPPLYING_AGENCY_MESSAGE',
          header:[
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
          ],
          messageInfo:[
            reasonForMessage:reason_for_message
          ],
          statusInfo:[
            status:status
          ]
      ]

      if ( reasonUnfilled ) {
        unfilled_message_request.messageInfo.reasonUnfilled = [ value: reasonUnfilled ]
      }

      log.debug("calling protocolMessageService.sendProtocolMessage(${pr.supplyingInstitutionSymbol},${pr.requestingInstitutionSymbol},${unfilled_message_request})");
      def send_result = protocolMessageService.sendProtocolMessage(pr.supplyingInstitutionSymbol,
                                                                   pr.requestingInstitutionSymbol, 
                                                                   unfilled_message_request);
    }
    else {
      log.error("Unable to send protocol message - supplier(${pr.resolvedSupplier}) or requester(${pr.resolvedRequester}) is missing in PatronRequest ${pr.id}");
    }
  }

  /**
   * ToDo: Fill out.
   * Needs to send a supplyingAgencyMessage where requestingAgencyMessage.action (typedef type_action) = 'Received'
   */
  public void sendRequesterReceived(PatronRequest pr) {
    sendRequestingAgencyMessage(pr, 'Received', null);
  }

  /**
   * ToDo: Fill out.
   */
  public void sendRequestingAgencyMessage(PatronRequest pr, String action, String note) {
    Map eventData = [header:[]];

    eventData.messageType = 'REQUESTING_AGENCY_MESSAGE';
    String message_sender_symbol = pr.requestingInstitutionSymbol;
    Long rotaPosition = pr.rotaPosition;
      
    // We check that it is sensible to send a message, ie that we have a non-empty rota and are pointing at an entry in that.
    if (pr.rota.isEmpty()) {
      log.error("sendRequestingAgencyMessage has been given an empty rota")
      return;
    }
    if (rotaPosition == null) {
      log.error("sendRequestingAgencyMessage could not find current rota postition")
      return;
    } else if (pr.rota.empty()) {
      log.error("sendRequestingAgencyMessage has been handed an empty rota")
      return;
    }

    log.debug("ROTA TYPE: ${pr.rota.getClass()}")
    PatronRequestRota prr = pr.rota.find({it.rotaPosition == rotaPosition})
    log.debug("ROTA at position ${pr.rotaPosition}: ${prr}")
    String peer_symbol = "${prr.peerSymbol.authority.symbol}:${prr.peerSymbol.symbol}"

    eventData.header = [
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
    eventData.activeSection = [
      action: action,
      note: note
    ]

    def send_result = protocolMessageService.sendProtocolMessage(message_sender_symbol, peer_symbol, eventData);

  }

  public Symbol resolveSymbol(String authorty, String symbol) {
    Symbol result = null;
    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                   [authority:authorty?.toUpperCase(), symbol:symbol?.toUpperCase()]);
    if ( symbol_list.size() == 1 ) {
      result = symbol_list.get(0);
    }

    return result;
  }

  public Symbol resolveCombinedSymbol(String combinedString) {
    Symbol result = null;
    if ( combinedString != null ) {
      String[] name_components = combinedString.split(':');
      if ( name_components.length == 2 ) {
        result = resolveSymbol(name_components[0], name_components[1]);
      }
    }
    return result;
  }

  public Status lookupStatus(String model, String code) {
    Status result = null;
    List<Status> qr = Status.executeQuery('select s from Status as s where s.owner.shortcode=:model and s.code=:code',[model:model, code:code]);
    if ( qr.size() == 1 ) {
      result = qr.get(0);
    }
    return result;
  }


  private String generateHrid() {
    String result = null;

    AppSetting prefix_setting = AppSetting.findByKey('request_id_prefix')
    log.debug("Got app setting ${prefix_setting} ${prefix_setting?.value} ${prefix_setting?.defValue}");

    String hrid_prefix = prefix_setting.value ?: prefix_setting.defValue ?: ''

    // Use this to make sessionFactory.currentSession work as expected
    PatronRequest.withSession { session ->
      log.debug("Generate hrid");
      def sql = new Sql(session.connection())
      def query_result = sql.rows("select nextval('pr_hrid_seq')".toString());
      log.debug("Query result: ${query_result.toString()}");
      result = hrid_prefix + query_result[0].get('nextval')?.toString();
    }
    return result;
  }

  private Patron lookupOrCreatePatronProxy(Map patron_details) {
    Patron result = null;
    if ( ( patron_details != null ) && 
         ( patron_details.userid != null ) &&
         ( patron_details.userid.trim().length() > 0 ) ) {
      result = Patron.findByHostSystemIdentifier(patron_details.userid) ?: new Patron(
                                                           hostSystemIdentifier:patron_details.userid, 
                                                           givenname: patron_details.givenName, 
                                                           surname: patron_details.surname).save()
    }
    return result;
  }

  private void notificationEntry(PatronRequest pr, Map eventData, Boolean isRequester) {
    def inboundMessage = new PatronRequestNotification()

    inboundMessage.setPatronRequest(pr)
    inboundMessage.setSeen(false)
    inboundMessage.setTimestamp(LocalDateTime.now())
    if (isRequester) {
      inboundMessage.setMessageSender(resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
    } else {
      inboundMessage.setMessageSender(resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
    }
    inboundMessage.setIsSender(false)
    if (isRequester) {
      inboundMessage.setMessageContent(eventData.messageInfo.note)
    } else {
      inboundMessage.setMessageContent(eventData.activeSection.note)
    }
    
    log.debug("Inbound Message: ${inboundMessage}")
    inboundMessage.save(flush:true, failOnError:true)
  }
}
