:set verbosity QUIET
:load ./modrsCli.groovy
okapi=new OkapiClient('local')
rsclient = new RSClient(okapi);

println('to force a walk of the FOAF graph, call okapi.walkFoafGraph()');

symbols_resp = okapi.listTenantSymbols()
println symbols_resp
initial_setup = false;

if ( !symbols_resp.symbols.contains('OCLC:ZMU') ) {
  println('OCLC:ZMU not found in list of registered symbols... do setup')
  initial_setup = true
}

if (initial_setup) {
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
}

if (initial_setup) {
  okapi.createRequest([title:'The Heart of Enterprise',requestingInstitutionSymbol:'RESHARE:KNOWINT01']);
  okapi.createRequest([title:'Temeraire', requestingInstitutionSymbol:'OCLC:ZMU']);
}


printf('%-2s %-36s %-30s %-5s\n', '#', 'id', 'title', 'isReq');
i=0;
lr = okapi.listRequests()
lr.results.each { pr ->
  printf('%-2d %-36s %-30s %-5b %-10s\n', i++, pr.id, pr.title, pr.isRequester, pr.state.code);
}

return 'OK'
