package org.olf.rs

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.SaveOrUpdateEvent
import org.olf.rs.statemodel.Events;
import org.olf.rs.statemodel.StateModel;
import org.springframework.context.ApplicationListener

import grails.gorm.multitenancy.Tenants


/**
 * This class listens for asynchronous domain class events and fires of any needed indications
 * This is the grails async framework in action - the notifications are in a separate thread to
 * the actual save or update of the domain class instance. Handlers should be short lived and if
 * work is needed, spawn a worker task.
 */
public class AppListenerService implements ApplicationListener {

  EventPublicationService eventPublicationService

  /**
   * It's not really enough to do this afterInsert - we actually want this event to fire after the transaction
   * has committed. Be aware that the event may arrive back before the transaction has committed.
   */
  void afterInsert(PostInsertEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      PatronRequest pr = (PatronRequest) event.entityObject;
      String tenant = Tenants.currentId(event.source);
      log.debug("afterInsert ${event} ${event?.entityObject?.class?.name} dirtyProps:${event?.entityObject?.dirtyPropertyNames}");

      String topic = "${tenant}_PatronRequestEvents".toString()
      log.debug("afterInsert ${event} ${event?.entityObject?.class?.name} (${pr.class.name}:${pr.id})");
      log.debug("Publish NewPatronRequest_ind event on topic ${topic}");
      String eventName = getPatronRequestEventName(pr?.stateModel?.shortcode, pr?.isRequester)
      log.debug("For statemodel ${pr.stateModel.shortcode} and isRequester ${pr.isRequester}, publishing event ${eventName}");
      eventPublicationService.publishAsJSON(
          topic,
          null, // key
          [
            event: eventName,
            tenant: tenant,
            oid: 'org.olf.rs.PatronRequest:'+pr.id,
            payload: [
              id: pr.id,
              title: pr.title
            ]
          ]
      );
      log.debug("AppListenerService::afterInsert event published");
    }
  }

  // https://www.codota.com/code/java/methods/org.hibernate.event.spi.PostUpdateEvent/getPersister
  void afterUpdate(PostUpdateEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      PatronRequest pr = (PatronRequest)event.entityObject;
      String tenant = Tenants.currentId(event.source);
      if ( pr.stateHasChanged==true && !pr.manuallyClosed) {
        log.debug("PatronRequest State has changed - issue an indication event so we can react accordingly");
        String topic = "${tenant}_PatronRequestEvents".toString()
        eventPublicationService.publishAsJSON(
          topic,
          null,             // key
          [
            event:Events.STATUS_EVENT_PREFIX + pr.state.code + Events.STATUS_EVENT_POSTFIX,
            tenant: tenant,
            oid:'org.olf.rs.PatronRequest:'+pr.id,
            payload:[
              id: pr.id,
              title: pr.title,
              state: pr.state.code,
              dueDate: pr.dueDateRS
            ]
          ]
        );
      }
      else {
        log.warn("PatronRequest ${pr?.id} updated but no state change detected")
      }
    }
  }

  void beforeInsert(PreInsertEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      log.debug("beforeInsert ${event} ${event?.entityObject?.class?.name}");
      // Stuff to do before insert of a patron request which need access
      // to the spring boot infrastructure
      // log.debug("beforeInsert of PatronRequest");
    }
  }

  void onSaveOrUpdate(SaveOrUpdateEvent event) {
    // log.debug("onSaveOrUpdate ${event} ${event?.entityObject?.class?.name}");
    // I don't think we need this method as afterUpdate is triggered
  }

  public void onApplicationEvent(org.springframework.context.ApplicationEvent event) {
    // log.debug("--> ${event?.class.name} ${event}");
    if ( event instanceof AbstractPersistenceEvent ) {
      if ( event instanceof PostUpdateEvent ) {
        afterUpdate(event);
      }
      else if ( event instanceof PreInsertEvent ) {
        beforeInsert(event);
      }
      else if ( event instanceof PostInsertEvent ) {
        afterInsert(event);
      }
      else if ( event instanceof SaveOrUpdateEvent ) {
        // On save the record will not have an ID, but it appears that a subsequent event gets triggered
        // once the id has been allocated
        onSaveOrUpdate(event);
      }
      else {
        // log.debug("No special handling for appliaction event of class ${event}");
      }
    }
    else {
      // log.debug("Event is not a persistence event: ${event}");
    }
  }

    private static String getPatronRequestEventName(String stateModelName, Boolean isRequester) {
        String eventName

        if (isRequester) {
            if (stateModelName == StateModel.MODEL_SLNP_REQUESTER ||
                    stateModelName == StateModel.MODEL_SLNP_NON_RETURNABLE_REQUESTER) {
                eventName = Events.EVENT_REQUESTER_NEW_SLNP_PATRON_REQUEST_INDICATION
            } else if (stateModelName == StateModel.MODEL_REQUESTER ||
                    stateModelName == StateModel.MODEL_DIGITAL_RETURNABLE_REQUESTER) {
                eventName = Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION
            } else if (stateModelName == StateModel.MODEL_NR_REQUESTER) {
                eventName = Events.EVENT_NONRETURNABLE_REQUESTER_NEW_PATRON_REQUEST_INDICATION
            } else {
                eventName = Events.EVENT_REQUESTER_NEW_PATRON_REQUEST_INDICATION
            }
        } else {
            if (stateModelName == StateModel.MODEL_SLNP_RESPONDER ||
                    stateModelName == StateModel.MODEL_SLNP_NON_RETURNABLE_RESPONDER) {
                eventName = Events.EVENT_RESPONDER_NEW_SLNP_PATRON_REQUEST_INDICATION
            } else if (stateModelName == StateModel.MODEL_RESPONDER ||
                    stateModelName == StateModel.MODEL_CDL_RESPONDER) {
                eventName = Events.EVENT_RESPONDER_NEW_PATRON_REQUEST_INDICATION
            } else if (stateModelName == StateModel.MODEL_NR_RESPONDER) {
                eventName = Events.EVENT_NONRETURNABLE_RESPONDER_NEW_PATRON_REQUEST_INDICATION
            } else {
                eventName = Events.EVENT_RESPONDER_NEW_PATRON_REQUEST_INDICATION
            }
        }
        return eventName
    }
}
