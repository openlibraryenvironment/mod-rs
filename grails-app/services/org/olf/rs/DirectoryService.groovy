package org.olf.rs

import grails.gorm.multitenancy.Tenants
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import org.olf.rs.workflow.Action;
import javax.sql.DataSource
import groovy.sql.Sql
import grails.core.GrailsApplication
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import org.olf.rs.workflow.ReShareMessageService

import com.k_int.okapi.OkapiClient;
import groovyx.net.http.FromServer

/**
 * This service provides java/groovy interface to mod-directory affordances.
 */
@Transactional
public class DirectoryService {

  OkapiClient okapiClient;

  // Held and managed at the level of the service
  String system_agent_jwt = null;
  
  // grab and store a long lived JWT for doing system-to-system calls without a logged in user -
  // in this case doing a directory lookup
  private synchronized String getSystemJwt() {
    if ( system_agent_jwt == null ) {
      system_agent_jwt = getToken('SystemAgent','SystemAgent');
    }
    return system_agent_jwt;
  }

  String getIDForSymbol(String authority, String symbol) {
    // This helper needs to talk to mod-directory, and in particular, needs to return the results of
    // calling
    // /directory/entry?filters=symbols.symbol%3dDIKUA&filters=symbols.authority.value=RESHARE&stats=true
    // That call returns an array - if one row is returned, this method should return the object so that the ID can be extracted (Or
    // whatever other properties are needed). If multiple rows, an exception, if no rows, null.
    //
    // Asking steve for advice as there is no incoming OKAPI request object here so authentication and authorization are problems
    //
    return getDirectoryEntryForSymbol(authority,symbol)?.id;
  }

  Map getDirectoryEntryForSymbol(String authority, String symbol) {
    // This helper needs to talk to mod-directory, and in particular, needs to return the results of
    // calling
    // /directory/entry?filters=symbols.symbol%3dDIKUA&filters=symbols.authority.value=RESHARE&stats=true
    // That call returns an array - if one row is returned, this method should return the object so that the ID can be extracted (Or
    // whatever other properties are needed). If multiple rows, an exception, if no rows, null.
    //
    // Asking steve for advice as there is no incoming OKAPI request object here so authentication and authorization are problems
    //
   
    // Need help from @Sosguthorpe here - can't get this to work and not sure why
    // String jwt = getSystemJwt();
    String jwt = null;

    if ( jwt ) {
      log.error("Get directory entry");
    }
    else {
      log.warn("no JWT available for directory service call");
    }

    return [
      id:'1234'
    ]
  }

  private String getToken(String user, String pass) {

    String result = null;
    Map loginDetails = [ 'username':user, 'password':pass ]

    okapiClient.post('/authn/login', loginDetails, [:]) {
      // request.setHeaders(headers)
      response.success { FromServer fs ->
        okapiPresent = true
        
        fs.getHeaders().each { FromServer.Header h ->
          if (h.key.toLowerCase() == OkapiHeaders.TOKEN.toLowerCase()) {
            // headers.put(h.key, h.value)
            result = h.value;
          }
        }
      }

      response.failure { resp ->
        log.warn("Error response attempting to get login for ${user} : ${resp}");
      }
    }
    log.debug("Got token: ${result}");

    return result
  }

}
