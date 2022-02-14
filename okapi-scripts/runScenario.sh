#!/bin/bash

# Set where this directory lives in relation to where it is being run from
SCRIPT_DIRECTORY=`dirname "$0"`
#echo Script directory: ${SCRIPT_DIRECTORY}

# setOkapiUrl sets the variable OKAPI_URL
. ${SCRIPT_DIRECTORY}/setOkapiUrl
#echo OKAPI URL: ${OKAPI_URL}

# Get hold of an auth token
AUTH_TOKEN=`${SCRIPT_DIRECTORY}/okapi-login`
#echo Auth Token: $AUTH_TOKEN

# Which tenant are we dealing with
TENANT="diku"

# Read in the patronRequest
PATRON_REQUEST=`cat patronRequest.json`

# Replace the author holder wither the current date / time 
AUTHOR_VALUE=`date +"%Y%m%d%H%M%S"`
AUTHOR_PLACEHOLDER="<<authorPlaceholder>>"
PATRON_REQUEST=${PATRON_REQUEST/"${AUTHOR_PLACEHOLDER}"/"${AUTHOR_VALUE}"}
#echo Patron request: ${PATRON_REQUEST}

RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
   -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/rs/patronrequests" -d "${PATRON_REQUEST}")
#echo Reequest Response: $RESPONSE

# Extract the requester id
REQUESTER_ID=`echo $RESPONSE | jq '.id'`

# Strip the enclosing quotes
REQUESTER_ID=${REQUESTER_ID//"\""/""}
echo Requester Id: ${REQUESTER_ID}

# Sleep for 5 secs to give it time to process
sleep 5

# Lookup responder request
RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
  -H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XGET "$OKAPI_URL/rs/patronrequests?filters=author==${AUTHOR_VALUE}&filters=isRequester==false")
#echo Responder Response: $RESPONSE

# Extract the responder id
RESPONDER_ID=`echo $RESPONSE | jq '.[0].id'`

# Strip the enclosing quotes
RESPONDER_ID=${RESPONDER_ID//"\""/""}
echo "Responder Id: ${RESPONDER_ID}, hrid: `echo $RESPONSE | jq '.[0].hrid'`"

# Have we been supplied a file
SCENARIO_FILE=scenario
if [ -z "$1" ]
then
    echo "Defaulting to scenario file $SCENARIO_FILE as one was not supplied"
elif [ -f "$1" ];
then
    echo "Using $1 for the scenarios file."
	SCENARIO_FILE=$1
else
    echo "Unable to locate scenario file $1, defaulting to scenario file $SCENARIO_FILE"
fi

# Set the surrent id to being the requester
CURRENT_ID=${REQUESTER_ID}

# Read from the file
while read -r LINE
do
	# Ignore comment lines and lines that only contain just whitespace characters
	if [[ ! ${LINE} == \#* ]] && [[ ${LINE} = *[^[:space:]]* ]]
	then
		if [ "requester" = "${LINE}" ];
		then
			# Subsequent actions are against the requester
			CURRENT_ID=${REQUESTER_ID}
			echo "Directing actions against the requester: ${REQUESTER_ID}"
		elif [ "responder" = "${LINE}" ];
		then
			# Subsequent actions are against the requester
			CURRENT_ID=${RESPONDER_ID}
			echo "Directing actions against the responder: ${RESPONDER_ID}"
		elif [ -f "${LINE}" ];
		then
			echo "Reading action json from $LINE to perform against request $CURRENT_ID"
			ACTION_JSON=`cat ${LINE}`
#			echo "JSON: ${ACTION_JSON}"
#			echo "URL: ${OKAPI_URL}/rs/patronrequests/${CURRENT_ID}/performAction"
			RESPONSE=$(curl --http1.1 -sSLf -H "x-okapi-token: $AUTH_TOKEN" -H 'accept: application/json' -H 'Content-type: application/json' \
							-H "X-Okapi-Tenant: $TENANT" --connect-timeout 10 --max-time 30 -XPOST "${OKAPI_URL}/rs/patronrequests/${CURRENT_ID}/performAction" -d "${ACTION_JSON}")
			echo "Response from action: ${RESPONSE}"
			
			# Sleep for 5 seconds for the server to fully digest it (events / protocol messages/ etc ...)
			sleep 5
		else
			echo "Ignoring line in file, as it is not something we understand: $LINE"
		fi	
	fi
done < ${SCENARIO_FILE}

# Seperate string into an array
#IFS=':'
#read -r -a FIELDS <<< "$LINE"
#unset IFS

#if [[ "${#FIELDS[@]}" -eq 0 ]]; then
#echo Empty line
#elif [[ "${#FIELDS[@]}" -eq 1 ]]; then
#echo Action: ${FIELDS[0]}
#elif [[ "${#FIELDS[@]}" -eq 2 ]]; then
#echo Action: ${FIELDS[0]}, File: ${FIELDS[1]}
#else
#echo Line not formatted correctly ignoring
#fi
