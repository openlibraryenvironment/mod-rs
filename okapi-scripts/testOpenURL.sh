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

RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 300 -XGET "http://localhost:8081/rs/patronrequests/openURL?Action=10&Form=30&rft.genre=book&rft.title=Property+and+wealth+in+classical+Sparta+%2F&rft.stitle=Property+and+wealth+in+classical+Sparta+%2F&rft.atitle=&rft.date=2000.&rft.month=&rft.volume=&rft.issue=&rft.number=&rft.epage=&rft.spage=&rft.edition=&rft.isbn=0715630407&rft.eisbn=&rft.au=,&rft.auinit=&rft.pub=Duckworth+and+the+Classical+Press+of+Wales&rft.publisher=Duckworth+and+the+Classical+Press+of+Wales&rft.place=London+%3A&rft.doi=&rfe_dat=44786349&rfr_id=info%3Asid%2Fprimo.exlibrisgroup.com-01NWU_ALMA&svc.pickupLocation=ChasSlug&rft.identifier.illiad=illiad123")
echo Reequest Response: $RESPONSE

#RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' \
#   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 300 -XGET "http://localhost:8081/rs/patronrequests/openURL?artnum=articleNumber&aufirst=authorFirstName&aulast=authorLastName&bici=bici&coden=coden&issn=issn&eissn=eissn&isbn=isbn&issue=issue&epage=endPage&spage=startPage&pages=numberOfPages&part=part&date=publicationDate&quarter=quarter&ssn=ssn&sici=sici&title=title&stitle=stitle&atitle=atitle&volume=volume&pickupLocation=ChasSlug")
#echo Reequest Response: $RESPONSE
