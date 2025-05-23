package org.olf.rs

import groovy.json.JsonBuilder
import org.apache.commons.lang3.ObjectUtils

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import org.olf.okapi.modules.directory.Symbol;
import org.olf.rs.iso18626.NoteSpecials;
import org.olf.rs.lms.ItemLocation;
import org.olf.rs.logging.ContextLogging;
import org.olf.rs.statemodel.AbstractEvent;
import org.olf.rs.statemodel.ActionEvent;
import org.olf.rs.statemodel.ActionResult;
import org.olf.rs.statemodel.EventFetchRequestMethod;
import org.olf.rs.statemodel.EventResultDetails;
import org.olf.rs.statemodel.NewStatusResult;
import org.olf.rs.statemodel.Status;
import org.olf.rs.statemodel.StatusService;
import org.olf.rs.statemodel.events.EventISO18626IncomingRequesterService;
import org.olf.rs.statemodel.events.EventISO18626IncomingResponderService;
import org.olf.rs.statemodel.events.EventMessageRequestIndService;
import org.olf.rs.statemodel.events.EventNoImplementationService;

import grails.events.annotation.Subscriber;
import grails.gorm.multitenancy.Tenants;
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

    static List<String> preserveFields = ['supplierUniqueRecordId','title','author','subtitle','seriesTitle','edition','titleOfComponent','authorOfComponent','volume','issue','pagesRequested','estimatedNoPages','sponsor','informationSource']

    EventNoImplementationService eventNoImplementationService;
    EventISO18626IncomingRequesterService eventISO18626IncomingRequesterService;
    EventISO18626IncomingResponderService eventISO18626IncomingResponderService;
    EventMessageRequestIndService eventMessageRequestIndService;
    HostLMSLocationService hostLMSLocationService;
    HostLMSShelvingLocationService hostLMSShelvingLocationService;
    StatusService statusService;

      /**
       * Obtains the event processor for the supplied event name
       * @param eventName The name of the event that you want the processor for
       * @return The event processor for the supplied name
       */
      public AbstractEvent getEventProcessor(String eventName) {
          AbstractEvent eventprocessor = null;

          // For events, we have no way of knowing before hand whether they are for a requester or responder
          // So the service class will need to tak it into account
          def service = ActionEvent.lookupService(eventName, true, eventNoImplementationService);
          if (service instanceof AbstractEvent) {
              eventprocessor = (AbstractEvent)service;
          } else {
              log.error("Service for event is not of type AbstractEvent");
          }

          // return the processor to the caller
          return(eventprocessor);
      }

  	@Subscriber('PREventIndication')
	public handleApplicationEvent(Map eventData) {
		// Ignore anything without an event
		if (eventData?.event) {
            ContextLogging.startTime();
            ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_HANDLE_APPLICATION_EVENT);
            ContextLogging.setValue(ContextLogging.FIELD_EVENT, eventData.event);
            ContextLogging.setValue(ContextLogging.FIELD_JSON, eventData);
            ContextLogging.setValue(ContextLogging.FIELD_TENANT, eventData.tenant?.replace("_mod_rs", ""));
            ContextLogging.setValue(ContextLogging.FIELD_ID, eventData.payload?.id);
            log.debug(ContextLogging.MESSAGE_ENTERING);

			try {
				// Ensure we are talking to the right tenant database
				Tenants.withId(eventData.tenant) {
                    // Obtain the event processor, we do not know at this point whether we are dealing with a requester or responder event
                    // So the event service class needs to deal with whether they are a requester or responder
                    AbstractEvent eventBean = getEventProcessor(eventData.event);

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
                                    def newParams = eventData.bibliographicInfo.subMap(preserveFields)
                                    def customIdentifiersBody = [:]
                                    EventMessageRequestIndService.mapBibliographicRecordId(eventData, customIdentifiersBody, newParams)
                                    EventMessageRequestIndService.mapBibliographicItemId(eventData, newParams)

                                    if (ObjectUtils.isNotEmpty(customIdentifiersBody)) {
                                        request.customIdentifiers = new JsonBuilder(customIdentifiersBody).toPrettyString()
                                    }

									request = new PatronRequest(newParams)
									break;

								case EventFetchRequestMethod.PAYLOAD_ID:
									requestId = eventData.payload.id;
									request = delayedGet(requestId, true);

                                    // Add the hrid to the logging context
                                    ContextLogging.setValue(ContextLogging.FIELD_HRID, request?.hrid);
									break;
							}

							// Did we obtain a request
							if (request == null) {
								log.error("Within event \"" + eventData.event + "\", failed to obtain request with id: \"" + requestId + "\" using method Event " + eventBean.fetchRequestMethod().toString());
							} else {
                                // Lookup the ActionEvent record
                                ActionEvent actionEvent = ActionEvent.lookup(eventData.event);

								// Create ourselves a new result details
								EventResultDetails resultDetails = new EventResultDetails();
								Status currentState = request.state;

								// Default a few fields
								resultDetails.result = ActionResult.SUCCESS;
								resultDetails.auditMessage = "Executing event: " + eventData.event;
								resultDetails.auditData = eventData;

								// Now do whatever work is required of this event
								resultDetails = eventBean.processEvent(request, eventData, resultDetails);

								// Do we want to save the request and create an audit entry
								if (resultDetails.saveData == true) {
									// Set the status of the request
                                    NewStatusResult newResultStatus = statusService.lookupStatus(request, eventData.event, resultDetails.qualifier, resultDetails.result == ActionResult.SUCCESS, false);
                                    request.state = newResultStatus.status;

                                    // Do we need to update the rota location with the status
                                    if (newResultStatus.updateRotaLocation) {
                                        // we do
                                        request.updateRotaState(request.state);
                                    }

									// Adding an audit entry so we can see what states we are going to for the event
									// Do not commit this uncommented, here to aid seeing what transition changes we allow
//									auditEntry(request, currentState, request.state, "Event: " + eventData.event + ", State change: " + currentState.code + " -> "  + request.state.code, null);

									// Create the audit entry
									auditEntry(
										request,
										currentState,
										request.state,
										resultDetails.auditMessage,
										resultDetails.auditData,
                                        actionEvent,
                                        resultDetails.messageSequenceNo
                                    );

									// Finally Save the request
									request.save(flush:true, failOnError:true);

                                    // if the request id in the event was null then it is a new request so we now need to add the request id to the context logging
                                    if (requestId == null) {
                                        ContextLogging.setValue(ContextLogging.FIELD_ID, request.id);
                                    }
								}
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Problem trying to invoke event handler for ${eventData.event}", e);
				throw e;
			} finally {
                // Record how long it took
                ContextLogging.duration();
                log.debug(ContextLogging.MESSAGE_EXITING);

                // Clear the context, not sure if the thread is reused or not
                ContextLogging.clear();
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
        ContextLogging.startTime();
        ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_HANDLE_REQUEST_MESSAGE);
        ContextLogging.setValue(ContextLogging.FIELD_JSON, eventData);
        log.debug("${ContextLogging.MESSAGE_ENTERING} handleRequestMessage");

        // Just call event handler directly
        EventResultDetails eventResultDetails = eventMessageRequestIndService.processEvent(null, eventData, new EventResultDetails());

        // Record how long it took
        ContextLogging.duration();
        log.debug(ContextLogging.MESSAGE_EXITING);

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

    /**
     * Adds an audit record for the given request
     * @param request The request we want to add an audit event to
     * @param from The status we are moving from
     * @param to The status we are moving to
     * @param message A message for generating this audit record
     * @param data Any data that is applicable for this audit record
     * @param actionEvent The action or event that generated this audit record
     * @param messageSequenceNo If a a message was sent to the other side of the transaction this is thesequence number it was sent with
     */
    public void auditEntry(
        PatronRequest request,
        Status from,
        Status to,
        String message,
        Map data,
        ActionEvent actionEvent  = null,
        Integer messageSequenceNo = null
    ) {

        String json_data = ( data != null ) ? JsonOutput.toJson(data).toString() : null;
        LocalDateTime ts = LocalDateTime.now();
        log.debug("add audit entry at ${ts}, from ${from} to ${to}, message ${message}");

        try {
            request.addToAudit( new PatronRequestAudit(
                patronRequest: request,
                dateCreated:ts,
                fromStatus:from,
                toStatus:to,
                duration:null,
                message: message,
                auditData: json_data,
                auditNo: request.incrementLastAuditNo(),
                rotaPosition: request.rotaPosition,
                actionEvent: actionEvent,
                messageSequenceNo: messageSequenceNo
            ));
        } catch(Exception e) {
            log.error("Problem saving audit entry", e);
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
      HostLMSLocation loc = hostLMSLocationService.ensureActive(location.location, location.location);

      pr.localCallNumber = location.callNumber;
      pr.pickLocation = loc;
      pr.pickShelvingLocation = hostLMSShelvingLocationService.ensureExists(location.shelvingLocation, location.shelvingLocation);
      pr.selectedItemBarcode = location?.itemId;

      result = true;
    }
    else {
      log.debug("unable to reoute request as local responding location absent");
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

      inboundMessage.setMessageSender(DirectoryEntryService.resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(DirectoryEntryService.resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
      inboundMessage.setAttachedAction(eventData.messageInfo.reasonForMessage)
      inboundMessage.setMessageContent(note)
    } else {
      inboundMessage.setMessageSender(DirectoryEntryService.resolveSymbol(eventData.header.requestingAgencyId.agencyIdType, eventData.header.requestingAgencyId.agencyIdValue))
      inboundMessage.setMessageReceiver(DirectoryEntryService.resolveSymbol(eventData.header.supplyingAgencyId.agencyIdType, eventData.header.supplyingAgencyId.agencyIdValue))
      inboundMessage.setAttachedAction(eventData.action)
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
      loanCondition.setSupplyingInstitutionSymbol(pr.supplyingInstitutionSymbol)
	  pr.addToConditions(loanCondition);
  }

  private String stripOutSystemCode(String string) {
	  String returnString = string;

	  def systemCodes = [
		  NoteSpecials.ADD_LOAN_CONDITION,
		  NoteSpecials.AGREE_LOAN_CONDITION,
		  NoteSpecials.AWAITING_CONDITION_CONFIRMED,
		  NoteSpecials.CONDITIONS_ASSUMED_AGREED
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
}
