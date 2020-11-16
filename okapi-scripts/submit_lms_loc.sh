#! /bin/sh

if [ $# -ne 2 ]; then
   echo "Usage: $0 <locationCode> <locationName>" >&2
   exit 1;
fi

. ~/.okapi
set -x
curl \
	-H "X-Okapi-Tenant: ${OKAPI_TENANT}" \
	-H "X-Okapi-Token: ${OKAPI_TOKEN}" \
	-H "Content-Type: application/json" \
	-X POST \
	${OKAPI_URL}/rs/hostLMSLocations \
	-d "{
  code: \"$1\",
  name: \"$2\"
}"
