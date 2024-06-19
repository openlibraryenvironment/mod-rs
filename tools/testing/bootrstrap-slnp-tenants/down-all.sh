docker-compose -f ../docker-compose-apple-m2.yml down

kill -9 $(lsof -t -i :9177)