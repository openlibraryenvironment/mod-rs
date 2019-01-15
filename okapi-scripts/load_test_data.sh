#! /bin/sh

AUTH_TOKEN=`./okapi-login`

echo Listing current requests
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 

YESNO_CAT=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/refdata -d ' {
  "desc":"Yes/No/Other",
  "values":[
    { "value":"Yes", "label":"Yes" },
    { "value":"No", "label":"No" },
    { "value":"Other", "label":"Other (see notes)" }
  ]
}'`

echo Result of create YesNo Cat: $YESNO_CAT



PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Brain of the firm",
  author:"Beer, Stafford",
  patronReference:"Patron001",
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  serviceType:"Loan",
  state:"Idle",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  }
}
' | jq -r ".id" | tr -d '\r'`

echo Created request 1: $PATRON_REQ_1


PATRON_REQ_2=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"The Heart of Enterprise",
  author:"Beer, Stafford",
  patronReference:"Patron001",
  serviceType:"Loan",
  state:"Idle",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  }
}
' | jq -r ".id" | tr -d '\r'`

echo Created request 2: $PATRON_REQ_2

PATRON_REQ_3=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Biological neural networks in invertebrate neuroethology and robotics",
  isbn:"978-0120847280",
  patronReference:"Patron004",
  publisher:"Boston : Academic Press",
  serviceType:"Loan",
  state:"Idle",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  }
}
' | jq -r ".id" | tr -d '\r'`

echo Created request 3: $PATRON_REQ_3


echo Attempt to read back request 1

curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests/$PATRON_REQ_1

echo List requests after creation
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 
