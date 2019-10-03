import groovyx.net.http.*
import static groovyx.net.http.ContentType.URLENC
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import groovyx.net.http.*
import org.apache.http.entity.mime.*
import org.apache.http.entity.mime.content.*
import org.apache.http.*
import org.apache.http.protocol.*
import java.nio.charset.Charset
import static groovy.json.JsonOutput.*
import groovy.util.slurpersupport.GPathResult
import org.apache.log4j.*
import com.k_int.goai.*;
import java.text.SimpleDateFormat
import au.com.bytecode.opencsv.CSVReader
import au.com.bytecode.opencsv.CSVWriter
import groovyx.net.http.*
import static groovyx.net.http.ContentType.XML
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import groovy.json.JsonOutput
import java.io.File;
import org.ini4j.*;


/**
 * An Okapi Client for use in the groovysh
 */
public class OkapiClient {

  private String config_id;
  private String url;
  private String tenant;
  private String password;
  private String username;

  private HTTPBuilder httpclient = null;

  private Map session_ctx = [:]


  private OkapiClient() {
  }

  public OkapiClient(String config) {
    Wini ini = new Wini(new File(System.getProperty("user.home")+'/.folio/credentials'));

    this.url = ini.get(config, 'url', String.class);
    this.tenant = ini.get(config, 'tenant', String.class);
    this.password = ini.get(config, 'password', String.class);
    this.username = ini.get(config, 'username', String.class);
  }


  public HTTPBuilder getClient() {
    if ( this.httpclient == null ) {
      println("Connecting to ${this.url}");
      this.httpclient = new HTTPBuilder(this.url);
    }

    return this.httpclient;
  }

  public boolean createTenant(String tenant) {
    println("createTenant...");
    return true;
  }

  def login() {

    def postBody = [username: this.username, password: this.password]
    println("attempt login ${postBody}");
  
    this.getClient().request( POST, JSON) { req ->
      uri.path= '/bl-users/login'
      uri.query=[expandPermissions:true,fullPermissions:true]
      headers.'X-Okapi-Tenant'=this.tenant;
      headers.'accept'='application/json'
      headers.'Content-Type'='application/json'
      body= postBody
      response.success = { resp, json ->
        println("Login completed");
        session_ctx.auth = json;
      }
      response.failure = { resp ->
        println("Error: ${resp.status}");
        System.exit(1);
      }
    }
  }

  def addTenantSymbol(String symbol) {
    def postBody=['symbol': symbol];
    this.getClient().request( POST, JSON) { req ->
      uri.path= '/rs/settings/tenantSymbols'
      uri.query=[expandPermissions:true,fullPermissions:true]
      headers.'X-Okapi-Tenant'=this.tenant;
      headers.'accept'='application/json'
      headers.'Content-Type'='application/json'
      body= postBody
      response.success = { resp, json ->
        println("Symbol Registered");
        session_ctx.auth = json;
      }
      response.failure = { resp ->
        println("Error: ${resp.status}");
        System.exit(1);
      }
    }
  }

  def listTenantSymbols() {
    this.getClient().request( GET, JSON) { req ->
      uri.path= '/rs/settings/tenantSymbols'
      uri.query=[expandPermissions:true,fullPermissions:true]
      headers.'X-Okapi-Tenant'=this.tenant;
      headers.'accept'='application/json'
      response.success = { resp, json ->
        println("Result ${json}");
      }
      response.failure = { resp ->
        println("Error: ${resp.status}");
      }
    }
  }

  def createRequest(Map citation) {

    if ( citation.containsKey('title') &&
         citation.containsKey('requestingInstitutionSymbol') ) {

      citation.isRequester=true;

      String postBody = JsonOutput.toJson(citation)

      this.getClient().request( POST, JSON) { req ->
        uri.path= '/rs/patronrequests'
        headers.'X-Okapi-Tenant'=this.tenant;
        headers.'accept'='application/json'
        headers.'Content-Type'='application/json'
        body=postBody
        response.success = { resp, json ->
          println("Symbol Registered");
          session_ctx.auth = json;
        }
        response.failure = { resp ->
          println("Error: ${resp.status}");
          System.exit(1);
        }
      }
    }
    else {
      println("Citation must at least contain a title and requestingInstitutionSymbol");
    }
  }

  def walkFoafGraph() {
    this.getClient().request( GET, JSON) { req ->
      uri.path='/directory/settings/foaf'
      headers.'X-Okapi-Tenant'=this.tenant;
      headers.'accept'='application/json'
      headers.'Content-Type'='application/json'
      response.success = { resp, json ->
        println("Foaf triggered");
      }
      response.failure = { resp ->
          println("Error: ${resp.status}");
      }
    }
  }
}
