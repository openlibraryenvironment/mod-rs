BASEDIR=$(dirname "$0")

# setOkapiUrl sets the variable OKAPI_URL
. ./setOkapiUrl
echo OKAPI URL: ${OKAPI_URL}

# set the tenant we areinterested in using
TENANT_NAME=diku

echo $BASEDIR
pushd "$BASEDIR/../service"

DIR="$BASEDIR/../"

echo "Using directory $DIR"

# Check for decriptor target directory.

DESCRIPTORDIR="../service/build/resources/main/okapi"

if [ ! -d "$DESCRIPTORDIR" ]; then
    echo "No descriptors found. Let's try building them."
    
    ./gradlew generateDescriptors
fi

DEP_DESC=`cat ${DESCRIPTORDIR}/DeploymentDescriptor.json`
SVC_ID=`echo $DEP_DESC | jq -rc '.srvcId'`
INS_ID=`echo $DEP_DESC | jq -rc '.instId'`

echo Remove any existing module ${SVC_ID}/${INS_ID}
echo Waiting for curl -XDELETE "${OKAPI_URL}/_/proxy/tenants/${TENANT_NAME}/modules/${SVC_ID}"
curl -XDELETE "${OKAPI_URL}/_/proxy/tenants/${TENANT_NAME}/modules/${SVC_ID}"

echo Waiting for curl -XDELETE "${OKAPI_URL}/_/discovery/modules/${SVC_ID}/${INS_ID}"
curl -XDELETE "${OKAPI_URL}/_/discovery/modules/${SVC_ID}/${INS_ID}"

echo Waiting for curl -XDELETE "${OKAPI_URL}/_/proxy/modules/${SVC_ID}"
curl -XDELETE "${OKAPI_URL}/_/proxy/modules/${SVC_ID}"

# ./gradlew clean generateDescriptors
echo Install latest module ${SVC_ID}/${INS_ID} 
curl -XPOST ${OKAPI_URL}/_/proxy/modules -d @"${DESCRIPTORDIR}/ModuleDescriptor.json"

echo Install deployment descriptor
curl -XPOST ${OKAPI_URL}/_/discovery/modules -d "$DEP_DESC"

echo Activate for tenant ${TENANT_NAME}
# curl -XPOST ${OKAPI_URL}/_/proxy/tenants/${TENANT_NAME}/modules -d `echo $DEP_DESC | jq -rc '{id: .srvcId}'`
curl -XPOST ${OKAPI_URL}/_/proxy/tenants/${TENANT_NAME}'/install?tenantParameters=loadSample%3Dtrue,loadReference%3Dtrue' -d `echo $DEP_DESC | jq -c '[{id: .srvcId, action: "enable"}]'`

popd
