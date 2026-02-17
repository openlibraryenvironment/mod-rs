package org.olf.rs

import com.k_int.web.toolkit.tags.Tag
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.Symbol

import static groovy.transform.TypeCheckingMode.SKIP;

import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.olf.okapi.modules.directory.DirectoryEntry;
import org.olf.rs.constants.Directory;
import org.olf.rs.logging.ContextLogging;
import org.olf.rs.utils.TopicList;

import com.k_int.okapi.OkapiTenantResolver;
import com.k_int.web.toolkit.async.WithPromises;
import com.k_int.web.toolkit.custprops.CustomPropertyDefinition;
import com.k_int.web.toolkit.custprops.types.CustomPropertyText;

import grails.async.Promise;
import grails.core.GrailsApplication;
import grails.events.EventPublisher;
import grails.events.annotation.Subscriber;
import grails.gorm.multitenancy.Tenants;
import grails.web.databinding.DataBinder;
import groovy.json.JsonSlurper;
import groovy.transform.CompileStatic;

/**
 * Listen to configured KAFKA topics and react to them.
 * consume messages with topic TENANT_mod_rs_PatronRequestEvents for each TENANT activated - we do this as an explicit list rather
 * than as a regex subscription - so that we never consume a message for a tenant that we don't know about.
 * If the message parses, emit an asynchronous grails event.
 * This class is essentially the bridge between whatever event communication system we want to use and our internal method of
 * reacting to application events. Whatever implementation is used, it ultimately needs to call notify('PREventIndication',DATA) in order
 * for
 */
@Slf4j
@CompileStatic
public class EventConsumerService implements EventPublisher, DataBinder {

  public static final String TOPIC_PATRON_REQUESTS_SUFFIX = '_mod_rs_PatronRequestEvents'
  private static final String TOPIC_DIRECTORY_ENTRY_UPDATE_SUFFIX = '_mod_directory_DirectoryEntryUpdate'

  private static final String[] TOPIC_SUFFIXES = [TOPIC_PATRON_REQUESTS_SUFFIX, TOPIC_DIRECTORY_ENTRY_UPDATE_SUFFIX] as String[]

  GrailsApplication grailsApplication

  private KafkaConsumer consumer = null

  private static volatile boolean running = true
  private static final TopicList topicList = new TopicList();

  private String version = 'development'

