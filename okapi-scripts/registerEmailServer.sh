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

# The various parameters
EMAIL_SMTP_HOST="smtp.gmail.com"
EMAIL_SMTP_PORT="465"
EMAIL_USERNAME="<<USERNAME>>"
EMAIL_PASSWORD="<Enter PASSWORD HERE>"
EMAIL_START_TLS_OPTIONS="REQUIRED"
EMAIL_TRUST_ALL="true"
EMAIL_SMTP_SSL="true"
EMAIL_FROM="<<FROM_EMAIL_ADDRES_TO_USE>>"

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_SMTP_HOST", "description": "server smtp host", "default": true, "enabled": true, "value": "<<EMAIL_SMTP_HOST>>" }'
REQUEST=${REQUEST/"<<EMAIL_SMTP_HOST>>"/"${EMAIL_SMTP_HOST}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_SMTP_PORT", "description": "server smtp host", "default": true, "enabled": true, "value": "<<EMAIL_SMTP_PORT>>" }'
REQUEST=${REQUEST/"<<EMAIL_SMTP_PORT>>"/"${EMAIL_SMTP_PORT}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_USERNAME","description": "Username to connect to server", "default": true, "enabled": true, "value": "<<EMAIL_USERNAME>>" }'
REQUEST=${REQUEST/"<<EMAIL_USERNAME>>"/"${EMAIL_USERNAME}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_PASSWORD", "description": "Password to connect to server", "default": true, "enabled": true, "value": "<<EMAIL_PASSWORD>>" }'
REQUEST=${REQUEST/"<<EMAIL_PASSWORD>>"/"${EMAIL_PASSWORD}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_START_TLS_OPTIONS", "description": "Are TLS options required", "default": true, "enabled": true, "value": "<<EMAIL_START_TLS_OPTIONS>>" }'
REQUEST=${REQUEST/"<<EMAIL_START_TLS_OPTIONS>>"/"${EMAIL_START_TLS_OPTIONS}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_TRUST_ALL", "description": "Do we trust all certificates", "enabled": true, "value": "<<EMAIL_TRUST_ALL>>" }'
REQUEST=${REQUEST/"<<EMAIL_TRUST_ALL>>"/"${EMAIL_TRUST_ALL}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{"module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_SMTP_SSL", "description": "Is this an SSL connection", "default": true, "enabled": true, "value": "<<EMAIL_SMTP_SSL>>" }'
REQUEST=${REQUEST/"<<EMAIL_SMTP_SSL>>"/"${EMAIL_SMTP_SSL}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE

REQUEST='{ "module": "SMTP_SERVER", "configName": "smtp", "code": "EMAIL_FROM", "description": "Where are emails sent from", "default": true, "enabled": true, "value": "<<EMAIL_FROM>>" }'
REQUEST=${REQUEST/"<<EMAIL_FROM>>"/"${EMAIL_FROM}"}
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/configurations/entries" \
   -d "${REQUEST}")
echo Reequest Response: $RESPONSE
