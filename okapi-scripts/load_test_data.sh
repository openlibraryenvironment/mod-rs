#! /bin/sh

AUTH_TOKEN=`./okapi-login`

echo Listing current requests
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 


echo Get the directory record for our responding system - we want to send a request to DIKUA
RESHARE_DIKUA=`curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/directory/entry?filters=symbols.symbol%3dDIKUA&filters=symbols.authority.value=RESHARE&stats=true"`

DIKUA_ID=`echo $RESHARE_DIKUA | jq -r ".results[0].id" | tr -d '\r'`

echo Submitting requests to responder with symbol RESHARE:DIKUA - has directory ID $RESHARE_DIKUA

PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Brain of the firm",
  author:"Beer, Stafford",
  patronReference:"Patron001",
  state:"Idle",
  isRequester = true,
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  serviceType:"Loan",
  pendingAction:"approve",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  },
  rota:[
    { directoryId:"'"$DIKUA_ID"'", rotaPosition:"0" }
  ]
}
'`

PATRON_REQ_1_ID=`echo $PATRON_REQ_1 | jq -r ".id" | tr -d '\r'`

echo Created request 1: $PATRON_REQ_1
echo Created request 1: $PATRON_REQ_1_ID


PATRON_REQ_2=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"The Heart of Enterprise",
  author:"Beer, Stafford",
  patronReference:"Patron001",
  state:"Idle",
  isRequester = true,
  serviceType:"Loan",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  },
  rota:[
    { directoryId:"'"$DIKUA_ID"'", rotaPosition:"0" }
  ]
}
' | jq -r ".id" | tr -d '\r'`

echo Created request 2: $PATRON_REQ_2

PATRON_REQ_3=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  title:"Biological neural networks in invertebrate neuroethology and robotics",
  isbn:"978-0120847280",
  patronReference:"Patron004",
  publisher:"Boston : Academic Press",
  state:"Idle",
  isRequester = true,
  serviceType:"Loan",
  tags:[
    "Testdata", "TestRun1", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  },
  rota:[
    { 
      directoryId:"'"$DIKUA_ID"'", 
      rotaPosition:"0" ,
      availability:"Availability as a string from the shared index",
      normalisedAvailability:"Unknown",
      protocolStatus:0,
      shelfmark:"A shelfmark",
      systemIdentifier:"The remote identifier for the ITEM",
      state:"Idle"
    }
  ]
}
' | jq -r ".id" | tr -d '\r'`

echo Created request 3: $PATRON_REQ_3


echo Attempt to read back request 1

curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests/$PATRON_REQ_1_ID

echo Find out what valid actions we can take for patron request 1
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests/$PATRON_REQ_1_ID/validActions

echo List requests after creation
curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X GET http://localhost:9130/rs/patronrequests 

echo hit return to exit
read a
