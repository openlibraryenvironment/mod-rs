@Grab('io.github.http-builder-ng:http-builder-ng-core:1.0.4')

import groovy.grape.Grape
import java.util.concurrent.ThreadLocalRandom;
import static groovyx.net.http.HttpBuilder.configure
import groovy.json.JsonOutput;

// getOkapiToken("http://localhost:9130", "diku_admin", "admin", "diku")
getOkapiToken("http://shared-index.reshare-dev.indexdata.com:9130", "diku_admin", "admin", "diku")


String getOkapiToken(String baseUrl, String user, String pass, String tenant) {
    String result = null;
    def postBody = [username: user, password: pass]
    println("getOkapiToken(${baseUrl},${postBody},..,${tenant})");
    try {
      def r1 = configure {
        request.headers['X-Okapi-Tenant'] = tenant
        request.headers['accept'] = 'application/json'
        request.contentType = 'application/json'
        request.uri = baseUrl+'/authn/login'
        request.uri.query = [expandPermissions:true,fullPermissions:true]
        request.body = postBody
      }.post() {
        response.success { resp ->
          if ( resp == null ) {
            println("Response null from http post");
          }
          else {
            println("Try to extract token - ${resp} ${resp?.headers}");
            def tok_header = resp.headers?.find { h-> h.key == 'x-okapi-token' }
            if ( tok_header ) {
              result = tok_header.value;
            }
            else {
              println("Unable to locate okapi token header amongst ${r1?.headers}");
            }
          }
        
        }
        response.failure { resp -> 
          println("RESP ERROR: ${resp.getStatusCode()}, ${resp.getMessage()}, ${resp.getHeaders()}")
        }
      }
    }
    catch ( Exception e ) {
        println("problem trying to obtain auth token for shared index ${e}");
      }

    println("Result of okapi login: ${result}");
    return result;
  }
