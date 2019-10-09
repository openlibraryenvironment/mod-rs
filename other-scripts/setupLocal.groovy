:load ./modrsCli.groovy
okapi=new OkapiClient('local')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()

okapi.addTenantSymbol('OCLC:ZMU');
okapi.addTenantSymbol('OCLC:PPU');
okapi.addTenantSymbol('OCLC:PPPA');
okapi.addTenantSymbol('OCLC:AVL');
okapi.addTenantSymbol('RESHARE:LOCALSYMBOL01');
okapi.addTenantSymbol('RESHARE:KNOWINT01');
okapi.addTenantSymbol('RESHARE:DIKU')
okapi.addTenantSymbol('RESHARE:DIKUA')
okapi.addTenantSymbol('RESHARE:DIKUB')
okapi.addTenantSymbol('RESHARE:DIKUC')
okapi.addTenantSymbol('RESHARE:KINT')
okapi.addTenantSymbol('RESHARE:TESTINST01')
okapi.addTenantSymbol('RESHARE:TESTINST02')
okapi.addTenantSymbol('RESHARE:TESTINST03')
okapi.addTenantSymbol('RESHARE:TESTINST04')
okapi.addTenantSymbol('RESHARE:TESTINST05')
okapi.addTenantSymbol('RESHARE:TESTINST06')
okapi.addTenantSymbol('RESHARE:TESTINST07')
okapi.addTenantSymbol('RESHARE:TESTINST08')
okapi.addTenantSymbol('RESHARE:TESTINST09')
okapi.addTenantSymbol('RESHARE:TESTINST10')
okapi.addTenantSymbol('RESHARE:KNOWINT01')
okapi.addTenantSymbol('RESHARE:IDVUFIND')


okapi.listTenantSymbols()

okapi.createRequest([title:'The Heart of Enterprise',requestingInstitutionSymbol:'RESHARE:KNOWINT01']);
