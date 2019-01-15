#!/bin/bash

curl --header "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:8080/rs/patronRequests -X GET
