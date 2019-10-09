#!/bin/bash

AUTH_TOKEN=`./okapi-login`

OKAPI="http://localhost:9130"
TENANT="diku"

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
fi


# curl -s -H "Content-Type: application/xml" --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -X POST http://localhost:9130/rs/iso18626 -d @- <<EOF
curl -s -H "Content-Type: application/xml" -X POST http://localhost:9130/rs/iso18626 -d @- <<EOF
<test>
  <ele>hello</elem>
</test>
EOF
