#!/bin/bash -e

TENANT=${TENANT:-'diku'}

# setOkapiUrl sets the variable OKAPI_URL
. ./setOkapiUrl
echo OKAPI Url: $OKAPI_URL

# Get hold of an auth token
AUTH_TOKEN=`./okapi-login`
echo Auth Token: $AUTH_TOKEN

# Check vars not blank
[[ ! -z "$OKAPI_URL" ]] || ( echo "OKAPI_URL is a required environment variable" && exit 1 )
[[ ! -z "$TENANT" ]] || ( echo "TENANT is a required environment variable" && exit 1 )
[[ ! -z "$AUTH_TOKEN" ]] || ( echo "AUTH_TOKEN is a required environment variable" && exit 1 )


# Grant all to found user.
ALL_PERMS=`curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
  -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 \
  -XGET "$OKAPI_URL/perms/permissions?length=100000"`
echo All Perms: $ALL_PERMS | grep "directory.api.get"
