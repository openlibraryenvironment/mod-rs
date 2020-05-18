#!/bin/bash

# RESOURCE_LIST=`curl --header "X-Okapi-Tenant: diku" "http://localhost:9130/kiwt/config" -X GET`
# echo Got resource list $RESOURCE_LIST

for resource in patronRequest timer hostLMSLocation directoryEntry
do
  printf "\n\nDocumenting ERM Resource : $resource"
  printf "\n/rs/$resource/?match={FIELD}&filters=path.to.field&perPage=&stats=true&term={VALUE}"
  printf "\n   filters can be repeated and are logically ANDed"
  printf "\n   within a filter logical disjunction can be applied with || - filters=field=value||field=othervalue"
  printf "\nResources returned have the following shape\n"
  curl --header "X-Okapi-Tenant: diku" "http://localhost:9130/rs/kiwt/config/schema/$resource" -X GET
done

