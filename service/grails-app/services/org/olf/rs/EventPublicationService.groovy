package org.olf.rs;

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.Properties
import static groovy.json.JsonOutput.*


public class EventPublicationService {

  private KafkaProducer producer = null;

  @javax.annotation.PostConstruct
  public void init() {
    log.debug("Configuring event publication service");
    Properties props = new Properties()
    props.put('zk.connect', 'localhost:2181')
    props.put('bootstrap.servers', 'localhost:9092') // ,<kafka-broker 2>:9092,<kafka-broker 3>:9092')
    props.put('key.serializer', 'org.apache.kafka.common.serialization.StringSerializer')
    props.put('value.serializer', 'org.apache.kafka.common.serialization.StringSerializer')
    producer = new KafkaProducer(props)
  }

  public void publishAsJSON(String topic, String key, Map data) {
    if ( key == null )
      key = new Random().nextLong()

    String compoundMessage = groovy.json.JsonOutput.toJson(data)

    log.debug("Send key:${key}, compoundMessage: ${compoundMessage}");
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
