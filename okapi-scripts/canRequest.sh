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

# The patron id
PATRON_ID="23033004447227"
#PATRON_ID="chas"

RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XGET "${OKAPI_URL}/rs/patron/${PATRON_ID}/canCreateRequest")
echo Request Response: $RESPONSE
