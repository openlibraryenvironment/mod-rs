package org.olf.rs;

import grails.gorm.multitenancy.Tenants
import org.olf.rs.HostLMSLocation
import org.olf.rs.PatronRequest
import org.olf.rs.statemodel.AvailableAction
import org.olf.okapi.modules.directory.Symbol;

import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import com.k_int.okapi.OkapiClient
import groovy.util.logging.Slf4j
import groovy.json.JsonSlurper

/**
 * A mock email service that allows the integration tests to complete without sending any actual emails
 *
 */
@Slf4j
public class MockEmailServiceImpl implements EmailService {

  def grailsApplication
  def reshareActionService
  static boolean running = false;
  OkapiClient okapiClient

  public Map sendEmail(Map header_information, Map eventInformation) {
    log.debug("MockEmailServiceImpl::sendNotification(${header_information},${eventInformation})");

    return [ status: 'OK' ]
  }
}
