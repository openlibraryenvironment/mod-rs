#!/bin/bash

# Start Docker Compose services
docker-compose -f ../docker-compose-apple-m2.yml up -d

# Set environment variables
export DATASOURCE_PASSWORD=folio_admin
export DATASOURCE_URL=jdbc:postgresql://localhost:54321/okapi_modules
export DATASOURCE_USERNAME=folio_admin
export DB_DATABASE=okapi_modules
export DB_HOST=localhost
export DB_PASSWORD=folio_admin
export DB_PORT=54321
export DB_USERNAME=folio_admin
export EVENTS_CONSUMER_BOOTSTRAP_SERVERS=localhost:29092
export EVENTS_CONSUMER_ZK_CONNECT=localhost:2181
export EVENTS_PUBLISHER_BOOTSTRAP_SERVERS=localhost:29092
export EVENTS_PUBLISHER_ZK_CONNECT=localhost:2181
export KAFKA_HOST=localhost
export KAFKA_PORT=29092

cd ../../../service

nohup ./gradlew bootRun --args='--server.port=9177' > gradle_output.log 2>&1 &

# Wait for Docker Compose services to start
echo "Waiting for services to be up..."
while ! curl -s http://localhost:9177/healthcheck > /dev/null; do
    echo "Service is not up yet, waiting..."
    sleep 5
done
echo "Service is up!"

cd ../tools/testing/bootrstrap-slnp-tenants
newman run mod-rs-setup.collection.json -e mod-rs-env.dev_environment.json

echo "Data inserted"
