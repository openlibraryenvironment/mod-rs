import groovy.grape.Grape

Grape.addResolver(name:'mvnRepository', root:'http://central.maven.org/maven2/')
Grape.addResolver(name:'kint', root:'http://nexus.k-int.com/content/repositories/releases')
Grape.grab(group:'org.slf4j', module:'slf4j-api', version:'1.7.25')
Grape.grab(group:'io.github.http-builder-ng', module:'http-builder-ng-core', version:'1.0.3')
Grape.grab(group:'org.slf4j', module:'jcl-over-slf4j', version:'1.7.6')
println("Loaded");

import java.util.concurrent.ThreadLocalRandom;
import static groovyx.net.http.HttpBuilder.configure
import groovy.json.JsonOutput;

getOkapiToken("http://localhost:9130", "diku_admin", "admin", "diku")


String getOkapiToken(String baseUrl, String user, String pass, String tenant) {
    String result = null;
    def postBody = [username: user, password: pass]
    log.debug("getOkapiToken(${baseUrl},${postBody},..,${tenant})");
    try {
      def r1 = configure {
        request.headers['X-Okapi-Tenant'] = tenant
        request.headers['accept'] = 'application/json'
        request.contentType = 'application/json'
        request.uri = baseUrl+'/bl-users/login'
        request.uri.query = [expandPermissions:true,fullPermissions:true]
        request.body = postBody
      }.get() {
        response.success { resp ->
          if ( resp == null ) {
            log.error("Response null from http post");
          }
          else {
            log.debug("Try to extract token - ${resp} ${resp?.headers}");
            def tok_header = resp.headers?.find { h-> h.key == 'x-okapi-token' }
            if ( tok_header ) {
              result = tok_header.value;
            }
            else {
              log.warn("Unable to locate okapi token header amongst ${r1?.headers}");
            }
          }
        
        }
        response.failure { resp -> 
          log.error("RESP ERROR: ${resp.getStatusCode()}, ${resp.getMessage()}, ${resp.getHeaders()}")
        }
      }
    }
    catch ( Exception e ) {
        log.error("problem trying to obtain auth token for shared index",e);
      }

    log.debug("Result of okapi login: ${result}");
    return result;
  }