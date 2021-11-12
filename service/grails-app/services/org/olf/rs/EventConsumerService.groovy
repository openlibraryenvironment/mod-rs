package org.olf.rs;

import static grails.async.Promises.*
import static groovy.json.JsonOutput.*

import org.apache.kafka.clients.consumer.KafkaConsumer

import com.k_int.web.toolkit.async.WithPromises
import grails.async.Promise
import grails.core.GrailsApplication
import grails.events.EventPublisher
import grails.events.annotation.Subscriber
import groovy.json.JsonSlurper
import grails.web.databinding.DataBinder
import org.olf.okapi.modules.directory.DirectoryEntry;
import grails.gorm.multitenancy.Tenants
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition
import com.k_int.web.toolkit.custprops.types.CustomPropertyText




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

    Promise p = WithPromises.task {
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
              log.debug("Completed processing of rs entry event");
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

  // Perhaps we should have a TenantListService or similar?
  public Set getTenantList() {
    return tenant_list
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
                clearCustomProperties(de)
                log.debug("Update directory entry ${data.payload.slug} : ${data.payload}");
              }

              // Remove any custom properties from the payload - currently the custprops
              // processing is additive - which means we get lots of values. Need a longer term solition for this
              def custprops = data.payload.remove('customProperties');

              // Bind all the data execep the custprops
              bindData(de, data.payload);

              // Do special handling of the custprops
              data.payload.customProperties = custprops;
              bindCustomProperties(de, data.payload)
              expireRemovedSymbols(de, data.payload)

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

  private void clearCustomProperties(DirectoryEntry de) {
    
  }

  /**
   * The "We must have the same ID's on directory entries in all modules has bitten us again.
   * We need to do special work to bind custom properties in the payload to the directory entry.
   * Right now, restricting this to string based properties. New custom properties will be bound without
   * issues.
   */
  private void bindCustomProperties(DirectoryEntry de, Map payload) {
    log.debug("Iterate over custom properties sent in directory entry payload ${payload.customProperties}");

    cleanCustomProperties(de);

    payload?.customProperties?.each { k, v ->
      // de.custprops is an instance of com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
      // we need to see if we can find
      if ( ['local_institutionalPatronId',
            'policy.ill.loan_policy',
            'policy.ill.last_resort',
            'policy.ill.returns',
            'policy.ill.InstitutionalLoanToBorrowRatio'].contains(k) ) {
        log.debug("processing binding for ${k} -> ${v}");
        boolean first = true;

        // Root level custom properties object is a custom properties container
        // We iterate over each custom property to see if it's one we want to process

        boolean existing_custprop_updated = false;

        // replace any existing property
        de.customProperties?.value.each { cp ->
          if ( cp.definition.name == k ) {
            log.debug("updating customProperties.${k}.value to ${v} - custprop type is ${cp.definition.type?.toString()}");
            if ( v instanceof List ) {
              // cp.value = v.get(0);
              mergeCustpropWithList(cp, v)
            }
            else if ( v instanceof String ) {
              // cp.value = v
              mergeCustpropWithString(cp, v)
            }
            existing_custprop_updated = true
          }
        }

        // IF we didn't update an existing property, we need to add a new one
        if ( existing_custprop_updated == false ) {
          log.debug("Need to add new custom property: ${k} -> ${v}");
          CustomPropertyDefinition cpd = CustomPropertyDefinition.findByName(k);
          if ( cpd != null ) {
            if ( v instanceof String ) {
              CustomPropertyText cpt = new CustomPropertyText();
              cpt.definition=cpd;
              cpt.value=v;
              de.customProperties?.addToValue(cpt);
            }
            else if ( v instanceof List ) {
              if ( ( v.size() == 1 ) && ( v[0] != null ) ) {
                CustomPropertyText cpt = new CustomPropertyText();
                cpt.definition=cpd;
                cpt.value=v[0]
                de.customProperties?.addToValue(cpt);
              }
              else {
                log.warn("List props size > 1 are not supported at this time")
              }
            }
          }
          else {
            log.warn("No definition for custprop ${k}. Skipping");
          }
        }
      }
      else {
        log.debug("skip binding for ${k} -> ${v}");
      }
    }
  }

  private void mergeCustpropWithList(Object cp_value, List binding_value) {
    log.debug("mergeCustpropWithList(${cp_value?.class?.name}, ${binding_value})");
    // log.debug("  -> existing cp value is ${cp_value?.value} / ${cp_value?.class?.name}");
    if ( cp_value instanceof com.k_int.web.toolkit.custprops.types.CustomPropertyText ) {
      // We're only concerned with single values for now.
      cp_value.value = binding_value.get(0)?.toString();
    }
    else {
      log.warn("Re-binding lists is not currently supported");
    }
  }

  private void mergeCustpropWithString(Object cp_value, String binding_value) {
    log.debug("mergeCustpropWithString(${cp_value?.class?.name}, ${binding_value})");
    // log.debug("  -> existing cp value is ${cp_value?.value} / ${cp_value?.class?.name}");
    if ( cp_value instanceof com.k_int.web.toolkit.custprops.types.CustomPropertyText ) {
      cp_value.value = binding_value
    }
  }

  @Subscriber('okapi:tenant_load_reference')
  public void onTenantLoadReference(final String tenantId, 
                                    final String value, 
                                    final boolean existing_tenant, 
                                    final boolean upgrading, 
                                    final String toVersion, 
                                    final String fromVersion) {
    log.info("onTenantLoadReference(${tenantId},${value},${existing_tenant},${upgrading},${toVersion},${fromVersion})");
  }

  private void cleanCustomProperties(DirectoryEntry de) {

    // Fror each of these custprops - we should have a scalar, not a set
    ['local_institutionalPatronId',
     'policy.ill.loan_policy',
     'policy.ill.last_resort',
     'policy.ill.returns',
     'policy.ill.InstitutionalLoanToBorrowRatio'].each { k ->

      boolean first = true;
      boolean updated = false;

      List cps_to_remove = []

      de.customProperties?.value.each { cp ->
        if ( cp.definition.name == k ) {
          if ( first ) {
            first=false;
          }
          else {
            // Extra value - dispose of it.
            cps_to_remove.add(cp);
            // de.customProperties?.removeFromValue(cp)
          }
        }
      }

      cps_to_remove.each { cp ->
        de.customProperties?.removeFromValue(cp)
        updated = true;
      }
    }
  }

  /**
   *  Sometimes a symbol can be removed in the record without the local cache copy of that relationship being removed. This function
   *  iterates over all symbols attached to a directory entry and flags up any which are not in the parsed json record. If the parsed
   *  JSON is authoritative, then we should remove the symbol or flag it in some way as being removed.
   *  Current implementation only detects, no action taken presently.
   */
  private void expireRemovedSymbols(DirectoryEntry de, Map payload) {
    log.debug("expireRemovedSymbols....");
    // In the payload root.symbols is an array of maps where each map contains the keys  authority:'ISIL', symbol:'RST1', priority:'a'
    // de.symbols is a List<Symbol>
    try {
      List symbols_to_remove = []

      de.symbols.each { dbsymbol ->
        log.debug("Verify symbol ${dbsymbol}");
        // Look in payload.symbols for a map entry where dbsymbol.symbol == entry.symbol and dbsymbol.authority.symbol == entry.authority
        def located_map_entry = payload.symbols.find { ( ( it.symbol == dbsymbol.symbol ) && ( it.authority == dbsymbol.authority.symbol ) ) }
        if ( located_map_entry ) {
          // DB symbol still present in data, no action needed
        }
        else {
          log.warn("Residual symbol still in db : ${dbsymbol} - should be removed")
          symbols_to_remove.add(dbsymbol);
        }
      }

      symbols_to_remove.each { symbol_to_remove ->
        try {
          log.debug("Remove ${symbol_to_remove}");
          symbol_to_remove.delete()
        }
        catch ( Exception e ) {
          log.error("problem deleting symbol",e);
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem detecting residual symbols",e);
    }
  }

}
