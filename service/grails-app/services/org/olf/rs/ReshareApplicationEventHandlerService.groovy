package org.olf.rs;

import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.Status
import org.olf.okapi.modules.directory.Symbol;
import groovy.json.JsonOutput;
import java.time.LocalDateTime;

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
    'STATUS_REQ_SUPPLIER_IDENTIFIED_ind': { service, eventData ->
      service.sendToNextLender(eventData);
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

  // Requests are created with a STATE of IDLE, this handler validates the request and sets the state to VALIDATED, or ERROR
  public void handleNewPatronRequestIndication(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::handleNewPatronRequestIndication(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      if ( ( req != null ) && ( req.state?.code == 'REQ_IDLE' ) ) {

        // If the role is requester then validate the request and set the state to validated
        if ( req.isRequester == true ) {
          log.debug("Got request ${req}");
          log.debug(" -> Request is currently REQ_IDLE - transition to REQ_VALIDATED");
          req.state = Status.lookup('PatronRequest', 'REQ_VALIDATED');
          auditEntry(req, Status.lookup('PatronRequest', 'REQ_IDLE'), Status.lookup('PatronRequest', 'REQ_VALIDATED'), 'Request Validated', null);
          req.save(flush:true, failOnError:true)
        }
        else {
          log.debug("No action to take as a responder (yet)");
        }
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != REQ_IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title} ${it.state?.shortcode}");
        }
      }
    }
    log.debug("==================================================")
  }

  // This takes a request with the state of VALIDATED and changes the state to REQ_SOURCING_ITEM, and then on to REQ_SUPPLIER_IDENTIFIED
  public void sourcePatronRequest(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::sourcePatronRequest(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      PatronRequest req = delayedGet(eventData.payload.id);
      if ( ( req.isRequester == true ) && ( req != null ) && ( req.state?.code == 'REQ_VALIDATED' ) ) {

        req.lock();

        log.debug("Got request ${req}");
        log.debug(" -> Request is currently VALIDATED - transition to REQ_SOURCING_ITEM");
        req.state = Status.lookup('PatronRequest', 'REQ_SOURCING_ITEM');
        req.save(flush:true, failOnError:true)


        if(req.rota.size() != 0) {
          log.debug("Found a potential supplier for ${req}");
          log.debug(" -> Request is currently REQ_SOURCING_ITEM - transition to REQ_SUPPLIER_IDENTIFIED");
          req.state = Status.lookup('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED');
          auditEntry(req, Status.lookup('PatronRequest', 'REQ_VALIDATED'), Status.lookup('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), 'Request supplied with Lending String', null);
          req.save(flush:true, failOnError:true)
        } else {
          log.debug("No rota supplied - call sharedIndexService.findAppropriateCopies to find appropriate copies");
          // NO rota supplied - see if we can use the shared index service to locate appropriate copies
          // N.B. grails-app/conf/spring/resources.groovy causes a different implementation to be injected
          // here in the test environments.
          List<AvailabilityStatement> sia = sharedIndexService.findAppropriateCopies([title:req.title])
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
                                                     copyIdentifier:av_stmt.copyIdentifier))
              }
            }
            req.state = Status.lookup('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED');
            auditEntry(req, Status.lookup('PatronRequest', 'REQ_VALIDATED'), Status.lookup('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), 
                       'Lending String calculated from shared index', null);
            req.save(flush:true, failOnError:true)
          }
          else {
            log.error("Unable to identify a rota for ID ${eventData.payload.id}")
            req.state = Status.lookup('PatronRequest', 'REQ_END_OF_ROTA');
            auditEntry(req, Status.lookup('PatronRequest', 'REQ_VALIDATED'), Status.lookup('PatronRequest', 'REQ_END_OF_ROTA'), 
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
    log.debug("==================================================")
  }


  // This takes a request with the state of REQ_SUPPLIER_IDENTIFIED and changes the state to REQUEST_SENT_TO_SUPPLIER
  public void sendToNextLender(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::sendToNextLender(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      req.lock()
      if ( ( req != null ) && ( req.state?.code == 'REQ_SUPPLIER_IDENTIFIED' ) ) {
        log.debug("Got request ${req}");
        
        //TODO - sendRequest called here, make it do stuff - A request to send a protocol level resource sharing request message
        Map request_message_request = [
          messageType:'REQUEST',
          request: [
            title: req.title,
            patronReference: req.patronReference
          ]
        ]

        if ( req.rota.size() > 0 ) {
          boolean request_sent = false;

          // There may be problems with entries in the lending string, so we loop through the rota
          // until we reach the end, or we find a potential lender we can talk to. The request must
          // also explicitly state a requestingInstitutionSymbol
          while ( ( !request_sent ) && 
                  ( req.rotaPosition?:-1 < req.rota.size() ) && 
                  ( req.requestingInstitutionSymbol != null ) ) {
            // We have rota entries left, work out the next one
            req.rotaPosition = (req.rotaPosition!=null ? req.rotaPosition+1 : 0 )

            // get the responder
            PatronRequestRota prr = req.rota.find( { it.rotaPosition == req.rotaPosition } )
            if ( prr != null ) {
              String next_responder = prr.directoryId
              // send the message

              // Fill out the directory entry reference if it's not currently set.
              if ( ( next_responder != null ) && (prr.peer == null ) ) {
                String[] name_compnents = next_responder.split(':')
                if ( name_compnents.length == 2 ) {
                  log.debug("Attempting to locate Symbol ${name_compnents}");
                  def peer_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
                                                      [authority:name_compnents[0], symbol:name_compnents[1]]);
                  if ( ( peer_list.size() == 1 ) &&
                       ( peer_list[0].owner != null ) ) {
                    prr.lock();
                    prr.peer = peer_list[0].owner
                    prr.save(flush:true, failOnError:true)
                  }
                  else {
                    log.warn("Unable to set peer institution");
                  }
                }
                else {
                  log.warn("Cannot understand symbol ${next_responder}");
                }

                // Probably need a lender_is_valid check here
                protocolMessageService.sendProtocolMessage(req.requestingInstitutionSymbol, next_responder, request_message_request)
                request_sent = true;
              }
              else {
                log.warn("Lender at position ${req.rotaPosition} invalid, skipping");
              }
            }
            else {
              log.error("Unable to find rota entry at position ${req.rotaPosition}. Try next");
            }
          }

          // Did we send a request?
          if ( request_sent ) {
            log.debug("sendToNextLender sent to next lender.....");
            req.state = Status.lookup('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER');
            auditEntry(req, Status.lookup('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), Status.lookup('PatronRequest', 'REQ_REQUEST_SENT_TO_SUPPLIER'), 
                       'Sent to next lender', null);
            req.save(flush:true, failOnError:true)
          }
          else {
            // END OF ROTA
            log.warn("sendToNextLender reached the end of the lending string.....");
            req.state = Status.lookup('PatronRequest', 'REQ_END_OF_ROTA');
            auditEntry(req, Status.lookup('PatronRequest', 'REQ_SUPPLIER_IDENTIFIED'), Status.lookup('PatronRequest', 'REQ_END_OF_ROTA'), 
                       'End of rota', null);
            req.save(flush:true, failOnError:true)
          }
        }
        else {
          log.warn("Annot send to next lender - rota is empty");
          req.state = Status.lookup('PatronRequest', 'REQ_END_OF_ROTA');
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
    log.debug("==================================================")
  }


  /**
   * A new request has been received from a peer institution. We will need to create a request where isRequester==false
   */
  public void handleRequestMessage(Map eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::handleRequestMessage(${eventData})");
    if ( eventData.request != null ) {
      log.debug("*** Create new request***");
      PatronRequest pr = new PatronRequest(eventData.request)
      pr.isRequester=false;
      auditEntry(pr, null, null, 'New request (Lender role) created as a result of protocol interaction', null);
      pr.save(flush:true, failOnError:true)
    }
    else {
      log.error("A REQUEST indicaiton must contain a request key with properties defining the sought item - eg request.title");
    }

    log.debug("==================================================")
  }

  /**
   * Sometimes, we might receive a notification before the source transaction has committed. THats rubbish - so here we retry
   * up to 5 times.
   */
  public PatronRequest delayedGet(String pr_id) {
    log.debug("PatronRequest called")
    PatronRequest result = null;
    int retries = 0;

    try {
      while ( ( result == null ) && (retries < MAX_RETRIES) ) {
        result = PatronRequest.get(pr_id)
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

  private void auditEntry(PatronRequest pr, Status from, Status to, String message, Map data) {

    String json_data = ( data != null ) ? JsonOutput.toJson(data).toString() : null;
    LocalDateTime ts = LocalDateTime.now();
    log.debug("add audit entry at ${ts}");

    pr.addToAudit( new PatronRequestAudit(
      patronRequest: pr,
      dateCreated:ts,
      fromStatus:from,
      toStatus:to,
      duration:null,
      message: message,
      auditData: json_data))
  }
}
