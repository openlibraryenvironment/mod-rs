package org.olf.rs;

import static groovy.json.JsonOutput.*

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

import grails.core.GrailsApplication


public class EventPublicationService {

  private KafkaProducer producer = null;
  GrailsApplication grailsApplication

  @javax.annotation.PostConstruct
  public void init() {
    log.debug("Configuring event publication service")
    
    Properties props = new Properties()
    props.put('zk.connect', grailsApplication.config.getProperty('events.consumer.zk.connect'));
    props.put('bootstrap.servers', grailsApplication.config.getProperty('events.consumer.bootstrap.servers'))
    props.put('key.serializer', grailsApplication.config.getProperty('events.consumer.key.serializer'));
    props.put('value.serializer', grailsApplication.config.getProperty('events.consumer.value.serializer'));
    log.debug("Configure consumer ${props}")

    producer = new KafkaProducer(props)
  }

  public void publishAsJSON(String topic, String key, Map data) {
    if ( key == null )
      key = new Random().nextLong()

    String compoundMessage = groovy.json.JsonOutput.toJson(data)

    log.debug("publishAsJSON(topic:${topic} key:${key}, compoundMessage: ${compoundMessage})");
    producer.send(
        new ProducerRecord<String, String>(topic, key, compoundMessage), { RecordMetadata metadata, Exception e ->
          // println "The offset of the record we just sent is: ${metadata?.offset()}"
        }
        )
    // log.debug("Send returned, callback will be called once complete");
  }

  @javax.annotation.PreDestroy
  private void cleanUp() throws Exception {
    log.info("EventPublicationService::cleanUp");
    if ( producer != null ) {
      producer.close();
      producer = null;
    }
  }
}
