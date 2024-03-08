# Postman newman CLI script - bootrstrap SLNP tenants

# Install Newman from npm globally on your system, enabling you to run it from anywhere:

    npm install -g newman

# Running Newman:

    newman run mycollection.json

# You can also pass a collection as a URL by sharing it:

    newman run https://www.postman.com/collections/cb208e7e64056f5294e5

# You can also pass environment file:

    newman run mycollection.json -e dev_environment.json

# Example collection with failing tests:

Status Code Test
GET https://postman-echo.com/status/404 [404 Not Found, 534B, 1551ms]
1\. response code is 200

|                                       | executed | failed |
|---------------------------------------|----------|--------|
| iterations                            | 1        | 0      |
| requests                              | 1        | 0      |
| test-scripts                          | 1        | 0      |
| prerequest-scripts                    | 0        | 0      |
| assertions                            | 1        | 1      |


#  Failure details

1\.  AssertionFai…  response code is 200
at assertion:1 in test-script
inside "Status Code Test" of "Example Collection with
Failing Tests"