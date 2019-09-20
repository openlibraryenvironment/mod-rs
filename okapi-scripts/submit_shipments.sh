#! /bin/sh

AUTH_TOKEN=`./okapi-login`

OKAPI="http://localhost:9130"
TENANT="diku"

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
fi


response1=`curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/rs/directoryEntry?filters=name%3dUT%20Austin:%20Main%20Branch&stats=true"`
id1=`echo $response1|jq -r ".results[0].id" | tr -d '\r'`

response2=`curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/rs/directoryEntry?filters=name%3dNew%20School:%20Law%20Library&stats=true"`
id2=`echo $response2|jq -r ".results[0].id" | tr -d '\r'`

response3=`curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/rs/directoryEntry?filters=name%3dUniversity%20of%20Sheffield:%20The%20IC&stats=true"`
id3=`echo $response3|jq -r ".results[0].id" | tr -d '\r'`

response4=`curl -sSL --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: $AUTH_TOKEN" -H "Content-Type: application/json" -X GET "http://localhost:9130/rs/directoryEntry?filters=name%3dUniversity%20of%20Leeds:%20Brotherton%20Library&stats=true"`
id4=`echo $response4|jq -r ".results[0].id" | tr -d '\r'`

#  serviceType:"Shipment",
SHIP_1=`curl --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST ${OKAPI}/rs/shipments -d ' {
  id:"000001",
  shippingLibrary:{id: "'"$id1"'"},
  receivingLibrary:{id: "'"$id2"'"},
  shipDate:"1925-01-01T12:30:00",
  receivedDate:"2019-09-19T15:09:00",
  trackingNumber: "1234-5678-0001",
  notARealProperty:"Test that this is ignored in accordance with Postels law",
  systemInstanceIdentifier:"893475987348973"
}
'`

SHIP_2=`curl --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST ${OKAPI}/rs/shipments -d ' {
  id:"000002",
  shippingLibrary: {id: "'"$id3"'"},
  receivingLibrary:{id: "'"$id4"'"},
  shipDate:"2019-09-19T15:08:59",
  receivedDate:"2019-09-19T15:09:00", 
  trackingNumber: "1234-5678-0002",
  systemInstanceIdentifier:"893475987348974"
}
'`

echo Result : $SHIP_1
echo Result : $SHIP_2

echo Parse result to extract request ID
SHIP_1_ID=`echo $SHIP_1 | jq -r ".id" | tr -d '\r'`
SHIP_2_ID=`echo $SHIP_2 | jq -r ".id" | tr -d '\r'`
echo Created request 1: $SHIP_1_ID
echo Created request 2: $SHIP_2_ID
