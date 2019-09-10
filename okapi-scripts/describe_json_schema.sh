#! /bin/sh

AUTH_TOKEN=`./okapi-login`
# 
# echo Listing current requests
# curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 
curl -H "Content-Type: application/json" -X GET "http://localhost:8081/rs/kiwt/config/schema/PatronRequest"
