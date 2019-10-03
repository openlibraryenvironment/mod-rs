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
okapi.addTenantSymbol('RESHARE:DIKU')
okapi.addTenantSymbol('RESHARE:KINT')
okapi.addTenantSymbol('RESHARE:TestInst01')
okapi.addTenantSymbol('RESHARE:TestInst02')
okapi.addTenantSymbol('RESHARE:TestInst03')
okapi.addTenantSymbol('RESHARE:TestInst04')
okapi.addTenantSymbol('RESHARE:TestInst05')
okapi.addTenantSymbol('RESHARE:TestInst06')
okapi.addTenantSymbol('RESHARE:TestInst07')
okapi.addTenantSymbol('RESHARE:TestInst08')
okapi.addTenantSymbol('RESHARE:TestInst09')
okapi.addTenantSymbol('RESHARE:TestInst10')
okapi.addTenantSymbol('RESHARE:idVuFind')


okapi.listTenantSymbols()

okapi.createRequest([title:'The Heart of Enterprise',requestingInstitutionSymbol:'RESHARE:KNOWINT']);
