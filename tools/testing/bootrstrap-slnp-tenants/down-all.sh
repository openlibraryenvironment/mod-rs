docker-compose -f ../docker-compose-apple-m2.yml down

sudo kill -9 $(sudo lsof -t -i :9177)