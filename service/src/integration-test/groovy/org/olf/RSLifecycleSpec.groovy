package org.olf

import grails.testing.mixin.integration.Integration
import grails.transaction.*
import static grails.web.http.HttpHeaders.*
import static org.springframework.http.HttpStatus.*
import spock.lang.*
import geb.spock.*
import groovy.util.logging.Slf4j
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.k_int.okapi.OkapiHeaders
import spock.lang.Shared
import grails.gorm.multitenancy.Tenants
import org.olf.okapi.modules.directory.DirectoryEntry
import com.k_int.web.toolkit.testing.HttpSpec


@Slf4j
@Integration
@Stepwise
class RSLifecycleSpec extends HttpSpec {

  def grailsApplication

  Closure authHeaders = {
    header OkapiHeaders.TOKEN, 'dummy'
    header OkapiHeaders.USER_ID, 'dummy'
    header OkapiHeaders.PERMISSIONS, '[ "directory.admin", "directory.user", "directory.own.read", "directory.any.read" ]'
  }

  def setupSpec() {
    httpClientConfig = {
      client.clientCustomizer { HttpURLConnection conn ->
        conn.connectTimeout = 5000
        conn.readTimeout = 20000
      }
    }
  }

  def setup() {
  }

  def cleanup() {
  }

  void "Attempt to delete any old tenants"(tenantid, name) {
    when:"We post a delete request"
      try {
        setHeaders(['X-Okapi-Tenant': tenantid])
        def resp = doDelete("${baseUrl}_/tenant".toString(),null)
      }
      catch ( Exception e ) {
        // If there is no TestTenantG we'll get an exception here, it's fine
      }

    then:"Any old tenant removed"
      1==1

    where:
      tenantid | name
      'RSInstOne' | 'RSInstOne'
      'RSInstTwo' | 'RSInstTwo'
      'RSInstThree' | 'RSInstThree'
  }

  // Set up a new tenant called RSTestTenantA
  void "Set up test tenants "(tenantid, name) {
    when:"We post a new tenant request to the OKAPI controller"

      log.debug("Post new tenant request for ${tenantid} to ${baseUrl}_/tenant");

      setHeaders(['X-Okapi-Tenant': tenantid])
      def resp = doPost("${baseUrl}_/tenant") {
        // header 'X-Okapi-Tenant', tenantid
        authHeaders.rehydrate(delegate, owner, thisObject)()
      }

    log.debug("Got response: ${resp}");
    then:"The response is correct"
      resp != null;

    where:
      tenantid | name
      'RSInstOne' | 'RSInstOne'
      'RSInstTwo' | 'RSInstTwo'
      'RSInstThree' | 'RSInstThree'
  }

}
