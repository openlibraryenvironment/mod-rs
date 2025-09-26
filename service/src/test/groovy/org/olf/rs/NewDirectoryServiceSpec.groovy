package org.olf.rs

import grails.testing.services.ServiceUnitTest
import groovy.json.JsonSlurper
import spock.lang.Specification

class NewDirectoryServiceSpec extends Specification implements ServiceUnitTest<NewDirectoryService> {
    def 'Test parsing of directory info'(String location, Integer resultSize) {
        setup: 'We parse a record'
            String jsonText = new File("src/test/resources/directoryinfo/melbourne.json").text;
            def json = new JsonSlurper().parseText(jsonText);
            NewDirectoryService.metaClass.static.entriesBySymbol = { String symbol -> { return json;}};
        when:
            def entry = service.branchEntryByNameAndParentSymbol(location, "ISIL:AU-MELBOURNE");
            Map address = service.shippingAddressMapForEntry(entry, location)
        then:
            assert(address?.size() == resultSize)

        where:
        location                                  | resultSize
        "Melbourne dev tenant / Branch Campus"    | null
        "Melbourne dev tenant / Melbourne desk A" | 6
    }
}
