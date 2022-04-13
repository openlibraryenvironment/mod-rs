package org.olf.rs;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.events.EventISO18626IncomingRequesterService;
import org.olf.rs.statemodel.events.EventISO18626IncomingResponderService;
import org.olf.rs.statemodel.events.EventMessageRequestIndService;
import org.olf.rs.statemodel.events.EventNoImplementationService;

import grails.events.annotation.Subscriber;
import grails.gorm.multitenancy.Tenants;
import grails.util.Holders
import groovy.json.JsonOutput;
import groovy.util.logging.Slf4j

/**
 * Handle application events.
 *
 * This class is all about high level reshare events - the kind of things users want to customise and modify. Application functionality
 * that might change between implementations can be added here.
 * REMEMBER: Notifications are not automatically within a tenant scope - handlers will need to resolve that.
 *
 * StateModel: https://app.diagrams.net/#G1fC5Xtj5fbk_Z7dIgjqgP3flBQfTSz-1s
 */

@Slf4j
public class ReshareApplicationEventHandlerService {

  private static final int MAX_RETRIES = 10;

  	EventNoImplementationService eventNoImplementationService;
    EventISO18626IncomingRequesterService eventISO18626IncomingRequesterService;
    EventISO18626IncomingResponderService eventISO18626IncomingResponderService;
    EventMessageRequestIndService eventMessageRequestIndService;

  	/** Holds map of the event to the bean that will do the processing for this event */
  	private static Map serviceEvents = [ : ];

