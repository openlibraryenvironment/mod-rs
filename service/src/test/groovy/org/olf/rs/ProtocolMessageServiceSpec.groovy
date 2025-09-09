package org.olf.rs

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import org.olf.rs.statemodel.events.EventISO18626IncomingAbstractService
import org.olf.rs.logging.ProtocolAuditService
import org.olf.rs.logging.IIso18626LogDetails
import org.grails.databinding.xml.GPathResultMap
import spock.lang.Specification
import spock.lang.Shared
import com.github.tomakehurst.wiremock.WireMockServer
import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options

@Slf4j
class ProtocolMessageServiceSpec extends Specification implements ServiceUnitTest<ProtocolMessageService>, DataTest {

  @Shared WireMockServer wireMockServer

  def setupSpec() {
    mockDomain PatronRequestNotification
    mockDomain PatronRequest

    // Start WireMock server on a random port
    wireMockServer = new WireMockServer(options().dynamicPort())
    wireMockServer.start()
  }

  def cleanupSpec() {
    wireMockServer?.stop()
  }

  def cleanup() {
    wireMockServer?.resetAll()
  }

  // disable multitenancy as GORM's DataTest does not support it
  Closure doWithConfig() {
    { config ->
      config.grails.gorm.multiTenancy.mode = null
    }
  }

  def 'test sendISO18626Message with ERROR XML response using WireMock'() {
    setup:
      // Sample ERROR XML response
      String sampleErrorXml = '''<?xml version="1.0" encoding="utf-8"?>
<ISO18626Message xmlns="http://illtransactions.org/2013/iso18626" ill:version="1.2" xmlns:ill="http://illtransactions.org/2013/iso18626" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_2.xsd">
  <requestingAgencyMessageConfirmation>
    <confirmationHeader>
      <supplyingAgencyId>
        <agencyIdType></agencyIdType>
        <agencyIdValue>S1</agencyIdValue>
      </supplyingAgencyId>
      <requestingAgencyId>
        <agencyIdType></agencyIdType>
        <agencyIdValue>R1</agencyIdValue>
      </requestingAgencyId>
      <timestamp>2025-08-25T17:12:39.840Z</timestamp>
      <timestampReceived>0001-01-01T00:00:00.000Z</timestampReceived>
      <messageStatus>ERROR</messageStatus>
    </confirmationHeader>
    <action></action>
    <errorData>
      <errorType>UnrecognisedDataValue</errorType>
      <errorValue>RequestingAgencyRequestId cannot be empty</errorValue>
    </errorData>
  </requestingAgencyMessageConfirmation>
</ISO18626Message>'''

      // Configure WireMock to return the ERROR XML response
      wireMockServer.stubFor(post(urlPathEqualTo("/api"))
        .willReturn(aResponse()
          .withStatus(200)
          .withHeader("Content-Type", "application/xml; charset=utf-8")
          .withBody(sampleErrorXml)))

      // Mock service dependencies
      def mockLogDetails = Mock(IIso18626LogDetails)
      mockLogDetails.request(_, _) >> {}
      mockLogDetails.response(_, _) >> {}

      service.settingsService = Mock(SettingsService)
      service.settingsService.getSettingAsInt(_, _, _) >> 30

      service.iso18626MessageValidationService = Mock(Iso18626MessageValidationService)
      service.iso18626MessageValidationService.validateAgainstXSD(_) >> {}

      Map eventData = [
        messageType: 'REQUESTING_AGENCY_MESSAGE',
        note: 'Test message',
        header: [
          supplyingAgencyId: [
            agencyIdType: '',
            agencyIdValue: 'S1'
          ],
          requestingAgencyId: [
            agencyIdType: '',
            agencyIdValue: 'R1'
          ]
        ]
      ]

    when: 'sendISO18626Message makes HTTP request to WireMock server'
      String wireMockUrl = "http://localhost:${wireMockServer.port()}/api"
      Map auditMap = [messageType: 'REQUESTING_AGENCY_MESSAGE']
      Map result = service.sendISO18626Message(eventData, wireMockUrl, [:], auditMap, mockLogDetails)

    then: 'The service correctly processes the ERROR XML response'
      result.messageStatus == 'ERROR'
      result.errorData != null
      result.errorData.errorType == 'UnrecognisedDataValue'
      result.errorData.errorValue == 'RequestingAgencyRequestId cannot be empty'
      result.rawData != null
      result.rawData.contains('UnrecognisedDataValue')
      result.rawData.contains('RequestingAgencyRequestId cannot be empty')

      // Verify WireMock received the request
      wireMockServer.verify(1, postRequestedFor(urlPathEqualTo("/api"))
        .withHeader("Content-Type", containing("application/xml")))
  }


}