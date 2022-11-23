# To run integration tests
	# Ensure you do not have mod-directory running as that will conflict with ports being used
	cd tools\testing
	docker-compose up
		# In a separate window
		cd service
		gradlew.bat clean build test
	# When finished
	Ctrl+c docker-compose
	docker-compose down -v

# View count the number of dockers running
	docker ps| wc -l

# View okapi logs
	docker logs -f okapi
	docker logs -f magical_newton (for email service)

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
	gradlew.bat -Dgrails.env=rancher-desktop :dbmGormDiff -Pargs="--defaultSchema=test1_mod_rs" > dbChanges.txt

# Run migration and load reference and sample data (change the port to 8080 for mod-directory)
	curl -XPOST -H "Content-Type: application/json" -H "X-OKAPI-TENANT: test1" "http://localhost:8081/_/tenant" -d "{\"parameters\":[{\"key\": \"loadReference\", \"value\": true}, {\"key\": \"loadSample\", \"value\": true}]}"

# View logs in realtime for east / west or north / south
install kubectl from https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/ which essentially tells you to use the command

	curl -LO "https://dl.k8s.io/release/v1.22.0/bin/windows/amd64/kubectl.exe"
	
Note the version number needs to be within 1 minor version of the cluster, at the time of writing cluster is on 1.21
Ian Hardy will then provide you with a kubectil.cfg file for connecting to the cluster
Run the following to list the pods

	kubectl --kubeconfig ./kubectl.cfg get pods

The pod name that begins with mod-rs-latest is East / West the other pod is North / South with the version in its pod name
So as an example to list the latest logs for East / West run the following

	kubectl logs --kubeconfig ./kubectl.cfg -f mod-rs-latest-76d7cb8cf-n9hsl

# Inspect database for east / west or north / south
Login via ssh to id-bastion.folio-dev.indexdata.com see Ian for access
run ./connect.sh
you are now in psql, use schema reshare_east_mod_rs, reshare_west_mod_rs , etc ...

# Generating State Model Graph
	curl --http1.1 -sSLf -H "accept: image/png" -H "X-Okapi-Tenant: diku" --connect-timeout 10 --max-time 300 -XGET http://localhost:8081/rs/availableAction/createGraph/PatronRequest?height=4000\&excludeActions=manualClose
