package org.olf.rs;


import com.k_int.okapi.OkapiClient
import groovy.util.logging.Slf4j

import grails.core.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired

/**
 * A mock email service that allows the integration tests to complete without sending any actual emails
 *
 */
@Slf4j
public class MockEmailServiceImpl implements EmailService {

  @Autowired
  GrailsApplication grailsApplication

  static boolean running = false;
  OkapiClient okapiClient

  public Map sendEmail(Map email_params) {
    log.debug("MockEmailServiceImpl::sendNotification(${email_params}) - grailsApplication is ${grailsApplication}");
    return [ status: 'OK' ]
  }
}
