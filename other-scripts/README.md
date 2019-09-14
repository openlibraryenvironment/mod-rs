

Backend developer tools for working with mod-rs

As a backend dev, you might not have a node environment, but if you're working on mod-rs, you do have a groovy environment. 

Set up ~/.folio/credentials as an ini file with sections that describe the different OKAPI servers you want to be able to talk to. Each section supports

    [configName]
    url=https://host.of.server/path.of.server
    tenant=thetenant
    password=thepassword
    username=theusername



From this directory run

    groovysh
    :load ./modrsCli.groovy
    okapi=new OkapiClient('configName')



