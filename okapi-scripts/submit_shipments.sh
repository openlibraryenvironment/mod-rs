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
SHIP_1=`curl --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST ${OKAPI}/rs/shipments -d ' {
  id:"000001",
  trackingNumber: "9999-9999-0001",
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  systemInstanceIdentifier:"893475987348973",
}
'`

SHIP_2=`curl --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST ${OKAPI}/rs/shipments -d ' {
  id:"000002",
  trackingNumber: "9999-9999-0002",
  systemInstanceIdentifier:"893475987348974",
}
'`

echo Result : $SHIP_1
echo Result : $SHIP_2

echo Parse result to extract request ID
SHIP_1_ID=`echo $SHIP_1 | jq -r ".id" | tr -d '\r'`
SHIP_2_ID=`echo $SHIP_2 | jq -r ".id" | tr -d '\r'`
echo Created request 1: $SHIP_1_ID
echo Created request 2: $SHIP_2_ID
