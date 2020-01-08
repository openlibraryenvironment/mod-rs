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
  okapi.addTenantSymbol('RESHARE:MILL')
  okapi.addTenantSymbol('RESHARE:VILL')
  okapi.addTenantSymbol('RESHARE:VLA')
  okapi.addTenantSymbol('RESHARE:MVS')
  okapi.addTenantSymbol('RESHARE:TEU')
  okapi.listTenantSymbols()
}

if (initial_setup) {

  println("Running initial setup");

  okapi.createRequest([title:'The Heart of Enterprise',requestingInstitutionSymbol:'RESHARE:KNOWINT01']);

  okapi.createRequest([
                       title:'The darkening land',
                       patronIdentifier: '905808497',
                       patronReference: 'PR',
                       patronSurname: 'PS',
                       patronGivenName: 'PGN',
                       systemInstanceIdentifier:'3246e0db-6d41-442b-ae61-27f1d607a8dc', 
                       requestingInstitutionSymbol:'RESHARE:KNOWINT01',
                       rota:[
                         [ directoryId:'RESHARE:TESTINST01', rotaPosition:'0' ]
                       ]]);

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

  // 3246e0db-6d41-442b-ae61-27f1d607a8dc | The darkening land
  // 593bca64-d19e-4d0d-95e0-219d0c36a3d1 | Medieval households
  // ecd5e043-c3a8-4eab-a9ae-7b9453d1bd57 | Astonishment and power
  // 3ce768c6-a3ef-40ac-8c69-d98aed2f50a5 | Poetry and prayer
  // 8c34033e-715d-4d76-8576-0bebd21b174c | English romantic poets
  // 4e6441a9-5d77-4cb5-a244-3d5a6d65d0ca | Etruscan Italy
  // c002cadb-6d3d-4bcc-8d29-6d76d0848e9f | The falcons of the world
  // be137ebc-c2de-4677-8c0e-f5867041b54b | Drug dependence
  // dd877111-0e99-41c3-999e-e336b96ada80 | Four poets on poetry
  // cb173644-b00a-46ec-93db-eaba47896ed1 | The Oxford dictionary of Popes


  // okapi.createRequest([
  //                      title:'10,000 Teachers, 10 Million Minds Science and Math Scholarship Act : report (to accompany H.R. 362) (including cost estimate of the Congressional Budget Office)', 
  //                      patronIdentifier: 'PI',
  //                      patronReference: 'PR',
  //                      patronSurname: 'PS',
  //                      patronGivenName: 'PGN',
  //                      patronType:'PT',
  //                      systemInstanceIdentifier:'491fe34f-ea1b-4338-ad20-30b8065a7b46',
  //                      requestingInstitutionSymbol:'OCLC:ZMU']);
  // 905808497 - is a valid patron ID for a user at Temple
  okapi.createRequest([
                       title:'Arthur Koestler : a collection of critical essays', 
                       patronIdentifier: '905808497',
                       patronReference: 'PR',
                       patronSurname: 'PS',
                       patronGivenName: 'PGN',
                       patronType:'PT',
                       patronEmail:'patron@institution',
                       patronNote:'Please dont shoot the messenger',
                       pickupLocation:'A string',
                       systemInstanceIdentifier:'08ef9430-878d-42c4-a9fa-f09951f36803',
                       requestingInstitutionSymbol:'OCLC:ZMU',
                       rota:[
                         [ directoryId:'RESHARE:TESTINST01', rotaPosition:'0' ]
                       ]]);
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
