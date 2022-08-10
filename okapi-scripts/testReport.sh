#!/bin/bash

# Set where this directory lives in relation to where it is being run from
SCRIPT_DIRECTORY=`dirname "$0"`
#echo Script directory: ${SCRIPT_DIRECTORY}

# setOkapiUrl sets the variable OKAPI_URL
. ${SCRIPT_DIRECTORY}/setOkapiUrl
#echo OKAPI URL: ${OKAPI_URL}

# Get hold of an auth token
AUTH_TOKEN=`${SCRIPT_DIRECTORY}/okapi-login`
#echo Auth Token: $AUTH_TOKEN

# Which tenant are we dealing with
TENANT="diku"

RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 300 -XGET "http://localhost:8081/rs/availableAction/testReport?id=c2bc1883-5d10-4fb3-ad84-5120f743ffca&id=7a42ed5a-9608-4bef-9ba2-3cc79a377d47&id=f5d803b3-9dcf-4451-a102-12849541df64")
echo Reequest Response: $RESPONSE

RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 300 -XGET "http://localhost:8081/rs/availableAction/testReport")
echo Reequest Response: $RESPONSE
