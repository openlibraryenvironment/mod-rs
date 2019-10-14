#! /bin/sh

AUTH_TOKEN=`./okapi-login`

OKAPI="http://localhost:9130"
TENANT="diku"

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
fi



#  serviceType:"Loan",
PATRON_REQ_1=`curl --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST ${OKAPI}/rs/patronrequests -d ' {
  requestingInstitutionSymbol:"RESHARE:KNOWINT01",
  title:"A non-existent title to show the not-supplied case",
  informationSource: "Made up by Ian",
  patronIdentifier:"PB00000001",
  patronReference:"Patron001",
  patronSurname:"patronsurname",
  patronGivenName: "patronGivenName",
  patronType: "Faculty Staff",
  sendToPatron: true,
  isRequester:true,
  neededBy:"2019-10-28"
}
'`

echo Result : $PATRON_REQ_1

echo Parse result to extract request ID
PATRON_REQ_1_ID=`echo $PATRON_REQ_1 | jq -r ".id" | tr -d '\r'`
echo Created request 1: $PATRON_REQ_1_ID
