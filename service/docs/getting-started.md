# Getting started
Get up-to-speed and set up with a Grails project that can be deployed as an OKAPI backend module. To create a new backend module that you wish to develop locally please clone/fork this repository.

## Pre - reading
The UI module is designed to work within the [FOLIO](https://github.com/folio-org) project behind the [OKAPI gateway](https://github.com/folio-org/okapi).
Our modules are built using the [Grails framework](http://docs.grails.org/latest/guide/single.html).
Modules are compiled into single jar application using [Spring boot](https://projects.spring.io/spring-boot/).

There are some pre-installed tools to help with [OKAPI integration](okapi-integration.md).

## Prerequisites
This starter project uses [Postgres](https://www.postgresql.org/) to fit with the rest of the FOLIO project. You will need to install postgres and create a databse for this (and other) modules to use:
There are 3 databases defined in the starter config,
* mod-rs_dev;
* mod-rs_test;
* and mod-rs.

Each uses the same credentials by default to connect:
* un: folio
* pw: folio

These can be set to whatever you like during the creation of the databases but be sure to reflect your changes in the the application config file at `grails-app/conf/application.yml` Example config that can be executed by postgres user:

    CREATE USER folio WITH PASSWORD 'folio' SUPERUSER CREATEDB INHERIT LOGIN;

    DROP DATABASE mod-rs_dev;
    CREATE DATABASE mod-rs_dev;
    GRANT ALL PRIVILEGES ON DATABASE mod-rs_dev to folio;
    DROP DATABASE mod-rs_test;
    CREATE DATABASE mod-rs_test;
    GRANT ALL PRIVILEGES ON DATABASE mod-rs_test to folio;
    DROP DATABASE mod-rs;
    CREATE DATABASE mod-rs;
    GRANT ALL PRIVILEGES ON DATABASE mod-rs to folio;

### Dockerized postgres local development

If you're running a local dockerized postgres, here is one way to get to the command line you will need to run these commands:

   docker exec -it your_pg_container_name psql -U postgres
   
_Note:_ If you are running using the version within the vagrant image then you should use the profile detailed later in this document.

To install and manage the following pre-requisites I recommend using [SDKMAN](http://sdkman.io/).
- [Groovy](http://groovy-lang.org/)
- [Grails](https://grails.org/)

With sdkman installed as above it's as easy as opening a terminal and typing:
* `sdk install groovy`
* `sdk install grails`

# Running the Application
From the root of your grails project you should be able to start the application by typing:
```grails run-app```

The above command should start the application using the development profile. If you are using either the folio vagrant backend from this repo, or a more recent one,
the app should try and automatically register itself as an okapi module and also register the deployment with the service. You should see console output like the following:
```
INFO --- [           main] com.k_int.okapi.OkapiClient              : Success: Got response [id: ...]

...

INFO --- [           main] com.k_int.okapi.OkapiClient              : Success: Got response [instId:10.0.2.2, ..., url:http://10.0.2.2:8080/]
```

For the above to work the app needs to know where to find OKAPI. These are both defined in the development profile section of the application settings file located at
`service/grails-app/conf/application.yml`

If you do see the above then you can safely skip the following section on registering with OKAPI.

## Registering our module with OKAPI in the vagrant machine
Part of the build process of the module produces some OKAPI descriptors. The templates can be found in `service/src/okapi`. The placeholders are substituted for values that are generated as part of the build process and then the descriptors
written to: `build/resources/okapi` with values substituted and the template suffix removed. To compile the app without running you can type:
```
grails compile
```

You can then use these json descriptors to register and deploy your module when it is running. See the [deployment and discovery](https://github.com/folio-org/okapi/blob/master/doc/guide.md#deployment-and-discovery) section of the OKAPI docs.
This allows you to run your module outside of the other core modules (for instance within your IDE) and debug in the normal way while developing.

## Alternate grails profile (Vagrant DB)
If you use the vagrant file from this repo, you will see that it forwards a local port of 54321 to the version of postgres running inside the vagrant machine. This is not
the default postgres port so as to avoid clashes with people who run postgres locally too. If you don't have postgres running locally, or wish to cause the app to connect to the
version inside the vagrant machine you can start the application using the alternate 'vagrant-db' profile:
```
grails -Dgrails.env=vagrant-db run-app
```

This profile also attempts to self register the app.

## Self registration
In order for self registration to work, the app needs to know where OKAPI lives. This is detailed in the application.yml settings file:
```
okapi: 
  service:
    host: localhost
    port: 9130
```
These are currently only set for the 'development' and 'vagrant-db' profiles. So if the app is started in production mode, the module will not attempt to register itself.

_Also_: It is possible to run the application directly as a spring boot app in an IDE. Doing so however, will bypass gradle and therfore the necessary descriptors will not
be present. This will cause automatic registration to skip also and you will see log output like:
```
INFO --- [           main] com.k_int.okapi.OkapiClient              : Skipping registration as no module descriptor could be found on the path.
INFO --- [           main] com.k_int.okapi.OkapiClient              : Skipping deployment registration with discovery as no deployment descriptor could be found on the path.
```

If you do encounter these messages you can manually register by running the script `scripts/register_and_enable.sh`

## Running as as a jar
It is possible to build the module and run it as a stand alone jar.

Running the command `grails war` will create a runnable jar file. However, by default this will use the production profile which does not have self registration enabled.
You can run the jar using the script `scripts/run_self_reg.sh`.

Alternatively, you can run the production jar with the following command:

    java -jar build/libs/olf-erm-1.0.jar \
     --grails.server.host=10.0.2.2 \
     --okapi.service.host=localhost \
     --okapi.service.port=9130 \
     --db.username=folio_admin \
     --db.password=folio_admin \
     --db.database=okapi_modules \
     --db.port=54321

The properties that start with "db." align with the folio conventions of allowing for the environment variable `DB_USERNAME`, `BD_PASSWORD` etc.
Setting these environment variables should also work.

The above assumes you want to connect to the postgres instance running in the vagrant image in this repo.

### Building the jar with a different profile ###
You can build the jar using a different profile, like the one detailed above that automatically contains the database
settings for the supplied vagrant instance of postgres, you can do the following:


    grails -Dgrails.env=vagrant-db war


This will default to the settings for the "vagrant-db" profile and thus make the run command:

    java -jar build/libs/olf-erm-1.0.jar


Notice the lack of variables needed. This is because they are automatically set in the file `service/grails-app/conf/application-vagrant-db.yml

# Troubleshooting

## Connection issues when using the vagrant database
When running any `grails` cli command it will attempt to run it in the development porfile by default, which assumes postgress to be running on port 5432.
If you are using the database within the vagrant image, you'll need to supply the profile when running the `grails` command. So instead of just `grails`
it will become `grails -Dgrails.env=vagrant-db`

## Integration Tests

### DataSource not found for name [...] in configuration. Please check your multiple data sources configuration and try again.

The grails multitenant handling is idiomatically a little different to folio usage, so mod grails-okapi provides several services to mediate this difference.
However, hibernateDatastore fails to provide some methods (Most notably, the ability to deregister a tenant datasource) so integration tests find it hard to re-use tenants
in a run. Sometimes an error can case a tenant to be left in place. Dropping the test database and re-creating with

    DROP DATABASE olftest;
    CREATE DATABASE olftest;
    GRANT ALL PRIVILEGES ON DATABASE olftest to folio;

can help a lot.

## Domain Classes and Database Schemas

Schemas are controlled by the liquibase database migrations plugin. This means domain classes work sligthly differently to normal grails projects.

After adding or editing domain classes, you will need to generate a liquibase config file. The full file can be regenerated with::

    grails dbm-gorm-diff description-of-change.groovy --add
    grails dbm-generate-gorm-changelog my-new-changelog.groovy
    
_NOTE:_ If you are using the database from the vagrant image, which is on 54321 to avoid clashes with any local postgres you might have,
the above won't be able to find your database. Try:

    grails -Dgrails.env=vagrant-db dbm-gorm-diff description-of-change.groovy --add
    grails -Dgrails.env=vagrant-db dbm-generate-gorm-changelog my-new-changelog.groovy

## Bootstraping some data

There are some scripts in ~/scripts you can use to inject agreeents and packages into the system.