  @javax.annotation.PostConstruct
  public void init() {
    if (Boolean.parseBoolean(System.getenv('MOD_RS_DISABLE_KAFKA'))) {
      log.debug("Kafka is disabled");
      return;
    }
    log.debug("Configuring event consumer service")
    version = grailsApplication.metadata.applicationVersion ?: version
    final Properties props = new Properties()
    try {
      final String consumerConfigPrefix = 'events.consumer.'
      for (final String key : grailsApplication.config.toProperties()?.keySet()) {
        if (key.startsWith(consumerConfigPrefix)) {
          final String value = grailsApplication.config.getProperty("${key}")
          props.setProperty(key.substring(consumerConfigPrefix.length()), value)
          log.debug("Configuring event consumer service :: key:${key} value:${value} ")
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem assembling props for consume",e)
    }

    consumer = new KafkaConsumer(props)

    /*
     * https://github.com/confluentinc/kafka-streams-examples/blob/5.4.1-post/src/main/java/io/confluent/examples/streams/WordCountLambdaExample.java
     * suggests this hook when using streams... We need to do something similar
     * Runtime.getRuntime().addShutdownHook(new Thread(streams::close))
     */
    Promise p = WithPromises.task {
      consumePatronRequestEvents()
    }

    p.onError { Throwable err ->
      log.warn("Problem with consumer",err)
    }

    p.onComplete { result ->
      log.debug("Consumer exited cleanly")
    }

    log.debug("EventConsumerService::init() returning")
  }


  private Set<String> getTopicsForTenantSchema ( final String tenantSchema ) {
    final String tenantName = OkapiTenantResolver.schemaNameToTenantId(tenantSchema)
    TOPIC_SUFFIXES.collect { "${tenantName}${it}" as String } as Set
  }

  @Subscriber('okapi:tenant_datasource_added')
  public void addTenantSubscriptions( final String tenantSchema ) {
    topicList.addAll( getTopicsForTenantSchema( tenantSchema ) )
  }

  @Subscriber('okapi:tenant_datasource_removed')
  public void removeTenantSubscriptions( final String tenantSchema ) {
    topicList.removeAll( getTopicsForTenantSchema( tenantSchema ) )
  }

  protected void adjustSubscriptions() {
    topicList.process() { List<String> topics ->
      consumer.subscribe(topics);
      log.info("this instance of mod-rs (${version}) is now listening out for topics : ${topics}")
    }
  }

  private Map<String,?> parseMapFromRecord ( ConsumerRecord record ) {

    // Cast to typecheck the value() retrunn type.
    final ConsumerRecord<?, String> typedRecord = record as ConsumerRecord<?, String>
    new JsonSlurper().parseText(typedRecord.value()) as Map
  }

  private void consumePatronRequestEvents() {

    try {
      adjustSubscriptions()

      final Duration pollTimeout = Duration.ofSeconds(1)

      while ( running ) {

        if (!consumer.subscription().empty) {

          final Iterable<ConsumerRecord> consumerRecords = consumer.poll( pollTimeout )

          // Read each topic entry.
          consumerRecords.each { final ConsumerRecord record ->
            try {
              log.debug("KAFKA_EVENT:: topic: ${record.topic()} Key: ${record.key()}, Partition:${record.partition()}, Offset: ${record.offset()}, Value: ${record.value()}")


              switch ( record.topic() ) {

                case { String topic -> topic.endsWith(TOPIC_PATRON_REQUESTS_SUFFIX) }:
                  final Map<String,?> data = parseMapFromRecord(record)
                  if ( data.event != null ) {
                    notify('PREventIndication', data)
                  }
                  else {
                    log.debug("No event specified in payoad: ${record.value()}")
                  }
                  break

                case { String topic -> topic.endsWith(TOPIC_DIRECTORY_ENTRY_UPDATE_SUFFIX) }:
                  notify('DirectoryUpdate', parseMapFromRecord(record))
                  break

                default:
                  log.debug("Not handling event for topic ${record.topic()}")
              }
            }
            catch(Exception e) {
              log.error("problem processing event notification",e)
            }
            finally {
              log.debug("Completed processing of rs entry event")
            }
          }
          consumer.commitAsync()
        }

        // Check for updated topic list
        adjustSubscriptions()
      }
    }
    catch ( WakeupException we ) {
      // Kafka client equivalent of Interrupt for poll operation.
      // We should log the fact it was aborted as info and not an error.
      if (!running) {
        log.info ( 'Aborted current polling because of shutdown' )
      } else {
        log.error ( 'Poll operation was unexpectedly aborted' )
      }
    }
    catch ( Exception e ) {
      log.error("Problem in consumer",e)
    }
    finally {
      consumer.close()
    }
  }

  @javax.annotation.PreDestroy
  private void cleanUp() throws Exception {
    log.info("EventConsumerService::cleanUp")
    running = false

    // @See https://stackoverflow.com/questions/46581674/how-to-finish-kafka-consumer-safetyis-there-meaning-to-call-threadjoin-inside
    consumer.wakeup()
  }

//  @Subscriber('okapi:tenant_list_updated')
//  public void onTenantListUpdated(event) {
//    log.debug("onTenantListUpdated(${event}) data:${event.data} -- Class is ${event.class.name}")
//    tenant_list = event.data
//    tenant_list_updated = true
//  }

//  // Perhaps we should have a TenantListService or similar?
//  public Set getTenantList() {
//    return tenant_list
//  }

  // We don't want to be doing these updates on top of eachother
  @Subscriber('DirectoryUpdate')
  @CompileStatic(SKIP)
  public synchronized void processDirectoryUpdate(Map<String, ?> data) {
    ContextLogging.startTime();
    ContextLogging.setValue(ContextLogging.FIELD_ACTION, ContextLogging.ACTION_PROCESS_DIRECTORY_UPDATE);
    ContextLogging.setValue(ContextLogging.FIELD_JSON, data);
    ContextLogging.setValue(ContextLogging.FIELD_TENANT, data?.tenant);
    log.debug(ContextLogging.MESSAGE_ENTERING);

    try {
      if ( data?.tenant ) {

        final String tenantSchema = OkapiTenantResolver.getTenantSchemaName("${data.tenant}")

        Tenants.withId(tenantSchema) {
          final Map<String,?> payload = data.payload as Map
          log.debug("Payload for directory update is ${payload}")
          log.debug("Process directory entry inside ${data.tenant}_mod_rs")
          if ( payload.slug ) {
            ContextLogging.setValue(ContextLogging.FIELD_SLUG, payload.slug)
            ContextLogging.setValue(ContextLogging.FIELD_ID, payload.id)
            cleanData(payload)
            DirectoryEntry.withTransaction { status ->
              log.debug("Trying to load DirectoryEntry ${payload.id}")
              DirectoryEntry de = DirectoryEntry.findById(payload.id as String)
              if (de == null) {
                log.debug("Create new directory entry ${payload.slug} : ${data.payload}")
                de = new DirectoryEntry()
                if (payload.id) {
                  de.id = payload.id
                } else {
                  de.id = java.util.UUID.randomUUID().toString()
                }
              } else {
                de.lock()
                clearCustomProperties(de)
                log.debug("Update directory entry ${payload.slug} : ${payload}")
              }

              // Remove any custom properties from the payload - currently the custprops
              // processing is additive - which means we get lots of values. Need a longer term solition for this
              def custprops = payload.get('customProperties')
              payload.remove('customProperties')

              // Bind all the data execep the custprops
              log.debug("Binding data except custom props")
              bindData(de, payload)

              // Do special handling of the custprops
              payload.customProperties = custprops
              log.debug("Binding custom properties")
              bindCustomProperties(de, payload)
              expireRemovedSymbols(de, payload)
              expireRemovedServiceAccounts(de, payload)

              log.debug("Binding complete - ${de}")
              de.save(flush: true, failOnError: true)
            }
          }
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem processing directory update",e)
    }
    finally {
      log.debug("Directory update processing complete (${data})")
    }
    // Record how long it took
    ContextLogging.duration();
    log.debug(ContextLogging.MESSAGE_EXITING);

    // Clear the context, not sure if the thread is reused or not
    ContextLogging.clear();
  }

  @CompileStatic(SKIP)
  private void cleanData(Map<String,?> payload) {
    boolean anonymize = false
    DirectoryEntry.withTransaction { status ->
      try {
        if (payload.deleted) {
          DirectoryEntry de = DirectoryEntry.findById(payload.id as String)
          if (de == null) {
            de = DirectoryEntry.findBySlug(payload.slug as String)
          }
          if (de) {
            log.debug("Delete directory entry ${payload.slug}")
            de.delete(flush: true, failOnError: true)
          }
        } else {
          DirectoryEntry de = DirectoryEntry.findBySlug(payload.slug as String)
          if (de && de.id != payload.id) {
            log.debug("Delete directory entry ${payload.slug} because of conflict id")
            de.delete(flush: true, failOnError: true)
          }
        }
      } catch (Exception e) {
        log.info("Failed to delete directory entry so anonymize it", e)
        anonymize = true
      }
    }
    if (anonymize) {
      anonymizeEntry(payload)
    }
  }

  @CompileStatic(SKIP)
  private void anonymizeEntry(Map<String,?> payload){
    DirectoryEntry.withTransaction { status ->
      DirectoryEntry de = DirectoryEntry.findById(payload.id as String)
      if (de == null) {
        de = DirectoryEntry.findBySlug(payload.slug as String)
      }
      if (de) {
        Tag deleted = new Tag()
        deleted.value = "deleted"
        de.tags.add(deleted)
        String prefix = "DELETED-" + System.currentTimeSeconds() + "-"
        de.slug = prefix + de.slug
        de.name = prefix + de.name
        for (Symbol symbol : de.symbols) {
          symbol.symbol = prefix + symbol.symbol
        }
        de.save(flush: true, failOnError: true)
      }
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
  @CompileStatic(SKIP)
  private void bindCustomProperties(DirectoryEntry de, Map payload) {
    log.debug("Iterate over custom properties sent in directory entry payload ${payload.customProperties}")

    cleanAllCustomProperties(de, payload?.customProperties)

    payload?.customProperties?.each { k, v ->
      // de.custprops is an instance of com.k_int.web.toolkit.custprops.types.CustomPropertyContainer
      // we need to see if we can find
      if ( [Directory.KEY_LOCAL_INSTITUTION_PATRON_ID,
            Directory.KEY_ILL_POLICY_LOAN,
            Directory.KEY_ILL_POLICY_LAST_RESORT,
            Directory.KEY_ILL_POLICY_RETURNS,
            Directory.KEY_ILL_POLICY_BORROW_RATIO,
            Directory.KEY_FOLIO_LOCATION_FILTER].contains(k) ) {
        log.debug("processing binding for ${k} -> ${v}")
        boolean first = true

        // Root level custom properties object is a custom properties container
        // We iterate over each custom property to see if it's one we want to process

        boolean existing_custprop_updated = false

        // replace any existing property
        de.customProperties?.value.each { cp ->
          if ( cp.definition.name == k ) {
            log.debug("updating customProperties.${k}.value to ${v} - custprop type is ${cp.definition.type?.toString()}")
            if ( v instanceof List ) {
              // cp.value = v.get(0)
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
          log.debug("Need to add new custom property: ${k} -> ${v}")
          CustomPropertyDefinition cpd = CustomPropertyDefinition.findByName(k)
          if ( cpd != null ) {
            if ( v instanceof String ) {
              CustomPropertyText cpt = new CustomPropertyText()
              cpt.definition=cpd
              cpt.value=v
              de.customProperties?.addToValue(cpt)
            }
            else if ( v instanceof List ) {
              if ( ( v.size() == 1 ) && ( v[0] != null ) ) {
                CustomPropertyText cpt = new CustomPropertyText()
                cpt.definition=cpd
                cpt.value=v[0]
                de.customProperties?.addToValue(cpt)
              }
              else {
                log.warn("List props size > 1 are not supported at this time")
              }
            }
          }
          else {
            log.warn("No definition for custprop ${k}. Skipping")
          }
        }
      }
      else {
        log.debug("skip binding for ${k} -> ${v}")
      }
    }
  }

  private void mergeCustpropWithList(Object cp_value, List binding_value) {
    log.debug("mergeCustpropWithList(${cp_value?.class?.name}, ${binding_value})")
    // log.debug("  -> existing cp value is ${cp_value?.value} / ${cp_value?.class?.name}")
    if ( cp_value instanceof com.k_int.web.toolkit.custprops.types.CustomPropertyText ) {
      // We're only concerned with single values for now.
      cp_value.value = binding_value.get(0)?.toString()
    }
    else {
      log.warn("Re-binding lists is not currently supported")
    }
  }

  private void mergeCustpropWithString(Object cp_value, String binding_value) {
    log.debug("mergeCustpropWithString(${cp_value?.class?.name}, ${binding_value})")
    // log.debug("  -> existing cp value is ${cp_value?.value} / ${cp_value?.class?.name}")
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
    log.info("onTenantLoadReference(${tenantId},${value},${existing_tenant},${upgrading},${toVersion},${fromVersion})")
  }

  private void cleanAllCustomProperties(DirectoryEntry de, Map updatedCustomProperties) {

    boolean updated = false
    List props_seen = []
    List cps_to_remove = []

    de.customProperties?.value.each { cp ->
      if ( props_seen.contains(cp.definition.name) || !updatedCustomProperties?.containsKey(cp.definition.name)) {
        log.debug("Removing unwanted prop value for ${cp.definition.name}")
        cps_to_remove.add(cp)
      }
      else {
        log.debug("Add ${cp.definition.name} to list of props see - this is the first one so it survives")
        props_seen.add(cp.definition.name)
      }
    }

    cps_to_remove.each { cp ->
      de.customProperties?.removeFromValue(cp)
      updated = true
    }

  }


  /**
   *  Sometimes a symbol can be removed in the record without the local cache copy of that relationship being removed. This function
   *  iterates over all symbols attached to a directory entry and flags up any which are not in the parsed json record. If the parsed
   *  JSON is authoritative, then we should remove the symbol or flag it in some way as being removed.
   *  Current implementation only detects, no action taken presently.
   */
  private void expireRemovedSymbols(DirectoryEntry de, Map payload) {
    log.debug("expireRemovedSymbols....")
    // In the payload root.symbols is an array of maps where each map contains the keys  authority:'ISIL', symbol:'RST1', priority:'a'
    // de.symbols is a List<Symbol>
    try {
      List symbols_to_remove = []

      de.symbols.each { dbsymbol ->
        log.debug("Verify symbol ${dbsymbol} (${dbsymbol?.authority?.symbol}:${dbsymbol?.symbol})")
        // Look in payload.symbols for a map entry where dbsymbol.symbol == entry.symbol and dbsymbol.authority.symbol == entry.authority

        List<Map<String, ?>> payloadSymbols = payload.symbols as List<Map>

        def located_map_entry = payloadSymbols.find { ( ( it.symbol == dbsymbol.symbol ) && ( it.authority == dbsymbol.authority.symbol ) ) }
        if ( located_map_entry ) {
          // DB symbol still present in data, no action needed
        }
        else {
          log.warn("Residual symbol still in db : ${dbsymbol} but not in updated record from mod-directory - should be removed")
          symbols_to_remove.add(dbsymbol)
        }
      }

      symbols_to_remove.each { symbol_to_remove ->
        try {
          log.debug("Remove ${symbol_to_remove}")
          // symbol_to_remove.delete()
          de.removeFromSymbols(symbol_to_remove)
        }
        catch ( Exception e ) {
          log.error("problem deleting symbol",e)
        }
      }
    }
    catch ( Exception e ) {
      log.error("Problem detecting residual symbols",e)
    }
  }

  private void expireRemovedServiceAccounts(DirectoryEntry de, Map payload){
    List<Map<String, ?>> payloadServices = payload.services as List<Map<String, ?>>
    def slugList = payloadServices.collect { it.slug }
    def filteredServices = de.services?.findAll { it.slug !in slugList }
    filteredServices.forEach {it ->
      try {
        log.debug("Remove service ${it.slug}")
        de.removeFromServices(it)
      } catch ( Exception e ) {
        log.error("problem deleting service",e)
      }
    }
  }

}
