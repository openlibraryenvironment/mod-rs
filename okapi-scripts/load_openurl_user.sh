#! /bin/sh

AUTH_TOKEN=`./okapi-login`

# Annoyingly, we need to create FOUR records:
# 1. The group that the OpenURL user will belong to
# 2. The user itself
# 3. The user's credentials
# 4. The user's password


GROUPID=$(
curl -s --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/groups -d @- <<EOF | tee /dev/tty | jq -r .id
{
  "group": "system",
  "desc": "For non-user accounts"
}
EOF
)

if [ "x$GROUPID" = xnull ]; then
    echo "Group creation failed: already exists?"
    exit 1;
fi
echo "Made group $GROUPID"


USERID=$(
curl -s --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/users -d @- <<EOF | tee /dev/tty | jq -r .id
{
  "id": "485bf173-8b96-1745-5ae7-5f51a8e45794",
  "active": "true",
  "patronGroup": "$GROUPID",
  "personal": {
    "email": "user1@index.com",
    "lastName": "OpenURL",
    "preferredContactTypeId": "002"
  },
  "username": "openurl"
}
EOF
)

if [ "x$USERID" = xnull ]; then
    echo "User creation failed: already exists?"
    exit 1;
fi
echo "Made user $USERID"


curl -s --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/authn/credentials -d @- <<EOF
{
  "userId": "$USERID",
  "username": "openurl",
  "password": "swordfish"
}
EOF


curl -s --header "X-Okapi-Tenant: diku" -H "X-Okapi-Token: ${AUTH_TOKEN}" -H "Content-Type: application/json" -X POST http://localhost:9130/perms/users -d @- <<EOF
{
  "permissions": [],
  "userId": "$USERID"
}
EOF
exit

{
  "id" : "6f5fe2fb-7c5c-4d27-93f8-3aef69646b09",
  "userId" : "485bf173-98b6-4175-a5e7-58f51a4e75c6",
  "permissions" : [ ],
  "metadata" : {
    "createdDate" : "2019-02-22T12:03:31.113+0000",
    "createdByUserId" : "e6b440d7-b455-59ab-ada8-dd03d0449ae8",
    "updatedDate" : "2019-02-22T12:03:31.113+0000",
    "updatedByUserId" : "e6b440d7-b455-59ab-ada8-dd03d0449ae8"
  }
}
