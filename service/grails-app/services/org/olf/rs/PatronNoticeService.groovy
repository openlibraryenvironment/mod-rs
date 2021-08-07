package org.olf.rs

import com.k_int.okapi.OkapiClient
import grails.gorm.multitenancy.Tenants
import com.k_int.web.toolkit.refdata.RefdataValue
import org.hibernate.Transaction
import org.hibernate.LockOptions
import javax.persistence.LockModeType

import org.olf.rs.EmailService
import org.olf.templating.TemplatingService
import org.olf.rs.NoticePolicyNotice

public class PatronNoticeService {

  EmailService emailService
  TemplatingService templatingService
  OkapiClient okapiClient

  public void triggerNotices(PatronRequest pr, RefdataValue trigger) {
    log.debug("triggerNotices(${pr.patronEmail}, ${trigger.value})")
    def ne = new NoticeEvent(patronRequest: pr, trigger: trigger)
    ne.save(flush:true, failOnError:true)
  }

  public void processQueue(String tenant) {
    log.debug("Processing patron notice queue for tenant ${tenant}")
    try {
      Tenants.withId(tenant) {
        NoticeEvent.withSession { sess ->
          Transaction tx = sess.beginTransaction()
          // Using SKIP_LOCKED we avoid selecting rows that other timers may be operating on
          sess.createQuery('from NoticeEvent where sent=false')
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setHint("javax.persistence.lock.timeout", LockOptions.SKIP_LOCKED)
            .list().each { ev ->
            def pr = ev.patronRequest
            Map values = [
              user: [
                id: pr.patronIdentifier,
                givenName: pr?.patronGivenName ?: '',
                surname: pr.patronSurname,
              ],
              request: [
                id: pr.hrid,
                pickupLocation: pr?.pickupLocation ?: '',
                neededBy: pr?.neededBy?.toString() ?: '',
                cancellationReason: pr?.cancellationReason?.label ?: ''
              ],
              item: [
                barcode: pr?.selectedItemBarcode ?: '',
                title: pr.title,
                materialType: pr?.publicationType?.label ?: ''
              ]
            ]
            // TODO: incorporate this into the above query
            def notices = NoticePolicyNotice.findAll { noticePolicy.active == true && trigger.id == ev.trigger.id }
            notices.each { notice ->
              log.debug("Generating patron notice corresponding to trigger ${ev.trigger.value} for policy ${notice.noticePolicy.name} and template ${notice.template.name}")
              try {
                def tmplResult = templatingService.performTemplate(notice.template, values, "en")
                Map emailParams = [
                  notificationId: notice.id,
                  to: pr.patronEmail,
                  header: tmplResult.result.header,
                  body: tmplResult.result.body,
                  outputFormat: "text/html"
                ]
                emailService.sendEmail(emailParams)
              }
              catch(Exception e) {
                log.error("Problem sending notice for ${tenant}", e);
              }
            }
            // "sent" in this case is more like processed -- not all events necessarily result in notices
            ev.sent = true
            ev.save()
          }
          tx.commit()
        }
        NoticeEvent.executeUpdate("delete NoticeEvent ne where ne.sent = true")
      }
    }
    catch(Exception e) {
      log.error("Problem processing notice triggers for ${tenant}", e);
    }
    finally {
      log.debug("Completed processing of patron notice triggers for ${tenant}");
    }
  }
}
