#! /bin/sh

AUTH_TOKEN=`./okapi-login`
USER_ID=$(curl -sSL -H "X-Okapi-Tenant:diku" -H "X-Okapi-Token:$AUTH_TOKEN" \
    -H "Content-Type: application/json" \
    -X GET "http://localhost:9130/users?query=username=diku_admin" | jq -r '.users | .[0] | .id')
echo $USER_ID
