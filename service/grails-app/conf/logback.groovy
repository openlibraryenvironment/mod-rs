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
                "%clr(%-30.30logger{29} %15(%replace([%X{tenant:-_NO_TENANT_}]){'\\[_NO_TENANT_\\]',''})){cyan} %clr(:){faint} " +
                '%m%n%wex' // Message

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
     ( Environment.getCurrent() == Environment.TEST ) ) {
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
  logger ('com.zaxxer.hikari.pool.HikariPool', DEBUG)


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
