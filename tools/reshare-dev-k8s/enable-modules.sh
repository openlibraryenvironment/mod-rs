#!/bin/bash -e

# Loads module descriptors to Okapi at $OKAPI, creates tenant $OKAPI_TENANT,
# then deploys modules passed on STDIN and installs them for  that tenant.

OKAPI="localhost:9130"
OKAPI_TENANT=diku

INSTALLJSON='[ '

echo Loading descriptors
curl -X POST -d '{ "urls": ["https://folio-registry.dev.folio.org"] }' http://${OKAPI}/_/proxy/pull/modules
echo

echo Creating tenant
curl -X POST -d '{ "id": "'"${OKAPI_TENANT}"'", "name": "ReShare Dev Tenant" }' http://${OKAPI}/_/proxy/tenants

while read MODULE; do
  MODULE_DOMAIN=`echo ${MODULE} | tr . -`
  echo Enabling ${MODULE}
  curl -X POST -d '{ "instId":"'"${MODULE}"'-inst", "srvcId":"'"${MODULE}"'", "url":"http://'"${MODULE_DOMAIN}"':8081" }' http://${OKAPI}/_/discovery/modules
  echo
  echo
  INSTALLJSON+='{ "id":"'"${MODULE}"'", "action":"enable" }, '
done

INSTALLJSON=${INSTALLJSON::-2}
INSTALLJSON+=' ]'

echo Installing the following modules for tenant ${OKAPI_TENANT}: ${INSTALLJSON}
echo
curl -X POST -d "${INSTALLJSON}" http://${OKAPI}/_/proxy/tenants/${OKAPI_TENANT}/install?tenantParameters=loadSample%3Dtrue,loadReference%3Dtrue
echo
