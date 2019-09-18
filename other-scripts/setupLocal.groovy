:load ./modrsCli.groovy
okapi=new OkapiClient('local')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()

okapi.addTenantSymbol('OCLC:ZMU');
okapi.addTenantSymbol('OCLC:PPU');
okapi.addTenantSymbol('OCLC:PPPA');
okapi.addTenantSymbol('OCLC:AVL');
okapi.addTenantSymbol('RESHARE:LOCALSYMBOL01');
okapi.addTenantSymbol('RESHARE:KNOWINT');
okapi.listTenantSymbols()

okapi.createRequest([title:'The Heart of Enterprise',requestingInstitutionSymbol:'RESHARE:KNOWINT']);
