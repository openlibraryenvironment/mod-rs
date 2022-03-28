#!/bin/bash -e

ADMIN_ID=${ADMIN_ID:-'diku_admin'}
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
[[ ! -z "$ADMIN_ID" ]] || ( echo "ADMIN_ID is a required environment variable" && exit 1 )
[[ ! -z "$AUTH_TOKEN" ]] || ( echo "AUTH_TOKEN is a required environment variable" && exit 1 )

# Ensure the admin account has every permission

# Lookup USER UUID
USER_ID=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
  -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30   -XGET "$OKAPI_URL/users?query=username%3D%3D${ADMIN_ID}&length=10000" \
  | jq -rc '.users[0].id')
echo User Id: $USER_UID

# Use the UUID to find the perms user.
PERMS_UID=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
  -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 \
  -XGET "$OKAPI_URL/perms/users?query=userId%3D%3D${USER_ID}&length=10000" | jq -rc ".permissionUsers[0].id")
echo Perms Id: $PERMS_ID

# Grant all to found user.
ALL_PERMS=`curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
  -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 \
  -XGET "$OKAPI_URL/perms/permissions?length=100000"`
echo All Perms: $ALL_PERMS
  
TO_ASSIGN=`echo $ALL_PERMS | jq -rc '.permissions[] |= { permissionName } | .permissions[]'`
for perm in $TO_ASSIGN; do
  echo "Add permission ${perm} (ignore error if already granted)"
  curl --http1.1 -w "\nstatus=%{http_code} size=%{size_download} time=%{time_total} content-type=\"%{content_type}\"\n" \
    -sSL -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
    -H "X-Okapi-Tenant: $TENANT" --connect-timeout 5 --max-time 30 \
    -XPOST "$OKAPI_URL/perms/users/${PERMS_UID}/permissions" -d "$perm"
done
