#! /bin/sh

AUTH_TOKEN=`./okapi-login`

PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
}
'
