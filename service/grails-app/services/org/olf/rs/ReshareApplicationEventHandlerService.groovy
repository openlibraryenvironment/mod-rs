package org.olf.rs;

import grails.events.annotation.Subscriber
import groovy.lang.Closure
import grails.gorm.multitenancy.Tenants
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.Status

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
    'STATUS_VALIDATED_ind': { service, eventData ->
      service.sourcePatronRequest(eventData);

    },
    'STATUS_SOURCING_ITEM_ind': { service, eventData ->
      service.log.debug("SOURCING_ITEM state should now be SUPPLIER_IDENTIFIED");
    },
    'STATUS_SUPPLIER_IDENTIFIED_ind': { service, eventData ->
      service.sendToNextLender(eventData);
    },
    'STATUS_REQUEST_SENT_TO_SUPPLIER_ind': { service, eventData ->
      service.shipToLender(eventData);
    },
    'STATUS_ITEM_SHIPPED_ind': { service, eventData ->
      service.lenderReceived(eventData);
    },
    'STATUS_BORROWING_LIBRARY_RECEIVED_ind': { service, eventData ->
      service.lenderFinishedWithItem(eventData);
    },
    'STATUS_AWAITING_RETURN_SHIPPING_ind': { service, eventData ->
      service.lenderShippedReturn(eventData);
    },
    'STATUS_BORROWER_RETURNED_ind': { service, eventData ->
      service.itemReturned(eventData);
    },
    'MESSAGE_REQUEST_ind': { service, eventData ->
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
        Tenants.withId(eventData.tenant) {
          c.call(this, eventData);
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
      if ( ( req != null ) && ( req.state?.code == 'IDLE' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently IDLE - transition to VALIDATED");
        req.state = Status.lookup('PatronRequest', 'VALIDATED');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }

  // This takes a request with the state of VALIDATED and changes the state to SOURCING_ITEM, and then on to SUPPLIER_IDENTIFIED
  public void sourcePatronRequest(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::sourcePatronRequest(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      PatronRequest req = delayedGet(eventData.payload.id);
      if ( ( req != null ) && ( req.state?.code == 'VALIDATED' ) ) {

        req.lock();

        log.debug("Got request ${req}");
        log.debug(" -> Request is currently VALIDATED - transition to SOURCING_ITEM");
        req.state = Status.lookup('PatronRequest', 'SOURCING_ITEM');
        req.save(flush:true, failOnError:true)


        if(req.rota.size() != 0) {
          log.debug("Found a potential supplier for ${req}");
          log.debug(" -> Request is currently SOURCING_ITEM - transition to SUPPLIER_IDENTIFIED");
          req.state = Status.lookup('PatronRequest', 'SUPPLIER_IDENTIFIED');
          req.save(flush:true, failOnError:true)
        } else {
          // NO rota supplied - see if we can use the shared index service to locate appropriate copies
          // N.B. grails-app/conf/spring/resources.groovy causes a different implementation to be injected
          // here in the test environments.
          SharedIndexAvailability sia = sharedIndexService.findAppropriateCopies([title:req.title])
          log.debug("Result of shared index lookup : ${sia}");
          log.error("Unable to identify a rota for ID ${eventData.payload.id}")
        }
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }


  // This takes a request with the state of SUPPLIER_IDENTIFIED and changes the state to REQUEST_SENT_TO_SUPPLIER
  public void sendToNextLender(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::sendToNextLender(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      req.lock()
      if ( ( req != null ) && ( req.state?.code == 'SUPPLIER_IDENTIFIED' ) ) {
        log.debug("Got request ${req}");
        
        //TODO - sendRequest called here, make it do stuff - A request to send a protocol level resource sharing request message
        Map request_message_request = [
          messageType:'REQUEST',
          request: [
            title: req.title
          ]
        ]

        if ( req.rota.size() > 0 ) {
          if ( req.rotaPosition?:-1 < req.rota.size() ) {
            // We have rota entries left, work out the next one
            req.rotaPosition = (req.rotaPosition!=null ? req.rotaPosition+1 : 0 )

            // get the responder
            PatronRequestRota prr = req.rota.find( { it.rotaPosition == req.rotaPosition } )
            if ( prr != null ) {
              String next_responder = prr.directoryId
              // send the message
              protocolMessageService.sendProtocolMessage(next_responder, request_message_request)
            }
            else {
              log.error("Unable to find rota entry at position ${req.rotaPosition}");
            }

          }
          else {
            log.debug("Reached end of rota (${req.rotaPosition}/${req.rota.size()})");
          }
        }
        else {
          log.warn("Annot send to next lender - rota is empty");
        }
        

        
        
        log.debug(" -> Request is currently SUPPLIER_IDENTIFIED - transition to REQUEST_SENT_TO_SUPPLIER");
        req.state = Status.lookup('PatronRequest', 'REQUEST_SENT_TO_SUPPLIER');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }


  // This takes a request with the state of REQUEST_SENT_TO_SUPPLIER and changes the state to ITEM_SHIPPED
  public void shipToLender(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::shipToLender(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      req.lock()
      if ( ( req != null ) && ( req.state?.code == 'REQUEST_SENT_TO_SUPPLIER' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently REQUEST_SENT_TO_SUPPLIER - transition to ITEM_SHIPPED");
        req.state = Status.lookup('PatronRequest', 'ITEM_SHIPPED');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }




  // This takes a request with the state of ITEM_SHIPPED and changes the state to BORROWING_LIBRARY_RECEIVED
  public void lenderReceived(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::lenderReceived(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      req.lock();
      if ( ( req != null ) && ( req.state?.code == 'ITEM_SHIPPED' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently ITEM_SHIPPED - transition to BORROWING_LIBRARY_RECEIVED");
        req.state = Status.lookup('PatronRequest', 'BORROWING_LIBRARY_RECEIVED');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }


  // This takes a request with the state of BORROWING_LIBRARY_RECEIVED and changes the state to AWAITING_RETURN_SHIPPING
  public void lenderFinishedWithItem(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::lenderFinishedWithItem(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      if ( ( req != null ) && ( req.state?.code == 'BORROWING_LIBRARY_RECEIVED' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently BORROWING_LIBRARY_RECEIVED - transition to AWAITING_RETURN_SHIPPING");
        req.state = Status.lookup('PatronRequest', 'AWAITING_RETURN_SHIPPING');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }

  
  // This takes a request with the state of AWAITING_RETURN_SHIPPING and changes the state to BORROWER_RETURNED
  public void lenderShippedReturn(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::lenderShippedReturn(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      if ( ( req != null ) && ( req.state?.code == 'AWAITING_RETURN_SHIPPING' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently BORROWING_LIBRARY_RECEIVED - transition to BORROWER_RETURNED");
        req.state = Status.lookup('PatronRequest', 'BORROWER_RETURNED');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
        log.debug("The current request IDs are")
        PatronRequest.list().each {
          log.debug("  -> ${it.id} ${it.title}");
        }
      }
    }
    log.debug("==================================================")
  }
  
  // This takes a request with the state of BORROWER_RETURNED and changes the state to REQUEST_COMPLETE
  public void itemReturned(eventData) {
    log.debug("==================================================")
    log.debug("ReshareApplicationEventHandlerService::itemReturned(${eventData})");
    PatronRequest.withNewTransaction { transaction_status ->

      def c_res = PatronRequest.executeQuery('select count(pr) from PatronRequest as pr')[0];
      log.debug("lookup ${eventData.payload.id} - currently ${c_res} patron requests in the system");

      def req = delayedGet(eventData.payload.id);
      if ( ( req != null ) && ( req.state?.code == 'BORROWER_RETURNED' ) ) {
        log.debug("Got request ${req}");
        log.debug(" -> Request is currently BORROWER_RETURNED - transition to REQUEST_COMPLETE");
        req.state = Status.lookup('PatronRequest', 'REQUEST_COMPLETE');
        req.save(flush:true, failOnError:true)
      }
      else {
        log.warn("Unable to locate request for ID ${eventData.payload.id} OR state != IDLE (${req?.state?.code})");
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
    log.debug("Create new request");
    PatronRequest pr = new PatronRequest()
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
          log.debug("Result found")
          result.refresh()
        }
      }
    }
    catch(Exception e){
      log.error("Problem", e)
    }
    return result;
  }
}
