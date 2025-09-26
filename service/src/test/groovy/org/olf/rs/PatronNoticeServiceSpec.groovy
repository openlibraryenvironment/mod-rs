package org.olf.rs

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.DirectoryEntry
import org.olf.okapi.modules.directory.Symbol
import org.olf.rs.referenceData.RefdataValueData
import org.olf.rs.referenceData.SettingsData
import org.olf.templating.TemplatingService
import spock.lang.Specification

import com.k_int.web.toolkit.refdata.RefdataValue

@Slf4j
class PatronNoticeServiceSpec extends Specification implements ServiceUnitTest<PatronNoticeService>, DomainUnitTest<PatronRequest> {

    def settingsService = Mock(SettingsService)
    def newDirectoryService = Mock(NewDirectoryService)
    def emailService = Mock(EmailService)
    def templatingService = Mock(TemplatingService)

    // disable multitenancy as GORM's DomainUnitTest does not support it
    Closure doWithConfig() {
        { config ->
            config.grails.gorm.multiTenancy.mode = null
        }
    }

    def setup() {
        service.settingsService = settingsService
        service.newDirectoryService = newDirectoryService
        service.emailService = emailService
        service.templatingService = templatingService

        // Mock domain classes needed by the service
        mockDomain(PatronRequest)
        mockDomain(NoticeEvent)
        mockDomain(RefdataValue)
        mockDomain(com.k_int.web.toolkit.refdata.RefdataCategory)
        mockDomain(DirectoryEntry)
        mockDomain(Symbol)
    }


    def 'test triggerNotices with NOTICE_TRIGGER_NEW_SUPPLY_REQUEST uses admin email'() {
        given: 'A mocked patron request and supply trigger'
            def patronRequest = Mock(PatronRequest) {
                getHrid() >> 'TEST-001'
                getPatronEmail() >> 'patron@test.com'
                getPatronIdentifier() >> 'patron123'
                getPatronSurname() >> 'TestSurname'
            }

            def supplyTrigger = Mock(RefdataValue) {
                getValue() >> RefdataValueData.NOTICE_TRIGGER_NEW_SUPPLY_REQUEST
            }

        and: 'Routing is disabled and NewDirectoryService returns admin email'
            settingsService.getSettingValue('routing_adapter') >> 'disabled'
            settingsService.getSettingValue(SettingsData.SETTING_DEFAULT_REQUEST_SYMBOL) >> 'TEST:SYMBOL'

            def mockInstitutionEntry = [email: 'admin@institution.edu']
            newDirectoryService.institutionEntryBySymbol('TEST:SYMBOL') >> mockInstitutionEntry

        and: 'Mock the service to capture the final triggerNotices call'
            def capturedJsonData = null
            def capturedTrigger = null
            def capturedPatronRequest = null

            service.metaClass.triggerNotices = { String jsonData, RefdataValue trigger, PatronRequest pr ->
                capturedJsonData = jsonData
                capturedTrigger = trigger
                capturedPatronRequest = pr
                // Don't save to avoid GORM issues
            }

        when: 'triggerNotices is called with supply trigger'
            service.triggerNotices(patronRequest, supplyTrigger)

        then: 'The service uses admin email from NewDirectoryService'
            capturedJsonData != null
            capturedTrigger == supplyTrigger
            capturedPatronRequest == patronRequest

            def jsonData = new groovy.json.JsonSlurper().parseText(capturedJsonData)
            jsonData.email == 'admin@institution.edu'
            jsonData.user.id == 'patron123'
            jsonData.user.surname == 'TestSurname'
            jsonData.request.id == 'TEST-001'
    }

    def 'test triggerNotices with routing enabled uses managed directory entries for admin email'() {
        given: 'A mocked patron request and supply trigger'
            def patronRequest = Mock(PatronRequest) {
                getHrid() >> 'TEST-002'
                getPatronEmail() >> 'patron@test.com'
                getPatronIdentifier() >> 'patron456'
                getPatronSurname() >> 'TestUser'
            }

            def supplyTrigger = Mock(RefdataValue) {
                getValue() >> RefdataValueData.NOTICE_TRIGGER_NEW_SUPPLY_REQUEST
            }

        and: 'Routing is enabled and managed directory entries are configured'
            settingsService.getSettingValue('routing_adapter') >> 'enabled'
            settingsService.getSettingValue(SettingsData.SETTING_LOCAL_SYMBOLS) >> 'INST:SYMBOL'

            // Mock the managed directory path components
            def mockInstitutionType = Mock(RefdataValue) {
                getId() >> 123L
            }
            def mockDirectoryEntry = Mock(DirectoryEntry) {
                getType() >> mockInstitutionType
                getEmailAddress() >> 'admin@managed-institution.edu'
                getParent() >> null
            }
            def mockSymbol = Mock(Symbol) {
                getOwner() >> mockDirectoryEntry
            }

            // Mock DirectoryEntryService static method to return our test symbol
            DirectoryEntryService.metaClass.static.resolveSymbolsFromStringList = { String symbols ->
                return [mockSymbol]
            }

            // Mock RefdataValue.lookupOrCreate for institution type lookup
            RefdataValue.metaClass.static.lookupOrCreate = { String category, String value1, String value2 ->
                return mockInstitutionType
            }

        and: 'Mock the service to capture the final triggerNotices call'
            def capturedJsonData = null

            service.metaClass.triggerNotices = { String jsonData, RefdataValue trigger, PatronRequest pr ->
                capturedJsonData = jsonData
                // Don't save to avoid GORM issues
            }

        when: 'triggerNotices is called with supply trigger'
            service.triggerNotices(patronRequest, supplyTrigger)

        then: 'The service uses admin email from managed directory entries'
            capturedJsonData != null

            def jsonData = new groovy.json.JsonSlurper().parseText(capturedJsonData)
            jsonData.email == 'admin@managed-institution.edu'
            jsonData.user.id == 'patron456'
            jsonData.user.surname == 'TestUser'
            jsonData.request.id == 'TEST-002'
    }

}