  	@Subscriber('PREventIndication')
	public handleApplicationEvent(Map eventData) {
		log.debug("ReshareApplicationEventHandlerService::handleApplicationEvent(${eventData})");
		if (eventData?.event) {
			// Get hold of the bean and store it in our map, if we previously havn't been through here
			if (serviceEvents[eventData.event] == null) {
				// Determine the bean name, if we had a separate event table we could store it as a transient against that
				// We split the event name on the underscores then capitalize each word and then join it back together, prefixing it with "event" and postfixing it with "Service"
				String[] eventNameWords = eventData.event.toLowerCase().split("_");
				String eventNameNormalised = "";
				eventNameWords.each{ word ->
					eventNameNormalised += word.capitalize();
				}

				String beanName = "event" + eventNameNormalised + "Service";

				// Now setup the link to the service action that actually does the work
				try {
					serviceEvents[eventData.event] = Holders.grailsApplication.mainContext.getBean(beanName);
				} catch (Exception e) {
					log.error("Unable to locate event bean: " + beanName);
				}
			}

			// Did we find the bean
			AbstractEvent eventBean = serviceEvents[eventData.event];
			if (eventBean == null) {
				log.error("Unable to find the bean for event: " + eventData.event);

				// We shall use the NoImplementation bean for this event instead
				eventBean = eventNoImplementationService;
				serviceEvents[eventData.event] = eventBean;
			}

			try {
				// Ensure we are talking to the right tenant database
				Tenants.withId(eventData.tenant) {
					// If the event handler is doing its own transaction handler, then we just call it, we do not expect it to return us anything
					if (eventBean.fetchRequestMethod() == EventFetchRequestMethod.HANDLED_BY_EVENT_HANDLER) {
						// This typically happens when the method is called directly as a response is required directly
						// So it is really handling the mapping for the whole model, rather than individual events
						eventBean.processEvent(null, eventData, null);
					} else {
						// Start the transaction as the event is not handling it itself
						PatronRequest.withNewTransaction { transactionStatus ->
							PatronRequest request = null;
							String requestId = null;

							// Get hold of the request
							switch (eventBean.fetchRequestMethod()) {
								case EventFetchRequestMethod.NEW:
									request = new PatronRequest(eventData.bibliographicInfo);
									break;

								case EventFetchRequestMethod.PAYLOAD_ID:
									requestId = eventData.payload.id;
									request = delayedGet(requestId, true);
									break;
							}

							// Did we obtain a request
							if (request == null) {
								log.error("Within event \"" + eventData.event + "\", failed to obtain request with id: \"" + requestId + "\" using method Event " + eventBean.fetchRequestMethod().toString());
							} else {
								// Create ourselves a new result details
								EventResultDetails resultDetails = new EventResultDetails();
								Status currentState = request.state;

								// Default a few fields
								resultDetails.newStatus = currentState;
								resultDetails.result = ActionResult.SUCCESS;
								resultDetails.auditMessage = "Executing event: " + eventData.event;
								resultDetails.auditData = eventData;

								// Now do whatever work is required of this event
								resultDetails = eventBean.processEvent(request, eventData, resultDetails);

								// Do we want to save the request and create an audit entry
								if (resultDetails.saveData == true) {
									// Set the status of the request
									request.state = resultDetails.newStatus;

									// Adding an audit entry so we can see what states we are going to for the event
									// Do not commit this uncommented, here to aid seeing what transition changes we allow
//									auditEntry(request, currentState, request.state, "Event: " + eventData.event + ", State change: " + currentState.code + " -> "  + request.state.code, null);

									// Create the audit entry
									auditEntry(
										request,
										currentState,
										request.state,
										resultDetails.auditMessage,
										resultDetails.auditData);

									// Finally Save the request
									request.save(flush:true, failOnError:true);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Problem trying to invoke event handler for ${eventData.event}", e);
				throw e;
			}
		} else {
			log.error("No event specified in the event data: " + eventData.toString());
		}
	}

  /**
   * A new request has been received from an external PEER institution using some comms protocol.
   * We will need to create a request where isRequester==false
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleRequestMessage(Map eventData) {

    log.debug("ReshareApplicationEventHandlerService::handleRequestMessage(${eventData})");

	// Just call it directly
	EventResultDetails eventResultDetails = eventMessageRequestIndService.processEvent(null, eventData, new EventResultDetails());

    log.debug("ReshareApplicationEventHandlerService::handleRequestMessage returning");
    return eventResultDetails.responseResult;
  }

  /**
   * An incoming message to the requesting agency FROM the supplying agency - so we look in
   * eventData.header?.requestingAgencyRequestId to find our own ID for the request.
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleSupplyingAgencyMessage(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleSupplyingAgencyMessage(${eventData})");
	// Just call it directly
	EventResultDetails eventResultDetails = eventISO18626IncomingRequesterService.processEvent(null, eventData, new EventResultDetails());
    return eventResultDetails.responseResult;
  }


/**
   * An incoming message to the supplying agency from the requesting agency - so we look in
   * eventData.header?.supplyingAgencyRequestId to find our own ID for the request.
   * This should return everything that ISO18626Controller needs to build a confirmation message
   */
  def handleRequestingAgencyMessage(Map eventData) {
    log.debug("ReshareApplicationEventHandlerService::handleRequestingAgencyMessage(${eventData})")
	// Just call it directly
	EventResultDetails eventResultDetails = eventISO18626IncomingResponderService.processEvent(null, eventData, new EventResultDetails());
	return eventResultDetails.responseResult;
  }

  /**
   * Sometimes, we might receive a notification before the source transaction has committed. THats rubbish - so here we retry
   * up to 5 times.
   */
  public PatronRequest delayedGet(String pr_id, boolean wth_lock=false) {
    // log.debug("delayedGet called (${wth_lock})")
    PatronRequest result = null;
    int retries = 0;

    try {
      while ( ( result == null ) && (retries < MAX_RETRIES) ) {
        if ( wth_lock ) {
          log.debug("LOCKING: get PatronRequest[${pr_id}] - attempt lock");
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
          // log.debug("Result found for ${pr_id}. Refresh")
        }
      }
    }
    catch(Exception e){
      log.error("Problem", e)
    }
    finally {
      log.debug("LOCKING Delayed get PatronRequest[${pr_id}] returning ${result} - with_lock=${wth_lock}")
    }
    return result;
  }

// what calls this, as I don't think it gets called
//  private void error(PatronRequest pr, String message) {
//    Status old_state = pr.state;
//    Status new_state = pr.isRequester ? lookupStatus('PatronRequest', 'REQ_ERROR') : lookupStatus('Responder', 'RES_ERROR');
//    pr.state = new_state;
//    auditEntry(pr, old_state, new_state, message, null);
//  }

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

  /**
   * The auto responder has determined that a local copy is available. update the state of the request and
   * mark the pick location as the selected location.
   */
  public boolean routeRequestToLocation(PatronRequest pr, ItemLocation location) {
    log.debug("routeRequestToLocation(${pr},${location})");
    boolean result = false;

    // Only proceed if there is location
    if ( location && location.location ) {
      // We've been given a specific location, make sure we have a record for that location
      HostLMSLocation loc = HostLMSLocation.EnsureActive(location.location, location.location);

      pr.localCallNumber = location.callNumber
      pr.pickLocation = loc
      pr.pickShelvingLocation = location.shelvingLocation

      result = true;
    }
    else {
      log.debug("unable to reoute request as local responding location absent");
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

  public void incomingNotificationEntry(PatronRequest pr, Map eventData, Boolean isRequester, String note) {
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
      inboundMessage.setMessageContent(note)
    } else {
      inboundMessage.setMessageSender(resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
      inboundMessage.setAttachedAction(eventData.activeSection.action)
      inboundMessage.setMessageContent(note)
    }

    inboundMessage.setIsSender(false)

    log.debug("Inbound Message: ${inboundMessage.messageContent}")
    pr.addToNotifications(inboundMessage)
    //inboundMessage.save(flush:true, failOnError:true)
  }

  public void addLoanConditionToRequest(PatronRequest pr, String code, Symbol relevantSupplier, String note = null) {
	  def loanCondition = new PatronRequestLoanCondition();
	  loanCondition.setPatronRequest(pr);
	  loanCondition.setCode(code);
	  if (note != null) {
		  loanCondition.setNote(stripOutSystemCode(note));
	  }
	  loanCondition.setRelevantSupplier(relevantSupplier);

	  pr.addToConditions(loanCondition);
  }

  private String stripOutSystemCode(String string) {
	  String returnString = string;
	  def systemCodes = [
		  "#ReShareAddLoanCondition#",
		  "#ReShareLoanConditionAgreeResponse#",
		  "#ReShareSupplierConditionsAssumedAgreed#",
		  "#ReShareSupplierAwaitingConditionConfirmation#"
	  ];
	  systemCodes.each {code ->
		  if (string.contains(code)) {
			  returnString.replace(code, "");
		  }
	  }
	  return returnString;
  }

  public void markAllLoanConditionsAccepted(PatronRequest pr) {
    def conditions = PatronRequestLoanCondition.findAllByPatronRequest(pr)
    conditions.each {cond ->
      cond.setAccepted(true)
      cond.save(flush: true, failOnError: true)
    }
  }

  public String getRotaString( Set rota ) {
    def returnList = [];
    rota.each { entry ->
      returnList.add("directoryId: ${entry.directoryId} loadBalancingScore: ${entry.loadBalancingScore} rotaPosition: ${entry.rotaPosition}");
    }
    return returnList.join(",");
  }

	public Symbol resolveSymbol(String authorty, String symbol) {
		Symbol result = null;
	    List<Symbol> symbol_list = Symbol.executeQuery('select s from Symbol as s where s.authority.symbol = :authority and s.symbol = :symbol',
	                                                   [authority:authorty?.toUpperCase(), symbol:symbol?.toUpperCase()]);
	    if ( symbol_list.size() == 1 ) {
			result = symbol_list.get(0);
	    }
            else {
              log.warn("Missing or multiple symbol match for : ${authorty}:${symbol} (${symbol_list?.size()})");
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
                        else {
                          log.warn("unexpected number of name components attempting to parse ${combinedString}: ${name_components}");
                        }
		}
                else {
                        log.warn("resolveCombinedSymbol called with NULL string");
                }
		return result;
	}
}
