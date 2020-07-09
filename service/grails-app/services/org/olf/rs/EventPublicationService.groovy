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
    grailsApplication.config.events.publisher.toProperties().each { final String key, final String value ->
      // Directly access each entry to cause lookup from env
      String prop = grailsApplication.config.getProperty("events.publisher.${key}")
      props.setProperty(key, prop)
    }
    producer = new KafkaProducer(props)
  }

  public void publishAsJSON(String topic, String key, Map data) {
    if ( key == null )
      key = new Random().nextLong()

    String compoundMessage = groovy.json.JsonOutput.toJson(data)

    log.debug("publishAsJSON(topic:${topic} key:${key}, event:${data?.event} tenant:${data?.tenant} oid:${data?.oid}  compoundMessage:...");

    producer.send(
        new ProducerRecord<String, String>(topic, key, compoundMessage), { RecordMetadata metadata, Exception e ->
          // println "The offset of the record we just sent is: ${metadata?.offset()}"
          if ( e != null ) {
            e.printStackTrace()
          }
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
