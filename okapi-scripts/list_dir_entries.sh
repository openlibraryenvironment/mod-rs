#! /bin/sh

AUTH_TOKEN=`./okapi-login`

OKAPI="http://localhost:9130"
TENANT="diku"

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
fi



curl --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET ${OKAPI}/rs/directoryEntry
