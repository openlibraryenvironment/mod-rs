// Use this file by starting up a groovy shell (groovysh) and then issuing :load ./modrsCli.groovy - the colon is important!

import groovy.grape.Grape

Grape.addResolver(name:'mvnRepository', root:'http://central.maven.org/maven2/')
Grape.addResolver(name:'kint', root:'http://nexus.k-int.com/content/repositories/releases')
Grape.grab(group:'org.slf4j', module:'slf4j-api', version:'1.7.25')
Grape.grab(group:'net.sf.opencsv', module:'opencsv', version:'2.3')
Grape.grab(group:'org.apache.httpcomponents', module:'httpclient', version:'4.5.10')
Grape.grab(group:'org.codehaus.groovy.modules.http-builder', module:'http-builder', version:'0.7.1')
Grape.grab(group:'org.apache.httpcomponents', module:'httpmime', version:'4.5.10')
Grape.grab(group:'org.slf4j', module:'slf4j-api', version:'1.7.6')
Grape.grab(group:'org.slf4j', module:'jcl-over-slf4j', version:'1.7.6')
Grape.grab(group:'net.sourceforge.nekohtml', module:'nekohtml', version:'1.9.22')
Grape.grab(group:'xerces', module:'xercesImpl', version:'2.11.0')
Grape.grab(group:'org.ini4j', module:'ini4j', version:'0.5.4')
println("Loaded");
