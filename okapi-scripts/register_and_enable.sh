BASEDIR=$(dirname "$0")
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
curl -XDELETE "http://localhost:9130/_/proxy/tenants/diku/modules/${SVC_ID}"
curl -XDELETE "http://localhost:9130/_/discovery/modules/${SVC_ID}/${INS_ID}"
curl -XDELETE "http://localhost:9130/_/proxy/modules/${SVC_ID}"

# ./gradlew clean generateDescriptors
echo Install latest module ${SVC_ID}/${INS_ID} 
curl -XPOST http://localhost:9130/_/proxy/modules -d @"${DESCRIPTORDIR}/ModuleDescriptor.json"

echo Install deployment descriptor
curl -XPOST http://localhost:9130/_/discovery/modules -d "$DEP_DESC"

echo Activate for tenant diku
curl -XPOST http://localhost:9130/_/proxy/tenants/diku/modules -d `echo $DEP_DESC | jq -rc '{id: .srvcId}'`
popd
