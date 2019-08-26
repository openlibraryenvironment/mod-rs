package org.olf.rs;

import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.Properties
import static groovy.json.JsonOutput.*
import grails.events.annotation.Subscriber

public class EventConsumerService {

  @javax.annotation.PostConstruct
  public void init() {
    log.debug("Configuring event consumer service");
  }

  @javax.annotation.PreDestroy
  private void cleanUp() throws Exception {
    log.info("EventConsumerService::cleanUp");
  }

  @Subscriber('okapi:tenant_list_updated')
  public void onTenantListUpdated(updated_list_of_tenants) {
    log.debug("onTenantListUpdated(${updated_list_of_tenants})");
  }
}
