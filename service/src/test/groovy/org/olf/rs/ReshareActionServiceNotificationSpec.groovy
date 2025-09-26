package org.olf.rs

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService
import org.olf.rs.logging.ProtocolAuditService
import org.olf.rs.logging.IIso18626LogDetails
import spock.lang.Specification

@Slf4j
class ReshareActionServiceNotificationSpec extends Specification implements ServiceUnitTest<ReshareActionService>, DataTest {

  def setupSpec() {
    mockDomain PatronRequestNotification
  }

  // disable multitenancy as GORM's DataTest does not support it
  Closure doWithConfig() {
    { config ->
      config.grails.gorm.multiTenancy.mode = null
    }
  }


  def 'test sendProtocolMessage creates notification with ERROR messageStatus'() {
    setup:
      // Mock dependencies with explicit types
      def mockProtocolMessageService = Mock(ProtocolMessageService)
      def mockProtocolAuditService = Mock(ProtocolAuditService)
      service.protocolMessageService = mockProtocolMessageService
      service.protocolAuditService = mockProtocolAuditService

      def mockRequest = Mock(PatronRequest)
      mockRequest.id >> UUID.randomUUID().toString()
      mockRequest.hrid >> 'TEST-ERROR'
      mockRequest.notifications >> []

      // Just allow addToNotifications to be called - we'll verify with argument constraints
      mockRequest.addToNotifications(_) >> {}

      Map eventData = [
        messageType: 'REQUESTING_AGENCY_MESSAGE',
        note: 'Test message that will get ERROR response',
        action: 'StatusRequest'
      ]

      // Mock audit service calls with explicit interface type
      mockProtocolAuditService.getIso18626LogDetails() >> Mock(IIso18626LogDetails)
      mockProtocolAuditService.save(_, _) >> {}

      // Mock the ERROR response
      mockProtocolMessageService.sendProtocolMessage('ISIL:R1', 'ISIL:S1', eventData, _) >> [
        status: ProtocolResultStatus.Sent,
        response: [
          messageStatus: EventISO18626IncomingAbstractService.STATUS_ERROR,
          errorData: [
            errorType: 'UnrecognisedDataValue',
            errorValue: 'RequestingAgencyRequestId cannot be empty'
          ]
        ]
      ]

    when: 'sendProtocolMessage is called with ERROR response scenario'
      boolean result = service.sendProtocolMessage(mockRequest, 'ISIL:R1', 'ISIL:S1', eventData)

    then: 'The method succeeds and creates notification with correct properties'
      result == true
      1 * mockRequest.addToNotifications({ PatronRequestNotification notification ->
        notification.messageStatus == EventISO18626IncomingAbstractService.STATUS_ERROR &&
        notification.messageContent == 'Test message that will get ERROR response' &&
        notification.isSender == true &&
        notification.seen == false
      })
  }
}