#! /bin/sh

AUTH_TOKEN=`./okapi-login`

echo Listing current requests
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 


PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Brain of the firm",
  patronReference:"Iantest112",
  serviceType:"Loan",
  state:"Idle",
  tags:[
    "Testdata", "TestRun1"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  }
}
'`
