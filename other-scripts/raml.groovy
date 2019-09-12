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
  @Grab(group='xerces', module='xercesImpl', version='2.11.0'),
  @Grab(group='org.ini4j', module='ini4j', version='0.5.4')
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
import groovy.json.JsonOutput

import java.io.File;
import org.ini4j.*;

Wini ini = new Wini(new File(System.getProperty("user.home")+'/.folio/credentials'));

String config_id = 'kidemo'
String url = ini.get(config_id, 'url', String.class);
String tenant = ini.get(config_id, 'tenant', String.class);
String password = ini.get(config_id, 'password', String.class);
String username = ini.get(config_id, 'username', String.class);

println("${tenant} ${username} ${password}");
def cli = new CliBuilder(usage: 'raml.groovy -h -c config')
// Create the list of options.
cli.with {
        h longOpt: 'help', 'Show usage information'
        c longOpt: 'config', args: 1, argName: 'config', 'Config Id', required:true
}

def options = cli.parse(args)
if (!options) {
  println("No options");
  return
}
else {
  println(options.config)
}

def ebsco_api = new HTTPBuilder('https://sandbox.ebsco.io');
