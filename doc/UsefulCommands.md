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
	gradlew.bat -Dgrails.env=vagrant-db :dbmGormDiff -Pargs="--defaultSchema=diku_mod_rs" > dbChanges.txt

# Run migration and load reference data (change the port to 8080 for mod-directory)
	curl -XPOST -H "Content-Type: application/json" -H "X-OKAPI-TENANT: diku" "http://localhost:8081/_/tenant" -d "{\"parameters\":[{\"key\": \"loadReference\", \"value\": true}]}"

# Run migration and load reference and sample data (change the port to 8080 for mod-directory)
	curl -XPOST -H "Content-Type: application/json" -H "X-OKAPI-TENANT: diku" "http://localhost:8081/_/tenant" -d "{\"parameters\":[{\"key\": \"loadReference\", \"value\": true}, {\"key\": \"loadSample\", \"value\": true}]}"

# Few logs in realtime for east / west or north / south
install kubectl from https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/ which essentially tells you to use the command

	curl -LO "https://dl.k8s.io/release/v1.22.0/bin/windows/amd64/kubectl.exe"
	
Note the version number needs to be within 1 minor version of the cluster, at the time of writing cluster is on 1.21
Ian Hardy will then provide you with a kubectil.cfg file for connecting to the cluster
Run the following to list the pods

	kubectl --kubeconfig ./kubectl.cfg get pods

The pod name that begins with mod-rs-latest is East / West the other pod is North / South with the version in its pod name
So as an example to list the latest logs for East / West run the following

	kubectl logs --kubeconfig ./kubectl.cfg -f mod-rs-latest-76d7cb8cf-n9hsl
