package org.olf.rs

import com.k_int.okapi.OkapiClient
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import groovy.json.JsonSlurper
import org.apache.kafka.clients.consumer.KafkaConsumer

import org.olf.rs.EmailService
import org.olf.templating.TemplatingService
import org.olf.rs.EventConsumerService
import org.olf.rs.EventPublicationService
import org.olf.rs.NoticePolicyNotice

public class PatronNoticeService {

  EmailService emailService
  TemplatingService templatingService
  EventConsumerService eventConsumerService
  EventPublicationService eventPublicationService
  OkapiClient okapiClient
  GrailsApplication grailsApplication

  Properties props = null

  @javax.annotation.PostConstruct
  public void init() {
    // We need another consumer because EventConsumerService consumes all topics
    // as they come in whereas the notice triggers need to queue until they can
    // be consumed under the aegis of a timer request
    log.debug("Configuring event consumer for patron notice service")
    props = new Properties()
    try {
      grailsApplication.config.events.consumer.toProperties().each { final String key, final String value ->
        // Directly access each entry to cause lookup from env
        String prop = grailsApplication.config.getProperty("events.consumer.${key}")
        log.debug("Configuring event consumer for patron notice service :: key:${key} value:${value} prop:${prop}");
        props.setProperty(key, prop)
      }
      log.debug("Configure consumer ${props}")
    }
    catch ( Exception e ) {
      log.error("Problem assembling props for consume",e);
    }

    log.debug("PatronNoticeService::init() returning");
  }

  @javax.annotation.PreDestroy
  private void cleanUp() throws Exception {
    log.info("PatronNoticeService::cleanUp");
    // consumer.wakeup();
  }

  public void triggerNotices(PatronRequest pr, String trigger) {
    if (!(pr?.patronEmail instanceof String)) {
      log.error("Notice triggered for request ${pr.hrid} with no email for patron");
      return;
    }
    log.debug("triggerNotices(${pr.patronEmail}, ${trigger})")
    String tenant = Tenants.currentId()
    eventPublicationService.publishAsJSON(
      "${tenant}_PatronNoticeEvents",
      null,
      [
        event:'TriggerNotices_ind',
        tenant: tenant,
        payload:[
          trigger: trigger,
          email: pr.patronEmail,
          values: [
            user: [
              id: pr.patronIdentifier,
              givenName: pr?.patronGivenName ?: '',
              surname: pr.patronSurname,
            ],
            request: [
              id: pr.hrid,
              pickupLocation: pr?.pickupLocation ?: '',
              neededBy: pr?.neededBy?.toString() ?: ''
            ],
            item: [
              barcode: pr?.selectedItemBarcode ?: '',
              title: pr.title,
              materialType: pr?.publicationType?.label ?: ''
            ]
          ]
        ]
      ]
    );
  }

  public void processQueue(String tenant) {
    log.debug("Processing patron notice queue for tenant ${tenant}");

    // KafkaConsumers cannot be shared - when we recieve multiple timers for different tenants this method
    // will be called causing us to try to reuse the same consumer for each tenant. Refactoring to
    // dynamically create the consumer here and then destroy it - KafkaConsumer is a heavy object and this is not
    // an ideal way of working - it's the only pragmatic way forward at the moment tho.
    KafkaConsumer consumer = new KafkaConsumer(props)

    consumer.subscribe(["${tenant}_PatronNoticeEvents".toString()]);
    // TODO pass a Duration object (long is deprecated) and determine a less-arbitrary amount of time
    def consumerRecords = consumer.poll(2000);
    // commit immediately though we've not processed the records as even successfully passing it to
    // mod-email does not guarantee that a notice has been delivered but leaving it on the queue
    // does incur some risk of sending multiple copies
    consumer.commitAsync();
    // relies on commitAsync reading subscriptions up front, perhaps best to
    // run unsubscribe afterwards by implementing OffsetCommitCallback (TODO)
    consumer.unsubscribe();
    consumerRecords.each{ record ->
      try {
        log.debug("KAFKA_EVENT:: topic: ${record.topic()} Key: ${record.key()}, Partition:${record.partition()}, Offset: ${record.offset()}, Value: ${record.value()}");
        def jsonSlurper = new JsonSlurper()
        def data = jsonSlurper.parseText(record.value())
        Tenants.withId(data.tenant) {
          def notices = NoticePolicyNotice.findAll { noticePolicy.active == true && trigger.value == data.payload.trigger }
          notices.each {
            log.debug("Generating patron notice corresponding to trigger ${data.payload.trigger} for policy ${it.noticePolicy.name}")
            def tmplResult = templatingService.performTemplate(it.template, data.payload.values, "en")
            Map emailParams = [
              notificationId: it.id,
              to: data.payload.email,
              header: tmplResult.result.header,
              body: tmplResult.result.body,
              outputFormat: "text/html"
            ]
            emailService.sendEmail(emailParams)
          }
        }
      }
      catch(Exception e) {
        log.error("Problem processing notice trigger for ${tenant}", e);
      }
      finally {
        log.debug("Completed processing of patron notice trigger for ${tenant}");
      }
    }
  }
}
