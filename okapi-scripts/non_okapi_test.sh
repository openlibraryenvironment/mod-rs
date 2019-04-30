#!/bin/bash

curl --header "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:8080/rs/patronrequests -X GET
curl --header "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:8080/rs/custprops -X GET
curl --header "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:8080/rs/refdata -X GET
curl --header "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:8080/chas/TestXML -X GET
