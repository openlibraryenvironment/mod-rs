package org.olf.rs

import grails.gorm.multitenancy.Tenants;
import grails.util.Holders;
import groovy.util.logging.Slf4j;
import org.olf.rs.PatronRequest;
import org.grails.datastore.mapping.engine.event.PostDeleteEvent
import org.grails.datastore.mapping.engine.event.PostInsertEvent
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PostUpdateEvent
import org.grails.datastore.mapping.engine.event.SaveOrUpdateEvent

import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent

import javax.annotation.PostConstruct;
import groovy.transform.CompileStatic
import org.springframework.context.ApplicationListener
import org.springframework.context.ApplicationEvent

import org.grails.orm.hibernate.AbstractHibernateDatastore
import grails.gorm.transactions.Transactional

public class AppListenerService implements ApplicationListener {

  EventPublicationService eventPublicationService

  void afterInsert(PostInsertEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      PatronRequest pr = (PatronRequest) event.entityObject;
      log.debug("afterInsert ${event} ${event?.entityObject?.class?.name} (${pr.class.name}:${pr.id})");
      eventPublicationService.publishAsJSON(
        'PatronRequest',  // topic
        null,             // key
        [
          event:'NewPatronRequest_ind',
          oid:'org.olf.rs.PatronRequest:'+pr.id,
          payload:[
            id: pr.id,
            title: pr.title
          ]
        ]
      );
    }
  }

  void afterUpdate(PostUpdateEvent event) {
    if ( event.entityObject instanceof PatronRequest ) {
      log.debug("afterUpdate ${event} ${event?.entityObject?.class?.name}");
      // Stuff to do after update of a patron request which need access
      // to the spring boot infrastructure
      // PatronRequest pr = (PatronRequest) event.entityObject;
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

  public void onApplicationEvent(org.springframework.context.ApplicationEvent event){
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
}
