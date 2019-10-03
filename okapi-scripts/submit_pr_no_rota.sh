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
  title:"Platform for Change",
  author:"Beer, Stafford A",
  subtitle:"A message from Stafford Beer",
  sponsoringBody:"A sponsoring body",
  publisher: "Wiley",
  placeOfPublication: "London",
  volume: "1",
  issue: "1",
  startPage: "1",
  numberOfPages: "123",
  publicationDate: "1972",
  edition:"2nd",
  isbn: "0471948403",
  informationSource: "Made up by Ian",
  patronIdentifier:"PB00000001",
  patronReference:"Patron001",
  patronSurname:"patronsurname",
  patronGivenName: "patronGivenName",
  patronType: "Faculty Staff",
  sendToPatron: true,
  state:"Idle",
  isRequester:true,
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  systemInstanceIdentifier:"893475987348973",
  tags:[
    "Testdata", "TestCase001", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  }
}
'`

echo Result : $PATRON_REQ_1

echo Parse result to extract request ID
PATRON_REQ_1_ID=`echo $PATRON_REQ_1 | jq -r ".id" | tr -d '\r'`
echo Created request 1: $PATRON_REQ_1_ID
