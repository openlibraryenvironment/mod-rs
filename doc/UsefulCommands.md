# View count the number of dockers running
docker ps| wc -l

# View okapi logs
	docker logs -f okapi

# Install npm-groovy-lint from here
	https://github.com/nvuillam/npm-groovy-lint#Autofixable-rules

### To run lint against all the groovy files
	npm-groovy-lint --files "**/*.groovy"

### To see the rules that can be applied by the groovy lint
	https://codenarc.org/codenarc-rule-index.html

### lint rules live in
	mod-rs/.groovylintrc.json

# To generate db changes
	gradlew.bat -Dgrails.env=vagrant-db :dbmGormDiff -Pargs="--defaultSchema=diku_mod_rs"

# Run migration and load reference data (change the port to 8080 for mod-directory)
	curl -XPOST -H "Content-Type: application/json" -H "X-OKAPI-TENANT: diku" "http://localhost:8081/_/tenant" -d "{\"parameters\":[{\"key\": \"loadReference\", \"value\": true}]}"

# Run migration and load reference and sample data (change the port to 8080 for mod-directory)
	curl -XPOST -H "Content-Type: application/json" -H "X-OKAPI-TENANT: diku" "http://localhost:8081/_/tenant" -d "{\"parameters\":[{\"key\": \"loadReference\", \"value\": true}, {\"key\": \"loadSample\", \"value\": true}]}"

