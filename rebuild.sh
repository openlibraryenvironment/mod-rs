#!/bin/bash
( cd service && ./gradlew --no-daemon -x integrationTest --console=plain clean build)
docker build -t mod-rs:2.7-debug .
docker tag mod-rs:2.7-debug 806757567005.dkr.ecr.us-east-1.amazonaws.com/mod-rs:2.7-debug
docker push 806757567005.dkr.ecr.us-east-1.amazonaws.com/mod-rs:2.7-debug
kubectl -n reshare-upgrade rollout restart deployment mod-rs-2-7-0
