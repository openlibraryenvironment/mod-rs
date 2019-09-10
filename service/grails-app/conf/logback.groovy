import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

logger ('grails.app.init', DEBUG)
logger ('grails.app.controllers', DEBUG)
logger ('grails.app.domains', DEBUG)
logger ('grails.app.jobs', DEBUG)
logger ('grails.app.services', DEBUG)
logger ('com.k_int', DEBUG)
logger ('okapi', INFO)
logger ('folio', DEBUG)
logger ('mod.rs', DEBUG)
logger ('org.olf', DEBUG)
logger ('org.olf.rs',DEBUG)
logger ('com.k_int.okapi.OkapiSchemaHandler', WARN)
logger ('com.k_int.okapi.OkapiClient', WARN)
logger ('org.olf.rs.workflow.ReShareMessageService', WARN)
logger ('com.k_int.okapi.springsecurity.OkapiAuthenticationFilter', WARN)
logger ('com.k_int.web.toolkit.refdata.GrailsDomainRefdataHelpers', WARN)
logger ('com.k_int.okapi.remote_resources.RemoteOkapiLinkListener', WARN)
logger ('com.k_int.okapi.OkapiSchemaHandler', WARN)
logger ('org.grails.datastore', WARN)


// Uncomment below logging for output of OKAPI client http.
//logger 'groovy.net.http.JavaHttpBuilder', DEBUG
//logger 'groovy.net.http.JavaHttpBuilder.content', DEBUG
//logger 'groovy.net.http.JavaHttpBuilder.headers', DEBUG

if (Environment.currentEnvironment == Environment.TEST) {
  logger 'groovy.net.http.JavaHttpBuilder', DEBUG
  logger 'groovy.net.http.JavaHttpBuilder.content', DEBUG
  logger 'groovy.net.http.JavaHttpBuilder.headers', DEBUG
}


def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}
root(WARN, ['STDOUT'])
