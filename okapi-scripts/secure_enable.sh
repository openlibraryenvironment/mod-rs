BASEDIR=$(dirname "$0")

# This script generates the module descriptor for mod-directory and posts it to a secured OKAPI control interface
# the script is controlled by a ~/.okapirc file where you need to specify the supertenant username (ST_UN)
# supertnent password (ST_PW) and the OKAPI_URL (For the rancher-desktop install, likely http://localhost:30100)

if [ -f .okapirc ]; then
  . .okapirc
elif [ -f $HOME/.okapirc ]; then
  . $HOME/.okapirc
else
  echo You must configure \$HOME/.okapirc
  echo export IS_SECURE_SUPERTENANT=
  echo export ST_UN=
  echo export ST_PW=
  echo export OKAPI_URL=
  exit 0
fi

echo Enable mod-rs for tenant $1 on URL $2

echo $BASEDIR
pushd "$BASEDIR/../service"

DIR="$BASEDIR/../"

echo "\nUsing directory $DIR"

# Check for decriptor target directory.

DESCRIPTORDIR="../service/build/resources/main/okapi"

if [ ! -d "$DESCRIPTORDIR" ]; then
    echo "No descriptors found. Let's try building them."
    
    ./gradlew generateDescriptors
fi

# cat ../service/build/resources/main/okapi/DeploymentDescriptor.json |  jq -c '.url="http://a.b.c"'
DEP_DESC=`cat ${DESCRIPTORDIR}/DeploymentDescriptor.json`
SVC_ID=`echo $DEP_DESC | jq -rc '.srvcId'`
INS_ID=`echo $DEP_DESC | jq -rc '.instId'`

AUTH_TOKEN=`../okapi-scripts/okapi-login -u $ST_UN -p $ST_PW -t supertenant`

ENABLE_DOC=`echo $DEP_DESC | jq -c '[{id: .srvcId, action: "enable"}]'`
echo "Enable service - enable doc is $ENABLE_DOC"

curl -XPOST -H "X-Okapi-Token: $AUTH_TOKEN" "${OKAPI_URL}/_/proxy/tenants/$1/install?tenantParameters=loadSample%3Dtest,loadReference%3Dother" -d "$ENABLE_DOC"

echo Done
