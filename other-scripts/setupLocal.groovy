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
  okapi.addTenantSymbol('RESHARE:TEMPLEI')
  okapi.listTenantSymbols()
}

if (initial_setup) {
  okapi.createRequest([title:'The Heart of Enterprise',requestingInstitutionSymbol:'RESHARE:KNOWINT01']);
}

if ( !initial_setup) {
  // okapi.createRequest([
  //                      title:'Temeraire', 
  //                      patronIdentifier: 'PI',
  //                      patronReference: 'PR',
  //                      patronSurname: 'PS',
  //                      patronGivenName: 'PGN',
  //                      patronType:'PT',
  //                      systemInstanceIdentifier:'8a6d65a3-709c-4ade-9ffa-043fb031fedd',
  //                      requestingInstitutionSymbol:'OCLC:ZMU']);

  okapi.createRequest([
                       title:'10,000 Teachers, 10 Million Minds Science and Math Scholarship Act : report (to accompany H.R. 362) (including cost estimate of the Congressional Budget Office)', 
                       patronIdentifier: 'PI',
                       patronReference: 'PR',
                       patronSurname: 'PS',
                       patronGivenName: 'PGN',
                       patronType:'PT',
                       systemInstanceIdentifier:'491fe34f-ea1b-4338-ad20-30b8065a7b46',
                       requestingInstitutionSymbol:'OCLC:ZMU']);
}

printf('%-2s %-36s %-30s %-9s %-20s\n', '#', 'id', 'title', 'role', 'Current State');
i=0;
lr = okapi.listRequests()
lr.results.each { pr ->
  printf('%-2d %-36s %-20s %-30s %-9s %-20s\n', i++, pr.id, pr.hrid, pr.title, ( pr.isRequester ? 'Requester' : 'Responder' ), pr.state.code);
  printf("    -> ${pr.validActions}\n");
  // printf("    -> ${pr}");
  // printf("    -> ${okapi.validActions(pr.id)}\n");
}

// okapi.actionPrintedPullSlip('');

return 'OK'
