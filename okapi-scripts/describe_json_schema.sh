#! /bin/sh

AUTH_TOKEN=`./okapi-login`

echo Listing current requests
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 

#  serviceType:"Loan",
PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Brain of the firm",
  author:"Beer, Stafford",
  patronReference:"Patron001",
  state:"Idle",
  isRequester = true,
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  pendingAction:"approve",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  },
  rota:[
    { directoryId:"TEST-DIRENT-000001", rotaPosition:"0" },
    { directoryId:"TEST-DIRENT-000002", rotaPosition:"1" },
    { directoryId:"TEST-DIRENT-000003", rotaPosition:"2" },
    { directoryId:"TEST-DIRENT-000004", rotaPosition:"3" },
    { directoryId:"TEST-DIRENT-000005", rotaPosition:"4" },
    { directoryId:"TEST-DIRENT-000006", rotaPosition:"5" }
  ]
}
'`

echo Result : $PATRON_REQ_1

PATRON_REQ_1_ID=`echo PATRON_REQ_1 | jq -r ".id" | tr -d '\r'`

echo Created request 1: $PATRON_REQ_1
