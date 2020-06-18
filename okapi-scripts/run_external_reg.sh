#!/bin/bash

# This script is for executing a double check that the endpoint calls that work directly
# also work via okapi.

# see https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html for info on overriding 
# spring boot app config on the command line

the_jar_file=`ls build/libs/mod-rs*.jar | tail -n 1`

echo Start mod-rs in external-register mode - Jar will be $the_jar_file

# curl --header "X-Okapi-Tenant: diku" http://localhost:9130/content -X GET

# THis DOES work as expected however - 
# removing --isoOverRide="http://localhost:8081/rs/iso18626"
java -jar $the_jar_file -Xmx1G --server.port=8081 --grails.server.host=10.0.2.2 --dataSource.username=folio_admin --dataSource.password=folio_admin --dataSource.url=jdbc:postgresql://localhost:54321/okapi_modules

