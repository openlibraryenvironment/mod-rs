package org.olf.rs;


import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.PatronRequestNotification
import org.olf.rs.PatronRequestLoanCondition
import org.olf.rs.statemodel.Status
import org.olf.rs.statemodel.StateModel
import org.olf.okapi.modules.directory.Symbol;
import org.olf.okapi.modules.directory.DirectoryEntry;
import groovy.json.JsonOutput;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZonedDateTime;

import groovy.sql.Sql
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*
import static groovyx.net.http.HttpBuilder.configure
import org.olf.rs.lms.ItemLocation;

/**
 * Handle application events.
 *
 * This class is all about high level reshare events - the kind of things users want to customise and modify. Application functionality
 * that might change between implementations can be added here.
 * REMEMBER: Notifications are not automatically within a tenant scope - handlers will need to resolve that.
 *
 * StateModel: https://app.diagrams.net/#G1fC5Xtj5fbk_Z7dIgjqgP3flBQfTSz-1s
 */
public class ReshareApplicationEventHandlerService {

  private static final int MAX_RETRIES = 10;
  
  ProtocolMessageService protocolMessageService
  ProtocolMessageBuildingService protocolMessageBuildingService
  SharedIndexService sharedIndexService
  HostLMSService hostLMSService
  ReshareActionService reshareActionService
  StatisticsService statisticsService
  PatronNoticeService patronNoticeService

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
    'STATUS_REQ_CANCELLED_WITH_SUPPLIER_ind': { service, eventData ->
      service.handleCancelledWithSupplier(eventData);
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
    'STATUS_REQ_SHIPPED_ind': { service, eventData ->
    },
    'STATUS_REQ_BORROWING_LIBRARY_RECEIVED_ind': { service, eventData ->
    },
    'STATUS_REQ_AWAITING_RETURN_SHIPPING_ind': { service, eventData ->
    },
    'STATUS_REQ_BORROWER_RETURNED_ind': { service, eventData ->
    },
    'STATUS_RES_OVERDUE_ind': { service, eventData ->
      log.debug("Overdue event handler called");
      service.handleResOverdue(eventData);
    },
    'MESSAGE_REQUEST_ind': { service, eventData ->
      // This is called on an incoming REQUEST - can be loopback, ISO18626, ISO10161, etc.
      service.handleRequestMessage(eventData);
    },
    'SUPPLYING_AGENCY_MESSAGE_ind': { service, eventData ->
      service.handleSupplyingAgencyMessage(eventData);
    },
    'STATUS_RES_CANCEL_REQUEST_RECEIVED_ind': { service, eventData ->
      service.handleCancelRequestReceived(eventData);
    },
    'STATUS_RES_CHECKED_IN_TO_RESHARE_ind': { service, eventData ->
      service.handleResponderItemCheckedIn(eventData);
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

        def lookup_patron = reshareActionService.lookupPatron(req, null)

        if ( lookup_patron.callSuccess ) {
          boolean patronValid = lookup_patron.patronValid
          // If we were supplied a pickup location code, attempt to resolve it here
          /*TODO the lmsLocationCode might not be unique... probably need to search for DirectoryEntry with tag "pickup",
            * where slug==requesterSlug OR ownerAtTopOfHeirachy==requesterSlug... Probably needs custom find method on DirectoryEntry
            */
          if( req.pickupLocationCode ) {
            DirectoryEntry pickup_loc = DirectoryEntry.findByLmsLocationCode(req.pickupLocationCode)
            
            if(pickup_loc != null) {
              req.resolvedPickupLocation = pickup_loc;
              req.pickupLocation = pickup_loc.name;
            }
          }

          if ( req.requestingInstitutionSymbol != null ) {
            // We need to validate the requsting location - and check that we can act as requester for that symbol
            Symbol s = resolveCombinedSymbol(req.requestingInstitutionSymbol);
            if (s != null) {
              // We do this separately so that an invalid patron does not stop information being appended to the request
              req.resolvedRequester = s
            }
            
            // If s != null and patronValid == true then the request has passed validation
            if ( s != null && patronValid) {
              log.debug("Got request ${req}");
              log.debug(" -> Request is currently REQ_IDLE - transition to REQ_VALIDATED");
              def validated_state = lookupStatus('PatronRequest', 'REQ_VALIDATED')
              auditEntry(req, req.state, validated_state, 'Request Validated', null);
              req.state = validated_state;
              patronNoticeService.triggerNotices(req, "new_request");
            } else if (s == null) {
              // An unknown requesting institution symbol is a bigger deal than an invalid patron
              req.needsAttention=true;
              log.warn("Unkown requesting institution symbol : ${req.requestingInstitutionSymbol}");
              req.state = lookupStatus('PatronRequest', 'REQ_ERROR');
              auditEntry(req, 
                          lookupStatus('PatronRequest', 'REQ_IDLE'), 
                          lookupStatus('PatronRequest', 'REQ_ERROR'), 
                          'Unknown Requesting Institution Symbol: '+req.requestingInstitutionSymbol, null);
            }
            else {
              // If we're here then the requesting institution symbol was fine but the patron is invalid
              def invalid_patron_state = lookupStatus('PatronRequest', 'REQ_INVALID_PATRON')
              String message = "Failed to validate patron with id: \"${req.patronIdentifier}\".${lookup_patron?.status != null ? " (Patron state=${lookup_patron.status})" : ""}".toString()
              auditEntry(req, req.state, invalid_patron_state, message, null);
              req.state = invalid_patron_state;
              req.needsAttention=true;
            }
          }
          else {
            req.state = lookupStatus('PatronRequest', 'REQ_ERROR');
            req.needsAttention=true;
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_IDLE'), lookupStatus('PatronRequest', 'REQ_ERROR'), 'No Requesting Institution Symbol', null);
          }
        }
        else {
          // unexpected error in Host LMS call
          req.needsAttention=true;
          String message = 'Host LMS integration: lookupPatron call failed. Review configuration and try again or deconfigure host LMS integration in settings. '+lookup_patron?.problems
          auditEntry(req, req.state, req.state, message, null);
        }

        if ( ( req.systemInstanceIdentifier != null ) && ( req.systemInstanceIdentifier.length() > 0 ) ) {
          log.debug("calling fetchSharedIndexRecords");
          List<String> bibRecords = sharedIndexService.getSharedIndexActions().fetchSharedIndexRecords([systemInstanceIdentifier: req.systemInstanceIdentifier]);
          if (bibRecords?.size() == 1) req.bibRecord = bibRecords[0];
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
          if ( auto_respond?.toLowerCase().startsWith('on') ) {
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
          def operation_data = [:]
          operation_data.candidates=[]
          log.debug("No rota supplied - call sharedIndexService.findAppropriateCopies to find appropriate copies");
          // NO rota supplied - see if we can use the shared index service to locate appropriate copies
          // N.B. grails-app/conf/spring/resources.groovy causes a different implementation to be injected
          // here in the test environments.
          List<AvailabilityStatement> sia = sharedIndexService.getSharedIndexActions().findAppropriateCopies(req.getDescriptiveMetadata());
          log.debug("Result of shared index lookup : ${sia}");
          int ctr = 0;

          List<Map> enrichedRota = createRankedRota(sia)
          log.debug("Created ranked rota: ${enrichedRota}")

          if (  enrichedRota.size() > 0 ) {

            // Pre-process the list of candidates
            enrichedRota?.each { av_stmt ->
              if ( av_stmt.symbol != null ) {
                operation_data.candidates.add([symbol:av_stmt.symbol, message:"Added"]);
                if ( av_stmt.illPolicy == 'Will lend' ) {

                  log.debug("Adding to rota: ${av_stmt}");

                  // Pull back any data we need from the shared index in order to sort the list of candidates
                  req.addToRota (new PatronRequestRota(
                                                       patronRequest:req,
                                                       rotaPosition:ctr++, 
                                                       directoryId:av_stmt.symbol,
                                                       instanceIdentifier:av_stmt.instanceIdentifier,
                                                       copyIdentifier:av_stmt.copyIdentifier,
                                                       state: lookupStatus('PatronRequest', 'REQ_IDLE'),
                                                       loadBalancingScore:av_stmt.loadBalancingScore,
                                                       loadBalancingReason:av_stmt.loadBalancingReason))
                }
                else {
                  operation_data.candidates.add([symbol:av_stmt.symbol, message:"Skipping - illPolicy is \"${av_stmt.illPolicy}\""]);
                }
              }
            }

            // Procesing
            req.state = lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_VALIDATED'), lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), 
                       'Ratio-Ranked lending string calculated from shared index', null);
            req.save(flush:true, failOnError:true)
          }
          else {
            // ToDo: Ethan: if LastResort app setting is set, add lenders to the request.
            log.error("Unable to identify any suppliers for patron request ID ${eventData.payload.id}")
            req.state = lookupStatus('PatronRequest', 'REQ_END_OF_ROTA');
            auditEntry(req, lookupStatus('PatronRequest', 'REQ_VALIDATED'), lookupStatus('PatronRequest', 'REQ_END_OF_ROTA'), 'Unable to locate lenders. Availability from SI was'+sia, null);
            req.save(flush:true, failOnError:true)
            patronNoticeService.triggerNotices(req, "end_of_rota");
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
             ( req.state?.code == 'REQ_CANCELLED_WITH_SUPPLIER' ) ||
             ( req.state?.code == 'REQ_UNFILLED' ) ) ) {
        log.debug("Got request ${req} (HRID Is ${req.hrid}) (Status code is ${req.state?.code})");
        
        Map request_message_request = protocolMessageBuildingService.buildRequestMessage(req);
        
        // search through the rota for the one with the highest load balancing score 
         
        if ( req.rota.size() > 0 ) {
          def top_entry = null;
          for(int i = 0; i < req.rota.size(); i++) {
            def current_entry = req.rota[i];
            if(top_entry == null ||
              ( current_entry.get("loadBalancingScore") > top_entry.get("loadBalancingScore")) 
            ) {
              top_entry = current_entry;
            }     
          }
          def symbol = top_entry.get("symbol");
          log.debug("Top symbol is ${symbol} with status ${symbol?.owner?.status?.value}");
          if(symbol != null) {
            def ownerStatus = symbol.owner?.status?.value;
            if ( ownerStatus == "Managed" || ownerStatus == "managed" ) {
              log.debug("Top lender is local, going to review state");
              req.state = lookupStatus('PatronRequest', 'REQ_LOCAL_REVIEW');
              auditEntry(req, lookupStatus('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), 
                lookupStatus('PatronRequest', 'REQ_LOCAL_REVIEW'), 'Sent to local review', null);
              req.save(flush:true, failOnError:true);
              return; //Nothing more to do here
            } else {
              log.debug("Owner status for ${symbol} is ${ownerStatus}");
            }
          }
        }
        
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
                  req.resolvedSupplier = s;
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

                if ( ( prr.instanceIdentifier != null ) && ( prr.instanceIdentifier.length() > 0 ) ) {
                  // update request_message_request.systemInstanceIdentifier to the system number specified in the rota
                  request_message_request.bibliographicInfo.systemInstanceIdentifier = prr.instanceIdentifier;
                }
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
            patronNoticeService.triggerNotices(req, "end_of_rota");
          }
        }
        else {
          log.warn("Cannot send to next lender - rota is empty");
          req.state = lookupStatus('PatronRequest', 'REQ_END_OF_ROTA');
          req.save(flush:true, failOnError:true)
          patronNoticeService.triggerNotices(req, "end_of_rota");
        }
        
        log.debug(" -> Request is currently REQ_SUPPLIER_IDENTIFIED - transition to REQUEST_SENT_TO_SUPPLIER");
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state (${req?.state?.code}) is not supported. Supported states are REQ_SUPPLIER_IDENTIFIED and REQ_CANCELLED_WITH_SUPPLIER");
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

      // Add publisher information to Patron Request
      Map publicationInfo = eventData.publicationInfo
      if (publicationInfo.publisher) {
        pr.publisher = publicationInfo.publisher
      }
      pr.publicationType = pr.lookupPublicationType( publicationInfo.publicationType )
      if (publicationInfo.publicationType) {
        pr.publicationType = pr.lookupPublicationType( publicationInfo.publicationType )
      }
      if (publicationInfo.publicationDate) {
        pr.publicationDate = publicationInfo.publicationDate
      }
      if (publicationInfo.publicationDateOfComponent) {
        pr.publicationDateOfComponent = publicationInfo.publicationDateOfComponent
      }
      if (publicationInfo.placeOfPublication) {
        pr.placeOfPublication = publicationInfo.placeOfPublication
      }
      // Add service information to Patron Request
      Map serviceInfo = eventData.serviceInfo
      if (serviceInfo.serviceType) {
        pr.serviceType = pr.lookupServiceType( serviceInfo.serviceType )
      }
      if (serviceInfo.needBeforeDate) {
        // This will come in as a string, will need parsing
        try {
          pr.neededBy = LocalDate.parse(serviceInfo.needBeforeDate)
        } catch (Exception e) {
          log.debug("Failed to parse neededBy date (${serviceInfo.needBeforeDate}): ${e.message}")
        }
      }
      if (serviceInfo.note) {
       pr.patronNote = serviceInfo.note
      }

      // UGH! Protocol delivery info is not remotely compatible with the UX prototypes - sort this later
      if ( eventData.requestedDeliveryInfo?.address instanceof Map ) {
        if ( eventData.requestedDeliveryInfo?.address.physicalAddress instanceof Map ) {
          log.debug("Incoming request contains delivery info: ${eventData.requestedDeliveryInfo?.address?.physicalAddress}");
          // We join all the lines of physical address and stuff them into pickup location for now.
          String stringified_pickup_location = eventData.requestedDeliveryInfo?.address?.physicalAddress.collect{k,v -> v}.join(' ');

          // If we've not been given any address information, don't translate that into a pickup location
          if ( stringified_pickup_location?.trim()?.length() > 0 ) 
            pr.pickupLocation = stringified_pickup_location
        }
      }

      // Add patron information to Patron Request
      Map patronInfo = eventData.patronInfo
      if (patronInfo.patronId) {
        pr.patronIdentifier = patronInfo.patronId
      }
      if (patronInfo.surname) {
        pr.patronSurname = patronInfo.surname
      }
      if (patronInfo.givenName) {
        pr.patronGivenName = patronInfo.givenName
      }
      if (patronInfo.patronType) {
        pr.patronType = patronInfo.patronType
      }
      if (patronInfo.patronReference) {
        pr.patronReference = patronInfo.patronReference
      }
      
      pr.supplyingInstitutionSymbol = "${header.supplyingAgencyId?.agencyIdType}:${header.supplyingAgencyId?.agencyIdValue}"
      pr.requestingInstitutionSymbol = "${header.requestingAgencyId?.agencyIdType}:${header.requestingAgencyId?.agencyIdValue}"

      pr.resolvedRequester = resolvedRequestingAgency;
      pr.resolvedSupplier = resolvedSupplyingAgency;
      pr.peerRequestIdentifier = header.requestingAgencyRequestId

      // For reshare - we assume that the requester is sending us a globally unique HRID and we would like to be
      // able to use that for our request.
      pr.hrid = header.requestingAgencyRequestId

      if ( ( pr.systemInstanceIdentifier != null ) && ( pr.systemInstanceIdentifier.length() > 0 ) ) {
        log.debug("Incoming request with pr.systemInstanceIdentifier - calling fetchSharedIndexRecords ${pr.systemInstanceIdentifier}");
        List<String> bibRecords = sharedIndexService.getSharedIndexActions().fetchSharedIndexRecords([systemInstanceIdentifier: pr.systemInstanceIdentifier]);
        if (bibRecords?.size() == 1) pr.bibRecord = bibRecords[0];
      }

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

  PatronRequest lookupPatronRequest(String id, boolean isRequester) {
    def patronRequestList = PatronRequest.executeQuery('select pr from PatronRequest as pr where (pr.id=:id OR pr.hrid=:id) and pr.isRequester=:isreq',
                                                      [id:id, isreq:isRequester])

    if (patronRequestList.size() != 1 && patronRequestList.size() != 0) {
      throw RuntimeException("Could not resolve patronRequest with id ${id}")
    } else if (patronRequestList.size() == 1) {
      return patronRequestList[0];
    }
    return null;
  }

  PatronRequest lookupPatronRequestByPeerId(String id) {
    return PatronRequest.findByPeerRequestIdentifier(id);
  }
  
  def handleResOverdue(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleOverdue handler called with eventData ${eventData}");

   PatronRequest.withNewTransaction { transactionStatus ->
     def pr = delayedGet(eventData.payload.id, true);
     if(pr == null) {
       log.warn("Unable to locate request for ID ${eventData.payload.id}");
     } else if(pr.isRequester) {
       log.debug("pr ${pr.id} is requester, not sending protocol message");
     } else {
       log.debug("Sending protocol message with overdue status change from PatronRequest ${pr.id}");
       Map params = [ note : "Request is Overdue"]
       //reshareActionService.sendSupplyingAgencyMessage(pr, "Notification", null, params)
       reshareActionService.sendStatusChange(pr, 'Overdue', "Request is Overdue");
     }
   }
    
  }

  /**
   * An incoming message to the requesting agency FROM the supplying agency - so we look in 
   * eventData.header?.requestingAgencyRequestId to find our own ID for the request.
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleSupplyingAgencyMessage(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleSupplyingAgencyMessage(${eventData})");
    def result = [:]

    /* Occasionally the incoming status is not granular enough, so we deal with it separately in order
     * to be able to cater to "in-between" statuses, such as Conditional--which actually comes in as "ExpectsToSupply"
    */
    Map incomingStatus = eventData.statusInfo;

    try {
      if ( eventData.header?.requestingAgencyRequestId == null ) {
        result.status = "ERROR"
        result.errorType = "BadlyFormedMessage"
        throw new Exception("requestingAgencyRequestId missing");
      }
        

      PatronRequest pr = lookupPatronRequest(eventData.header.requestingAgencyRequestId, true)
      if ( pr == null )
        throw new Exception("Unable to locate PatronRequest corresponding to ID or hrid in requestingAgencyRequestId \"${eventData.header.requestingAgencyRequestId}\"");

      // if eventData.deliveryInfo.itemId then we should stash the item id

      if (eventData?.deliveryInfo ) {

        if (eventData?.deliveryInfo?.loanCondition) {
          log.debug("Loan condition found: ${eventData?.deliveryInfo?.loanCondition}")
          incomingStatus = [status: "Conditional"]


          // Save the loan condition to the patron request
          String loanCondition = eventData?.deliveryInfo?.loanCondition
          Symbol relevantSupplier = resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue)
          String note = eventData.messageInfo?.note

          addLoanConditionToRequest(pr, loanCondition, relevantSupplier, note)
        }

        // If we're being told about the barcode of the selected item (and we don't already have one saved), stash it in selectedItemBarcode on the requester side
        if (!pr.selectedItemBarcode && eventData.deliveryInfo.itemId) {
          pr.selectedItemBarcode = eventData.deliveryInfo.itemId;
        }
      }

      // Awesome - managed to look up patron request - see if we can action
      if ( eventData.messageInfo?.reasonForMessage != null) {

        // If there is a note, create notification entry
        if (eventData.messageInfo?.note) {
          incomingNotificationEntry(pr, eventData, true)
        }

        switch ( eventData.messageInfo?.reasonForMessage ) {
          case 'RequestResponse':
            break;
          case 'StatusRequestResponse':
            break;
          case 'RenewResponse':
            break;
          case 'CancelResponse':
            switch (eventData.messageInfo.answerYesNo) {
              case 'Y':
                log.debug("Affirmative cancel response received")
                // The cancel response ISO18626 message should contain a status of "Cancelled", and so this case will be handled by handleStatusChange
                break;
              case 'N':
                log.debug("Negative cancel response received")
                def previousState = lookupStatus('PatronRequest', pr.previousStates[pr.state.code])
                auditEntry(pr, pr.state, previousState, "Supplier denied cancellation.", null)
                pr.previousStates[pr.state.code] = null
                pr.state = previousState
                break;
              default:
                log.error("handleSupplyingAgencyMessage does not know how to deal with a CancelResponse answerYesNo of ${eventData.messageInfo.answerYesNo}")
            }
            break;
          case 'StatusChange':
            break;
          case 'Notification':
            // If this note starts with #ReShareAddLoanCondition# then we know that we have to add another loan condition to the request -- might just work automatically.
            auditEntry(pr, pr.state, pr.state, "Notification message received from supplying agency: ${eventData.messageInfo.note}", null)
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
      
      if ( eventData.statusInfo?.dueDate ) {
        pr.dueDateRS = eventData.statusInfo.dueDate;        
      } else {
        log.debug("No duedate found in eventData.statusInfo");
      }

      if ( incomingStatus != null ) {
        handleStatusChange(pr, incomingStatus, eventData.header.supplyingAgencyRequestId);
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

        // If there's a note, create a notification entry
        if (eventData.activeSection?.note != null && eventData.activeSection?.note != "") {
          incomingNotificationEntry(pr, eventData, false)
        }

        switch ( eventData.activeSection?.action ) {
          case 'Received':
            auditEntry(pr, pr.state, pr.state, "Shipment received by requester", null)
            pr.save(flush: true, failOnError: true)
            break;
          case 'ShippedReturn':
            def new_state = lookupStatus('Responder', 'RES_ITEM_RETURNED')
            auditEntry(pr, pr.state, new_state, "Item(s) Returned by requester", null)
            pr.state = new_state;
            pr.save(flush: true, failOnError: true)
            break;
          case 'Notification':
            Map messageData = eventData.activeSection

            /* If the message is preceded by #ReShareLoanConditionAgreeResponse#
             * then we'll need to check whether or not we need to change state.
            */
            if (messageData.note.startsWith("#ReShareLoanConditionAgreeResponse#")) {
              // First check we're in the state where we need to change states, otherwise we just ignore this and treat as a regular message, albeit with warning
              if (pr.state.code == "RES_PENDING_CONDITIONAL_ANSWER") {
                def new_state = lookupStatus('Responder', pr.previousStates[pr.state.code])
                auditEntry(pr, pr.state, new_state, "Requester agreed to loan conditions, moving request forward", null)
                pr.previousStates[pr.state.code] = null;
                pr.state = new_state;
                markAllLoanConditionsAccepted(pr)
              } else {
                // Loan conditions were already marked as agreed
                auditEntry(pr, pr.state, pr.state, "Requester agreed to loan conditions, no action required on supplier side", null)
              }
            } else {
              auditEntry(pr, pr.state, pr.state, "Notification message received from requesting agency: ${messageData.note}", null)
            }
            pr.save(flush: true, failOnError: true)
            break;

          case 'Cancel':
            // We cannot cancel a shipped item
            auditEntry(pr, pr.state, lookupStatus('Responder', 'RES_CANCEL_REQUEST_RECEIVED'), "Requester requested cancellation of the request", null)
            pr.previousStates['RES_CANCEL_REQUEST_RECEIVED'] = pr.state.code;
            pr.state = lookupStatus('Responder', 'RES_CANCEL_REQUEST_RECEIVED')
            pr.requesterRequestedCancellation = true
            pr.save(flush: true, failOnError: true)
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

  private void handleCancelRequestReceived(eventData) {
    PatronRequest.withNewTransaction { transaction_status ->
      def req = delayedGet(eventData.payload.id, true);
      reshareActionService.sendMessage(req, [note:'Recieved Cancellation Request']);
      String auto_cancel = AppSetting.findByKey('auto_responder_cancel')?.value
      if ( auto_cancel?.toLowerCase().startsWith('on') ) {
        // System has auto-respond cancel on
        if ( req.state?.code=='RES_ITEM_SHIPPED' ) {
          // Revert the state to it's original before the cancel request was received - previousState
          def new_state = lookupStatus('Responder', req.previousStates['RES_CANCEL_REQUEST_RECEIVED']);
          auditEntry(req, req.state, new_state, "AutoResponder:Cancel is ON - but item is SHIPPED. Responding NO to cancel, revert to previous state ", null)
          req.state=new_state
          req.previousStates['RES_CANCEL_REQUEST_RECEIVED'] = null;
          reshareActionService.sendSupplierCancelResponse(req, [cancelResponse:'no'])
        }
        else {
          def new_state = lookupStatus('Responder', 'RES_CANCELLED')
          if ( new_state ) {
            req.state=new_state
            auditEntry(req, req.state, new_state, "AutoResponder:Cancel is ON - responding YES to cancel request", null);
          }
          else {
            log.error("Accepting requester cancellation--unable to find state: RES_CANCELLED")
            auditEntry(req, req.state, req.state, "AutoResponder:Cancel is ON - responding YES to cancel request (ERROR locating RES_CANCELLED state)", null);
          }
          reshareActionService.sendSupplierCancelResponse(req, [cancelResponse:'yes'])
        }
        req.save(flush: true, failOnError: true);
      }
      else {
        // Set needs attention=true
        auditEntry(req, req.state, req.state, "Cancellation Request Recieved", null);
        req.needsAttention=true;
        req.save(flush: true, failOnError: true);
      }
    }
  }

  private void handleCancelledWithSupplier(eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleCancelledWithSupplier(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id, true);
      // We must have found the request, and it as to be in a state of cancelled with supplier
      if (( req != null ) && ( req.state?.code == 'REQ_CANCELLED_WITH_SUPPLIER' )) {
        log.debug("Got request ${req} (HRID Is ${req.hrid})");

        if (req.requestToContinue == true) {
          log.debug("Request to continue, sending to next lender")
          auditEntry(req, req.state, req.state, 'Request to continue, sending to next lender', null);
          req.state = lookupStatus('PatronRequest', 'REQ_UNFILLED')
        } else {
          log.debug("Cancelling request")
          auditEntry(req, req.state, lookupStatus('PatronRequest', 'REQ_CANCELLED'), 'Request cancelled', null);
          req.state = lookupStatus('PatronRequest', 'REQ_CANCELLED')
        }
        req.save(flush:true, failOnError: true)
      } else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state (${req?.state?.code}) is not REQ_CANCELLED_WITH_SUPPLIER.");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
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
        case 'Conditional':
          log.debug("Moving to state REQ_CONDITIONAL_ANSWER_RECEIVED")
          def new_state = lookupStatus('PatronRequest', 'REQ_CONDITIONAL_ANSWER_RECEIVED')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state
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
          def new_state = lookupStatus('PatronRequest', 'REQ_CANCELLED_WITH_SUPPLIER')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
          pr.state=new_state
          if ( prr != null ) prr.state = new_state
          break;
        case 'LoanCompleted':
          def new_state = lookupStatus('PatronRequest', 'REQ_REQUEST_COMPLETE')
          auditEntry(pr, pr.state, new_state, 'Protocol message', null);
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
    log.debug("delayedGet called (${wth_lock})")
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
    finally {
      log.debug("Delayed get returning ${result}")
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
    log.debug("add audit entry at ${ts}, from ${from} to ${to}, message ${message}");

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
        reshareActionService.sendResponse(pr,  'ExpectToSupply', [:])
      }
      else {
        auditEntry(pr, lookupStatus('Responder', 'RES_IDLE'), lookupStatus('Responder', 'RES_UNFILLED'), 'AutoResponder Failed to route to location '+location, null);
        log.debug("Send unfilled(No Copy) response to ${pr.requestingInstitutionSymbol}");
        reshareActionService.sendResponse(pr,  'Unfilled', ['reason':'No copy'])
        pr.state=lookupStatus('Responder', 'RES_UNFILLED')
      }
    }
    else {
      log.debug("Send unfilled(No copy) response to ${pr.requestingInstitutionSymbol}");
      reshareActionService.sendResponse(pr,  'Unfilled', ['reason':'No copy'])
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

  public void incomingNotificationEntry(PatronRequest pr, Map eventData, Boolean isRequester) {
    def inboundMessage = new PatronRequestNotification()

    inboundMessage.setPatronRequest(pr)
    inboundMessage.setSeen(false)
    // This line should grab timestamp from message rather than current time.
    inboundMessage.setTimestamp(ZonedDateTime.parse(eventData.header.timestamp).toInstant())
    if (isRequester) {

      // We might want more specific information than the reason for message alone
      // also sometimes the status isn't enough by itself
      String status = eventData.statusInfo?.status;
      if (status) {
        inboundMessage.setActionStatus(status)

        if (status == "Unfilled") {
          inboundMessage.setActionData(eventData.messageInfo.reasonunfilled)
        }
      }

      // We overwrite the status information if there are loan conditions
      if (eventData.deliveryInfo?.loanCondition) {
        inboundMessage.setActionStatus("Conditional")
        inboundMessage.setActionData(eventData.deliveryInfo.loanCondition)
      }

      inboundMessage.setMessageSender(resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
      inboundMessage.setAttachedAction(eventData.messageInfo.reasonForMessage)
      inboundMessage.setMessageContent(eventData.messageInfo.note)
    } else {
      inboundMessage.setMessageSender(resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
      inboundMessage.setAttachedAction(eventData.activeSection.action)
      inboundMessage.setMessageContent(eventData.activeSection.note)
    }
    
    inboundMessage.setIsSender(false)
    
    log.debug("Inbound Message: ${inboundMessage.messageContent}")
    pr.addToNotifications(inboundMessage)
    //inboundMessage.save(flush:true, failOnError:true)
  }

  public String stripOutSystemCode(String string) {
    String returnString = string
    def systemCodes = [
      "#ReShareAddLoanCondition#", 
      "#ReShareLoanConditionAgreeResponse#",
      "#ReShareSupplierConditionsAssumedAgreed#",
      "#ReShareSupplierAwaitingConditionConfirmation#"
      ]
      systemCodes.each {code ->
        if (string.contains(code)) {
          returnString.replace(code, "")
        }
      }
      return returnString
  }

  public void addLoanConditionToRequest(PatronRequest pr, String code, Symbol relevantSupplier, String note = null) {
    def loanCondition = new PatronRequestLoanCondition()
    loanCondition.setPatronRequest(pr)
    loanCondition.setCode(code)
    if (note != null) {
      loanCondition.setNote(stripOutSystemCode(note))
    }
    loanCondition.setRelevantSupplier(relevantSupplier)

    pr.addToConditions(loanCondition)
  }

  /**
   * Take a list of availability statements and turn it into a ranked rota
   * @param sia - List of AvailabilityStatement
   * @return [
   *   [
   *     symbol:
   *   ]
   * ]
   */
  private List<Map> createRankedRota(List<AvailabilityStatement> sia) {
    log.debug("createRankedRota(${sia})");
    def result = []

    sia.each { av_stmt ->
      log.debug("Considering rota entry: ${av_stmt}");

      // 1. look up the directory entry for the symbol
      Symbol s = ( av_stmt.symbol != null ) ? resolveCombinedSymbol(av_stmt.symbol) : null;

      if ( s != null ) {
        log.debug("Refine availability statement ${av_stmt} for symbol ${s}");

        // 2. See if the entry has policy.ill.loan_policy set to "Not Lending" - if so - skip
        // s.owner.customProperties is a container :: com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
        def entry_loan_policy = s.owner.customProperties?.value?.find { it.definition.name=='ill.loan_policy' }
        log.debug("Symbols.owner.custprops['ill.loan_policy] : ${entry_loan_policy}");

        if ( ( entry_loan_policy == null ) ||
             ( entry_loan_policy.value?.value == 'lending_all_types' ) ) {

          Map peer_stats = statisticsService.getStatsFor(s);

          def loadBalancingScore = null;
          def loadBalancingReason = null;
          def ownerStatus = s.owner?.status?.value;
          log.debug("Found status of ${ownerStatus} for symbol ${s}");
          if ( ownerStatus == null ) {
            log.debug("Unable to get owner status for ${s}");
          } else if ( ownerStatus == "Managed" || ownerStatus == "managed" ) {
            loadBalancingScore = 10000;
            loadBalancingReason = "Local lending sources prioritized";
          } else if ( peer_stats != null ) {
            // 3. See if we can locate load balancing informaiton for the entry - if so, calculate a score, if not, set to 0
            double lbr = peer_stats.lbr_loan/peer_stats.lbr_borrow
            long target_lending = peer_stats.current_borrowing_level*lbr
            loadBalancingScore = target_lending - peer_stats.current_loan_level
            loadBalancingReason = "LB Ratio ${peer_stats.lbr_loan}:${peer_stats.lbr_borrow}=${lbr}. Actual Borrowing=${peer_stats.current_borrowing_level}. Target loans=${target_lending} Actual loans=${peer_stats.current_loan_level} Distance/Score=${loadBalancingScore}";
          }
          else {
            loadBalancingScore = 0;
            loadBalancingReason = 'No load balancing information available for peer'
          }

          def rota_entry = [
            symbol:av_stmt.symbol,
            instanceIdentifier:av_stmt.instanceIdentifier,
            copyIdentifier:av_stmt.copyIdentifier,
            illPolicy: av_stmt.illPolicy,
            loadBalancingScore: loadBalancingScore,
            loadBalancingReason:loadBalancingReason
          ]
          result.add(rota_entry)
        }
        else {
          log.debug("Directory entry says not currently lending - ${av_stmt.symbol}/policy=${entry_loan_policy.value?.value}");
        }
      }
      else {
        log.debug("Unable to locate symbol ${av_stmt.symbol}");
      } 
    }
    
    result.toSorted { a,b -> a.loadBalancingScore <=> b.loadBalancingScore }
    log.debug("createRankedRota returns ${result}");
    return result;
  }

  /**
   * It's not clear if the system will ever need to differentiate between the status of checked in and
   * await shipping, so for now we leave the 2 states in place and just automatically transition  between them
   * this method exists largely as a place to put functions and workflows that diverge from that model
   */
  private void handleResponderItemCheckedIn(eventData) {
    log.debug("handleResponderItemCheckedIn checked in - transition to await shipping");
    PatronRequest.withNewTransaction { transaction_status ->
      def req = delayedGet(eventData.payload.id, true);
      def new_state = lookupStatus('Responder', 'RES_AWAIT_SHIP')
      auditEntry(req, req.state, new_state, 'Request awaits shipping', null);
      req.state=new_state
      req.save(flush:true, failOnError: true)
    }
  }

  public void markAllLoanConditionsAccepted(PatronRequest pr) {
    def conditions = PatronRequestLoanCondition.findAllByPatronRequest(pr)
    conditions.each {cond ->
      cond.setAccepted(true)
      cond.save(flush: true, failOnError: true)
    }
  }
}
