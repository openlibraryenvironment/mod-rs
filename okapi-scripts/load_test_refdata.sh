#! /bin/sh

AUTH_TOKEN=`./okapi-login`

echo Listing current requests
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 

# List current refdata
#
# curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/refdata
#
# Set up some reference data - in this case values, Yes, no, other.
#
# Commented out - for steve to try
YESNO_CAT=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/refdata -d ' {
  "desc":"Yes/No/Other",
  "values":[
    { "value":"Yes", "label":"Yes" },
    { "value":"No", "label":"No" },
    { "value":"Other", "label":"Other (see notes)" }
  ]
}'`
#
echo Result of create YesNo Cat: $YESNO_CAT
