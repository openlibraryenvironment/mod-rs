#!/usr/bin/env groovy

@Grapes([
  @GrabResolver(name='mvnRepository', root='http://central.maven.org/maven2/'),
  @GrabResolver(name='kint', root='http://nexus.k-int.com/content/repositories/releases'),
  @Grab(group='org.slf4j', module='slf4j-api', version='1.7.25'),
  @Grab(group='net.sf.opencsv', module='opencsv', version='2.3'),
  @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.1.2'),
  @Grab(group='org.apache.httpcomponents', module='httpclient', version='4.5.3'),
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1'),
  @Grab(group='org.apache.httpcomponents', module='httpmime', version='4.1.2'),
  @Grab(group='org.slf4j', module='slf4j-api', version='1.7.6'),
  @Grab(group='org.slf4j', module='jcl-over-slf4j', version='1.7.6'),
  @Grab(group='net.sourceforge.nekohtml', module='nekohtml', version='1.9.22'),
  @Grab(group='xerces', module='xercesImpl', version='2.11.0')
])

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


import java.text.*

def cli = new CliBuilder(usage: 'configure_fractallsp.groovy -h -u user -p pass -h host')
// Create the list of options.
cli.with {
  h longOpt: 'help', 'Show usage information'
  s longOpt: 'sender', args: 1, argName: 'sender', 'sender', required:true
  r longOpt: 'recipient', args: 1, argName: 'recipient', 'recipient', required:true
  t longOpt: 'title', args: 1, argName: 'title', 'title', required:true
}

def options = cli.parse(args)
if (!options) {
  println("No options");
  return
}
else {
  println(options)
}

// Show usage text when -h or --help option is used.
if (options.h) {
  cli.usage()
  return
}

http = new HTTPBuilder( 'http://localhost:9130' )

okapi_context=[:]

// Use the directory service to look up sender and recipient
login('diku_admin', 'admin');
println("Logged in, token: ${okapi_context.token}");

// Find the directory entries for the symbols
def sender_record = lookupEntry(options.s)[0];
def recipient_record = lookupEntry(options.r)[0];

println("ID Of sender in directory ${sender_record?.id}");
println("ID Of recipient in directory ${recipient_record?.id}");

// Find ILL service for recipient
def recipient_service = lookupService(options.r)[0];
def pref_auth = recipient_service?.customProperties?.ILLPreferredNamespaces[0].value;

println("Recipients preferred naming authority: ${pref_auth}");

// Find symbols for the sender and the recipient which are in the recipients preferred namespace
def sender_symbol_to_use = prefSymbol(sender_record?.id,pref_auth);
def recip_symbol_to_use = prefSymbol(recipient_record?.id,pref_auth);

println("Sender symbol to use: ${sender_symbol_to_use}");
println("Recipient symbol to use: ${recip_symbol_to_use}");

// curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/directory/serviceAccount?filters=accountHolder.symbols.symbol%3dBRYN&filters=accountHolder.symbols.authority.symbol%3dPALCI&filters=service.type.value%3diso10161&stats=true"

// curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/directory/api/findSymbol?for=$BRYN_ID&ns=OCLC"


private void login(String username, String password) {
  def postBody = [username: username, password: password]

  println("attempt login ${postBody}");

  http.request( POST, JSON) { req ->
    uri.path= '/authn/login'
    headers.'accept'='application/json'
    headers.'Content-Type'='application/json'
    headers.'X-Okapi-Tenant'='diku'
    body= postBody
    response.success = { resp, json ->
      // okapi_context.token=resp.getFirstHeader('x-okapi-token');
      resp.headers.each { h ->
        // println("\"${h.name}\" \"${h.value}\" ${h.name.equalsIgnoreCase( 'x-okapi-token' )}");
        if ( h.name.equalsIgnoreCase( 'x-okapi-token' ) ) {
          okapi_context.token = h.value
        }
      }
    }
    response.failure = { resp ->
      println("Error: ${resp.status}");
      System.exit(1);
    }
  }
}

def lookupEntry(String symbol) {
  Object result = null;
  // Split symbol into namespace:authority
  String[] symbol_components = symbol.split(':');

  println("directoryLookup(${symbol}) ${symbol_components}");
  http.request( GET, JSON) { req ->
    uri.path= '/directory/entry'
    uri.query = [ 
      'filters':["symbols.symbol=${symbol_components[1]}","symbols.authority.value=${symbol_components[0]}"]
    ]
    headers.'accept'='application/json'
    headers.'Content-Type'='application/json'
    headers.'X-Okapi-Tenant'='diku'
    headers.'X-Okapi-Token'=okapi_context.token
    response.success = { resp, json ->
      result = json
    }
    response.failure = { resp ->
      println("Error: ${resp.status}");
      System.exit(1);
    }
  }
  result
}

def lookupService(String symbol) {
  Object result = null;
  // Split symbol into namespace:authority
  String[] symbol_components = symbol.split(':');

  println("lookupService(${symbol}) ${symbol_components}");
  http.request( GET, JSON) { req ->
    uri.path= '/directory/serviceAccount'
    uri.query = [
      // Normally, we would filter by service.businessFunction.value=ill, but we want to force ISO10161.TCP here
      'filters':["accountHolder.symbols.symbol=${symbol_components[1]}",
                 "accountHolder.symbols.authority.symbol=${symbol_components[0]}",
                 'service.type.value=iso10161.tcp']
    ]
    headers.'accept'='application/json'
    headers.'Content-Type'='application/json'
    headers.'X-Okapi-Tenant'='diku'
    headers.'X-Okapi-Token'=okapi_context.token
    response.success = { resp, json ->
      result = json
    }
    response.failure = { resp ->
      println("Error: ${resp.status}");
      System.exit(1);
    }
  }
  result
}

def prefSymbol(id, auth) {
  Object result = null;

  println("prefSymbol(${id},${auth})");

  http.request( GET, JSON) { req ->
    uri.path= '/directory/api/findSymbol'
    uri.query = [
      'for':id,
      'ns':auth
    ]
    headers.'accept'='application/json'
    headers.'Content-Type'='application/json'
    headers.'X-Okapi-Tenant'='diku'
    headers.'X-Okapi-Token'=okapi_context.token
    response.success = { resp, json ->
      println("result ${json}");
      result = json
    }
    response.failure = { resp ->
      println("Error: ${resp.status}");
      System.exit(1);
    }
  }
  result
}
