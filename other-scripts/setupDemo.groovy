:load ./modrsCli.groovy
okapi=new OkapiClient('reshare')
rsclient = new RSClient(okapi);

if ( 1==2 ) {
  okapi.addTenantSymbol('OCLC:ZMU');
  okapi.addTenantSymbol('OCLC:PPU');
  okapi.addTenantSymbol('RESHARE:LOCALSYMBOL01');
  okapi.addTenantSymbol('RESHARE:KNOWINT');
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
  okapi.addTenantSymbol('RESHARE:IDVUFIND')
}

if ( 1==1 ) {
  //okapi.walkFoafGraph()
  okapi.listTenantSymbols()
}

if ( 1==1 ) {
  // okapi.createRequest([
  //                      title:'The Heart of Enterprise',
  //                      patronIdentifier: 'PI',
  //                      patronReference: 'PR',
  //                      patronSurname: 'PS',
  //                      patronGivenName: 'PGN',
  //                      patronType:'PT',
  //                      requestingInstitutionSymbol:'RESHARE:KNOWINT']);

  //okapi.createRequest([title:'The Heart of Enterprise', requestingInstitutionSymbol:'OCLC:AVL']);
  okapi.createRequest([
                       title:'Temeraire',
                       patronIdentifier: 'PI',
                       patronReference: 'PR',
                       patronSurname: 'PS',
                       patronGivenName: 'PGN',
                       patronType:'PT',
                       patronEmail:'patron@institution',
                       patronNote:'Please dont shoot the messenger',
                       pickupLocation:'A string',
                       systemInstanceIdentifier:'8a6d65a3-709c-4ade-9ffa-043fb031fedd',
                       requestingInstitutionSymbol:'OCLC:ZMU']);
}

if ( 1== 2 ) {
  okapi.walkFoafGraph()
}

