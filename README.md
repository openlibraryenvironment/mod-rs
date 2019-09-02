mod-rs - Providing Resource Sharing Capabilities


# Developer Info

## Vagrant container with Kafka 

This module is developed and tested in a vagrant container projectreshare/development - see
https://app.vagrantup.com/projectreshare/boxes/development.

to update this box, make whatever changes are needed and then

Remember that when starting a vagrant image, the directory containing the Vagrantfile will be mounted as /vagrant in the started image
This will give you an easy way to make an updated .deb avaialable, for example.

As root, run 

    ./vagrant-tidy.sh

Exit from the VM and run

    vagrant package

This will create package.box - Go to https://app.vagrantup.com/projectreshare/boxes/development and create a new version with provider virtualbox then
upload the new .box image.

## Domain Classes and Database Schemas

Schemas are controlled by the liquibase database migrations plugin. This means domain classes work sligthly differently to normal grails projects.

After adding or editing domain classes, you will need to generate a liquibase config file. The full file can be regenerated with::

    grails dbm-gorm-diff description-of-change.groovy --add
    grails dbm-generate-gorm-changelog my-new-changelog.groovy

_NOTE:_ If you are using the database from the vagrant image, which is on 54321 to avoid clashes with any local postgres you might have,
the above won't be able to find your database. Try:

    grails -Dgrails.env=vagrant-db dbm-gorm-diff description-of-change.groovy --add
    grails -Dgrails.env=vagrant-db dbm-generate-gorm-changelog my-new-changelog.groovy


