:load ./modrsCli.groovy
okapi=new OkapiClient('millersville')
rsclient = new RSClient(okapi);

okapi.addTenantSymbol('RESHARE:MILL');
okapi.addTenantSymbol('RESHARE:MVS');
