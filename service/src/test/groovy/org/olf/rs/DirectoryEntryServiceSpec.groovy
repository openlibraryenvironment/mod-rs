package org.olf.rs

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import groovy.util.logging.Slf4j
import org.olf.okapi.modules.directory.Symbol
import org.olf.okapi.modules.directory.NamingAuthority
import spock.lang.Specification

/**
 * A mock email service that allows the integration tests to complete without sending any actual emails
 *
 */
@Slf4j
class DirectoryEntryServiceSpec extends Specification implements ServiceUnitTest<DirectoryEntryService>, DomainUnitTest<Symbol> {

    // disable multitenancy as GORM's DomainUnitTest does not support it
    Closure doWithConfig() {
        { config ->
            config.grails.gorm.multiTenancy.mode = null
        }
    }

    def 'test symbol parsing (resolveCombinedSymbol)'() {
        setup:
            // executeQuery is used here and not supported but that's fine
            // we can stub it out as we're testing resolvedCombinedSymbol
            DirectoryEntryService.metaClass.static.resolveSymbol = { String a, String s -> {
                def auth = new NamingAuthority(symbol: a);
                return new Symbol(symbol: s, authority: auth);
            }};

        when: 'We process an example set of ratio data'
            def result = service.resolveCombinedSymbol(combined)

        then:
            log.info("got result: ${result}");

        expect:
            result?.authority?.symbol == authority
            result?.symbol == symbol

        where:
        combined | authority | symbol
        'SYM:BOL' | 'SYM' | 'BOL'
        'AUTH:COL:ON' | 'AUTH' | 'COL:ON'
        'NOPE' | null | null
    }
}
