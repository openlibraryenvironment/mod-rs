#!/bin/bash

AUTH_TOKEN=`./okapi-login`

OKAPI="http://localhost:9130"
TENANT="diku"

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
fi


# curl -s -H "Content-Type: application/xml" --header "X-Okapi-Tenant: ${TENANT}" -H "X-Okapi-Token: ${AUTH_TOKEN}" -X POST http://localhost:9130/rs/iso18626 -d @- <<EOF
curl -s -H "Content-Type: application/xml" -X POST http://localhost:9130/rs/iso18626 -d @- <<EOF
<ISO18626Message ill:version="1.0" xsi:schemaLocation="http://illtransactions.org/2013/iso18626 http://illtransactions.org/schemas/ISO-18626-v1_1.xsd">
  <request>
    <header>
      <supplyingAgencyId>
        <agencyIdType>ISIL</agencyIdType><agencyIdValue>DK-710100</agencyIdValue>
      </supplyingAgencyId>
      <requestingAgencyId>
        <agencyIdType>ISIL</agencyIdType>
        <agencyIdValue>DK-820010</agencyIdValue>
      </requestingAgencyId>
      <timestamp>2014-03-17T09:30:47.0Z</timestamp>
      <requestingAgencyRequestId>49837539</requestingAgencyRequestId>
    </header>
    <bibliographicInfo>
      <supplierUniqueRecordId>27345786</supplierUniqueRecordId>
    </bibliographicInfo>
    <serviceInfo>
      <serviceType>Loan</serviceType>
      <serviceLevel>Normal</serviceLevel>
      <needBeforeDate>2014-05-01T00:00:00.0Z</needBeforeDate>
      <anyEdition>Y</anyEdition>
    </serviceInfo>
  </request>
</ISO18626Message>
EOF
