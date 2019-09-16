:load ./modrsCli.groovy
okapi=new OkapiClient('kitest')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()

// okapi.addTenantSymbol('OCLC:ZMU');
// okapi.addTenantSymbol('OCLC:PPU');
// okapi.addTenantSymbol('RESHARE:LOCALSYMBOL01');
// okapi.addTenantSymbol('RESHARE:KNOWINT');
