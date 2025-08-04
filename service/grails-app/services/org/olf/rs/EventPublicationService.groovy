package org.olf.rs;

import static groovy.json.JsonOutput.*

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata

import grails.events.EventPublisher
import grails.core.GrailsApplication


public class EventPublicationService implements EventPublisher {

  private KafkaProducer producer = null;
  String disableKafkaEnv = System.getenv('MOD_RS_DISABLE_KAFKA');
  private Boolean disableKafka = Boolean.parseBoolean(disableKafkaEnv);
  GrailsApplication grailsApplication

  @javax.annotation.PostConstruct
  public void init() {
    if (disableKafka) {
      log.debug("Kafka is disabled");
      return;
    };
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

    if ((disableKafka || Boolean.parseBoolean(System.getenv('MOD_RS_LOCAL_PR_EVENTS')))
        && topic.endsWith(EventConsumerService.TOPIC_PATRON_REQUESTS_SUFFIX)) {
      if ( data.event != null ) {
        notify('PREventIndication', data)
      }
      else {
        log.debug("No event specified in payoad: ${record.value()}")
      }
    } else {
      producer.send(
          new ProducerRecord<String, String>(topic, key, compoundMessage), { RecordMetadata metadata, Exception e ->
            // println "The offset of the record we just sent is: ${metadata?.offset()}"
            if ( e != null ) {
              println("Exception sending to kafka ${e}");
              e.printStackTrace()
            }
          }
      )
    }
    log.debug("publishAsJSON - producer.send completed");
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
