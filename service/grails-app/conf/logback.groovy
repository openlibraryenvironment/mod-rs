import java.nio.charset.Charset;

import org.springframework.boot.logging.logback.ColorConverter;
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import grails.util.BuildSettings;
import grails.util.Environment;

import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import net.logstash.logback.composite.loggingevent.*;
import net.logstash.logback.composite.*;
import net.logstash.logback.stacktrace.ShortenedThrowableConverter;

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    if ( ( Environment.isDevelopmentMode() ) ||
         ( Environment.getCurrent().getName() == 'rancher-desktop' ) ||
         ( Environment.getCurrent() == Environment.TEST )) {
        // Standard logging to Standard Out
        encoder(PatternLayoutEncoder) {
            charset = Charset.forName('UTF-8')
    
            pattern =
                    '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                    '%clr(%5p) ' + // Log level
                    '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                    "%clr(%-30.30logger{29} %15(%replace([%X{tenant:-_NO_TENANT_}]){'\\[_NO_TENANT_\\]',''})){cyan} %clr(:){faint} " +
                    'MDC:[tenant=%X{tenant}, ' +
                    'action=%X{action}, ' +
                    'event=%X{event}, ' +
                    'timer=%X{timer}, ' +
                    'resource=%X{resource}, ' +
                    'id=%X{id}, ' +
                    'hrid=%X{hrid}, ' +
                    'json=%X{json}, ' +
                    'xml=%X{xml}, ' +
                    'requestAction=%X{requestAction}, ' +
                    'slug=%X{slug}, ' +
                    'term=%X{term}, ' +
                    'fieldsToMatch=%X{fieldsToMatch}, ' +
                    'filters=%X{filters}, ' +
                    'numberPerPage=%X{numberPerPage}, ' +
                    'offset=%X{offset}, ' +
                    'page=%X{page}, ' +
                    'sort=%X{sort}, ' +
                    'statisticsRequired=%X{statisticsRequired}, ' +
                    'jvmUptime=%X{jvmUptime}, ' +
                    'maximumResults=%X{maximumResults}, ' +
                    'memoryAllocated=%X{memoryAllocated}, ' +
                    'memoryFree=%X{memoryFree}, ' +
                    'memoryMax=%X{memoryMax}, ' +
                    'memoryTotalFree=%X{memoryTotalFree}, ' +
                    'startTime%X{startTime}, ' +
                    'duration=%X{duration}] ' +
                    '%m%n%wex' // Message
    
        }
    } else {
        // Outputs the logging in json format, for formatted logging that can be easily parsed
        encoder(LoggingEventCompositeJsonEncoder) {
            providers(LoggingEventJsonProviders) {
                timestamp(LoggingEventFormattedTimestampJsonProvider) {
                    fieldName = '@time'
                    timeZone = 'UTC'
                    pattern = 'yyyy-MM-dd HH:mm:ss.SSS'
                }
                logLevel(LogLevelJsonProvider)
                loggerName(LoggerNameJsonProvider) {
                    fieldName = 'logger'
                    shortenedLoggerNameLength = 35
                }
                message(MessageJsonProvider) {
                    fieldName = 'msg'
                }
                globalCustomFields(GlobalCustomFieldsJsonProvider) {
                    // customFields = "${toJson(pid:"${new ApplicationPid()}", app:"XYZ")}"
                }
                threadName(LoggingEventThreadNameJsonProvider) {
                    fieldName = 'thread'
                }
                mdc(MdcJsonProvider)
                arguments(ArgumentsJsonProvider)
                stackTrace(StackTraceJsonProvider) {
                    throwableConverter(ShortenedThrowableConverter) {
                        maxDepthPerThrowable = 20
                        maxLength = 8192
                        shortenedClassNameLength = 35
                        exclude = /sun\..*/
                        exclude = /java\..*/
                        exclude = /groovy\..*/
                        exclude = /com\.sun\..*/
                        rootCauseFirst = true
                    }
                }
            }
        }
    }
}

