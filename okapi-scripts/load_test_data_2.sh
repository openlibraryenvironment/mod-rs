#! /bin/sh

AUTH_TOKEN=`./okapi-login`

# echo Listing current requests
# curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 

#  serviceType:"Loan",
PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Brain of the firm",
  author:"Beer, Stafford",
  subtitle:"The organisational cybernetics of business",
  sponsoringBody:"A sponsoring body",
  publisher: "Wiley",
  placeOfPublication: "London",
  volume: "1",
  issue: "1",
  startPage: "1",
  numberOfPages: "123",
  publicationDate: "1972",
  edition:"2nd",
  isbn: "123456789",
  informationSource: "Ians head",
  patronReference:"Patron001",
  patronSurname:"patronsurname",
  patronGivenName: "patronGivenName",
  patronType: "FacultyS Staff",
  sendToPatron: true,
  state:"Idle",
  isRequester:true,
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  pendingAction:"approve",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
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
