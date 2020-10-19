#!/bin/bash

AUTH_TOKEN=`./okapi-login`
echo "Got auth token $AUTH_TOKEN"
ADMIN_USER_ID=`./get_admin_id.sh`
echo "Got admin id $ADMIN_USER_ID"

curl -H "X-Okapi-Tenant:diku" -H "X-Okapi-Token:$AUTH_TOKEN" \
    -H "Content-Type: application/json" -X POST \
    -d '{"permissionName" : "rs.all"}' \
    "http://localhost:9130/perms/users/$ADMIN_USER_ID/permissions?indexField=userId"
