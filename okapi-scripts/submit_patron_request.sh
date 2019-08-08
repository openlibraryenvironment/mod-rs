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
PATRON_REQ_1=`curl --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/rs/patronrequests -d ' {
  requestingInstitutionSymbol:"wibble:wobble",
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
  patronReference:"Patron001",
  patronSurname:"patronsurname",
  patronGivenName: "patronGivenName",
  patronType: "Faculty Staff",
  sendToPatron: true,
  state:"Idle",
  isRequester:true,
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  pendingAction:"approve",
  tags:[
    "Testdata", "TestCase001", "MonographTest"
  ],
  customProperties:{
    "patronWalletHash": ["298348743738748728524854289743765"],
  },
  rota:[
    { 
      directoryId:"A-DIR-ID", 
      rotaPosition:"0" ,
      availability:"Availability as a string from the shared index",
      normalisedAvailability:"Unknown",
      protocolStatus:0,
      shelfmark:"A shelfmark",
      systemIdentifier:"The remote identifier for the ITEM",
      state:"Idle"
    },
    { 
      directoryId:"ANOTHER-DIR-ID", 
      rotaPosition:"1" ,
      availability:"Availability as a string from the shared index",
      normalisedAvailability:"Unknown",
      protocolStatus:0,
      shelfmark:"Another shelfmark",
      systemIdentifier:"The remote identifier for a different ITEM",
      state:"Pending"
    }
  ]
}
'`

echo Result : $PATRON_REQ_1

echo Parse result to extract request ID
PATRON_REQ_1_ID=`echo $PATRON_REQ_1 | jq -r ".id" | tr -d '\r'`
echo Created request 1: $PATRON_REQ_1_ID
