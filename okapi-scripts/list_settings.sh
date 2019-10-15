#! /bin/sh

AUTH_TOKEN=`./okapi-login`

OKAPI="http://localhost:9130"
TENANT="diku"

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
fi


response1=`curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/rs/settings/appSettings?stats=true"`
echo $response1