if (Environment.currentEnvironment == Environment.TEST) {
  // logger 'groovy.net.http.JavaHttpBuilder', DEBUG
  // logger 'groovy.net.http.JavaHttpBuilder.content', DEBUG
  // logger 'groovy.net.http.JavaHttpBuilder.headers', DEBUG
}


def targetDir = BuildSettings.TARGET_DIR

logger ('org.hibernate.orm.deprecation', ERROR)

if ( ( Environment.isDevelopmentMode() ) ||
     ( Environment.getCurrent().getName() == "rancher-desktop" ) ||
     ( Environment.getCurrent() == Environment.TEST )) {
  logger ('com.k_int', DEBUG)
  logger ('com.k_int.okapi.springsecurity.OkapiAuthenticationFilter', WARN)
  logger ('com.k_int.okapi', WARN)
  logger ('com.k_int.okapi.DataloadingService', DEBUG)
  logger ('com.k_int.okapi.OkapiTenantAdminService', WARN)
  logger ('com.k_int.okapi.TenantController', DEBUG)
  logger ('com.k_int.web.toolkit.refdata.GrailsDomainRefdataHelpers', WARN)
  logger ('folio', DEBUG)
  logger ('grails.app.init', DEBUG)
  logger ('grails.app.controllers', DEBUG)
  logger ('grails.app.domains', DEBUG)
  logger ('grails.app.jobs', DEBUG)
  logger ('grails.app.services', DEBUG)
  logger ('mod.rs', DEBUG)
  logger ('okapi', INFO)
  logger ('org.grails.datastore', WARN)
  logger ('org.olf', DEBUG)
  logger ('org.olf.rs.EventConsumerService', DEBUG)
  logger ('javax.persistence.criteria.CriteriaQuery', ERROR)
  logger ('org.olf.okapi.modules.directory.CustomBinders', WARN)
  logger ('com.zaxxer.hikari.HikariConfig', DEBUG)
  logger ('com.zaxxer.hikari.pool.HikariPool', INFO)
  logger ('liquibase', INFO)

  // Enable Hibernate SQL logging with param values
  //logger ('org.hibernate.type', TRACE)
  //logger ('org.hibernate.SQL', DEBUG)

  // Log HTTPBuilderNG traffic
  // logger 'org.apache.http', INFO
  // logger 'org.apache.http.headers', TRACE
  // logger 'org.apache.http.wire', TRACE

  // logger ('com.k_int.okapi.OkapiSchemaHandler', WARN)
  // logger ('com.k_int.okapi.OkapiClient', WARN)
  // logger ('com.k_int.okapi.remote_resources.RemoteOkapiLinkListener', WARN)

  // Debugging call to mod-email
  logger ('com.k_int.okapi.OkapiClient', TRACE)
  logger ('groovyx.net.http.HttpBuilder', TRACE)
  logger ('groovyx.net.http.HttpBuilder', TRACE)

  // logger ('com.k_int.okapi.OkapiClient', TRACE)
  // logger 'groovy.net.http.JavaHttpBuilder', DEBUG
  // logger 'groovy.net.http.JavaHttpBuilder.content', DEBUG
  // logger 'groovy.net.http.JavaHttpBuilder.headers', DEBUG

  // Uncomment below logging for output of OKAPI client http.
  //logger 'groovy.net.http.JavaHttpBuilder', DEBUG
  //logger 'groovy.net.http.JavaHttpBuilder.content', DEBUG
  //logger 'groovy.net.http.JavaHttpBuilder.headers', DEBUG
  logger 'org.olf.RSLifecycleSpec', DEBUG
}
else {
  logger ('com.zaxxer.hikari.HikariConfig', DEBUG)
  logger ('com.k_int', INFO)
  logger ('org.olf', INFO)
  logger ('mod.rs', INFO)
}

root(WARN, ['STDOUT'])
