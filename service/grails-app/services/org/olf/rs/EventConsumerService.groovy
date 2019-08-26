package org.olf.rs;

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.Properties
import static groovy.json.JsonOutput.*
import grails.events.annotation.Subscriber
import org.apache.kafka.clients.consumer.KafkaConsumer
import static grails.async.Promises.*
import grails.async.Promise


public class EventConsumerService {

  private KafkaConsumer consumer = null;
  private boolean running = true;
  private boolean tenant_list_updated = false;
  private Set tenant_list = null;

  @javax.annotation.PostConstruct
  public void init() {
    log.debug("Configuring event consumer service");
    Properties props = new Properties()
    props.put('zk.connect', 'localhost:2181')
    props.put('bootstrap.servers', 'localhost:9092') // ,<kafka-broker 2>:9092,<kafka-broker 3>:9092')
    props.put('key.deserializer', 'org.apache.kafka.common.serialization.StringDeserializer')
    props.put('value.deserializer', 'org.apache.kafka.common.serialization.StringDeserializer')
    props.put('group.id', 'ModRSConsumer')
    consumer = new KafkaConsumer(props)
    Promise p = task {
      consumePatronRequestEvents();
    }

    p.onError { Throwable err ->
      log.warn("Problem with consumer",e);
    }

    p.onComplete { result ->
      log.debug("Consumer exited cleanly");
    }
  }

  private void consumePatronRequestEvents() {

    try {
      while ( running ) {

        def topics = null;
        if ( ( tenant_list == null ) || ( tenant_list.size() == 0 ) ) 
          topics = [ 'dummy_topic' ]
        else
          topics = tenant_list.collect { "${it}_mod_rs_PatronRequestEvents".toString() }
       
        log.debug("Listening out for ${topics}");
        tenant_list_updated = false;
        consumer.subscribe(topics)
        while ( ( tenant_list_updated == false ) && ( running == true ) ) {
          log.debug("poll queue");
          def consumerRecords = consumer.poll(1000)
          consumerRecords.each{ record ->
            println "Key: ${record.key()}, Partition:${record.partition()}, Offset: ${record.offset()}, Value: ${record.value()}"
          }
          consumer.commitAsync();
        }
      }
    }
    catch ( Exception e ) {
      // log.error("Problem in consumer",e);
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
}
