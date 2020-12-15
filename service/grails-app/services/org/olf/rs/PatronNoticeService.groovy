package org.olf.rs

import com.k_int.okapi.OkapiClient
import grails.core.GrailsApplication
import grails.gorm.multitenancy.Tenants
import groovy.json.JsonSlurper
import org.apache.kafka.clients.consumer.KafkaConsumer

import org.olf.rs.EmailService
import org.olf.rs.EventConsumerService
import org.olf.rs.EventPublicationService
import org.olf.rs.NoticePolicyNotice

public class PatronNoticeService {

  EmailService emailService
  EventConsumerService eventConsumerService
  EventPublicationService eventPublicationService
  OkapiClient okapiClient
  GrailsApplication grailsApplication
  private KafkaConsumer consumer = null

  @javax.annotation.PostConstruct
  public void init() {
    // We need another consumer because EventConsumerService consumes all topics
    // as they come in whereas the notice triggers need to queue until they can
    // be consumed under the aegis of a timer request
    log.debug("Configuring event consumer for patron notice service")
    Properties props = new Properties()
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

    consumer = new KafkaConsumer(props)
    log.debug("PatronNoticeService::init() returning");
  }

  @javax.annotation.PreDestroy
  private void cleanUp() throws Exception {
    log.info("PatronNoticeService::cleanUp");
    consumer.wakeup();
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
              id: pr.patronReference,
              givenName: pr?.patronGivenName ?: '',
              surame: pr.patronSurname,
            ],
            request: [
              id: pr.hrid,
              pickupLocation: pr?.pickupLocation ?: '',
              neededBy: pr?.neededBy?.toString() ?: ''
            ],
            item: [
              title: pr.title
            ]
          ]
        ]
      ]
    );
  }

  public void processQueue() {
    log.debug("Processing patron notice queue")
    def tenant_list = eventConsumerService.getTenantList()
    if ( ( tenant_list == null ) || ( tenant_list.size() == 0 ) ) return
    def topics = tenant_list.collect { "${it}_mod_rs_PatronNoticeEvents".toString() }
    consumer.subscribe(topics)
    def consumerRecords = consumer.poll(0)
    consumerRecords.each{ record ->
      try {
        log.debug("KAFKA_EVENT:: topic: ${record.topic()} Key: ${record.key()}, Partition:${record.partition()}, Offset: ${record.offset()}, Value: ${record.value()}");
        def jsonSlurper = new JsonSlurper()
        def data = jsonSlurper.parseText(record.value())
        Tenants.withId(data.tenant) {
          def notices = NoticePolicyNotice.findAll { noticePolicy.active == true && trigger.value == data.payload.trigger }
          notices.each {
            Map tmplParams = [
              templateId: it.template,
              lang: "en",
              outputFormat: "text/html",
              context: data.payload.values
            ]
            log.debug("Generating patron notice corresponding to trigger ${data.payload.trigger} for policy ${it.noticePolicy.name}")
            def tmplResult = okapiClient.post("/template-request", tmplParams)
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
        log.error("Problem processing notice trigger", e);
      }
      finally {
        log.debug("Completed processing of patron notice trigger");
      }
    }
    consumer.commitAsync();
  }
}
