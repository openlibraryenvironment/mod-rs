package org.olf.rs;

import static grails.async.Promises.*
import static groovy.json.JsonOutput.*

import org.apache.kafka.clients.consumer.KafkaConsumer

import grails.async.Promise
import grails.core.GrailsApplication
import grails.events.EventPublisher
import grails.events.annotation.Subscriber
import groovy.json.JsonSlurper
import grails.web.databinding.DataBinder
import org.olf.okapi.modules.directory.DirectoryEntry;
import grails.gorm.multitenancy.Tenants


/**
 * Listen to configured KAFKA topics and react to them.
 * consume messages with topic TENANT_mod_rs_PatronRequestEvents for each TENANT activated - we do this as an explicit list rather
 * than as a regex subscription - so that we never consume a message for a tenant that we don't know about.
 * If the message parses, emit an asynchronous grails event.
 * This class is essentially the bridge between whatever event communication system we want to use and our internal method of
 * reacting to application events. Whatever implementation is used, it ultimately needs to call notify('PREventIndication',DATA) in order
 * for 
 */
public class EventConsumerService implements EventPublisher, DataBinder {

  GrailsApplication grailsApplication

  private KafkaConsumer consumer = null;
  private boolean running = true;
  private boolean tenant_list_updated = false;
  private Set tenant_list = null;

  @javax.annotation.PostConstruct
  public void init() {
    log.debug("Configuring event consumer service")
    Properties props = new Properties()
    try {
      grailsApplication.config.events.consumer.toProperties().each { final String key, final String value ->
        // Directly access each entry to cause lookup from env
        String prop = grailsApplication.config.getProperty("events.consumer.${key}")
        log.debug("Configuring event consumer service :: key:${key} value:${value} prop:${prop}");
        props.setProperty(key, prop)
      }
      log.debug("Configure consumer ${props}")
    }
    catch ( Exception e ) {
      log.error("Problem assembling props for consume",e);
    }

    consumer = new KafkaConsumer(props)

    /*
     * https://github.com/confluentinc/kafka-streams-examples/blob/5.4.1-post/src/main/java/io/confluent/examples/streams/WordCountLambdaExample.java
     * suggests this hook when using streams... We need to do something similar
     * Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
     */

    Promise p = task {
      consumePatronRequestEvents();
    }

    p.onError { Throwable err ->
      log.warn("Problem with consumer",e);
    }

    p.onComplete { result ->
      log.debug("Consumer exited cleanly");
    }

    log.debug("EventConsumerService::init() returning");
  }

  private void consumePatronRequestEvents() {

    try {
      while ( running ) {

        def topics = null;
        if ( ( tenant_list == null ) || ( tenant_list.size() == 0 ) )
          topics = ['dummy_topic']
        else
          topics = tenant_list.collect { "${it}_mod_rs_PatronRequestEvents".toString() } +
                   tenant_list.collect { "${it}_mod_directory_DirectoryEntryUpdate".toString() }

        log.debug("Listening out for topics : ${topics}");
        tenant_list_updated = false;
        consumer.subscribe(topics)
        while ( ( tenant_list_updated == false ) && ( running == true ) ) {
          def consumerRecords = consumer.poll(1000)
          consumerRecords.each{ record ->
            try {
              log.debug("KAFKA_EVENT:: topic: ${record.topic()} Key: ${record.key()}, Partition:${record.partition()}, Offset: ${record.offset()}, Value: ${record.value()}");

              if ( record.topic.contains('_mod_rs_PatronRequestEvents') ) {
                // Convert the JSON payload string to a map 
                def jsonSlurper = new JsonSlurper()
                def data = jsonSlurper.parseText(record.value)
                if ( data.event != null ) {
                  notify('PREventIndication', data)
                }
                else {
                  log.debug("No event specified in payoad: ${record.value}");
                }
              }
              else if ( record.topic.contains('_mod_directory_DirectoryEntryUpdate') ) {
                def jsonSlurper = new JsonSlurper()
                def data = jsonSlurper.parseText(record.value)
                notify('DirectoryUpdate', data)
              }
              else {
                log.debug("Not handling event for topic ${record.topic}");
              }
            }
            catch(Exception e) {
              log.error("problem processing event notification",e);
            }
            finally {
              log.debug("Completed processing of directory entry event");
            }
          }
          consumer.commitAsync();
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem in consumer",e);
    }
    finally {
      consumer.close()
    }
  }

  @javax.annotation.PreDestroy
  private void cleanUp() throws Exception {
    log.info("EventConsumerService::cleanUp");
    running = false;

    // @See https://stackoverflow.com/questions/46581674/how-to-finish-kafka-consumer-safetyis-there-meaning-to-call-threadjoin-inside
    consumer.wakeup();
  }

  @Subscriber('okapi:tenant_list_updated')
  public void onTenantListUpdated(event) {
    log.debug("onTenantListUpdated(${event}) data:${event.data} -- Class is ${event.class.name}");
    tenant_list = event.data
    tenant_list_updated = true;
  }

  // We don't want to be doing these updates on top of eachother
  @Subscriber('DirectoryUpdate')
  public synchronized  void processDirectoryUpdate(event) {
    log.debug("processDirectoryUpdate(${event})");

    def data = event.data;

    try {
      if ( data?.tenant ) {
        Tenants.withId(data.tenant+'_mod_rs') {
          DirectoryEntry.withTransaction { status ->
            log.debug("Process directory entry inside ${data.tenant}_mod_rs");
            if ( data.payload.slug ) {
              log.debug("Trying to load DirectoryEntry ${data.payload.slug}");
              DirectoryEntry de = DirectoryEntry.findBySlug(data.payload.slug)
              if ( de == null ) {
                log.debug("Create new directory entry ${data.payload.slug} : ${data.payload}");
                de = new DirectoryEntry();
                if ( data.payload.id ) {
                  de.id = data.payload.id;
                }
                else {
                  de.id = java.util.UUID.randomUUID().toString()
                }
              }
              else {
                de.lock()
                log.debug("Update directory entry ${data.payload.slug} : ${data.payload}");
              }
              bindData(de, data.payload);
              log.debug("Binding complete - ${de}");
              de.save(flush:true, failOnError:true);
            }
          }
        }

      }
    }
    catch ( Exception e ) {
      log.error("Problem processing directory update",e);
    }
    finally {
      log.debug("Directory update processing complete (${event})");
    }
  }
}
