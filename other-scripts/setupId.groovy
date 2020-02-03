:load ./modrsCli.groovy
okapi=new OkapiClient('idreshare')
rsclient = new RSClient(okapi);

if ( 1==1 ) {
  okapi.addTenantSymbol('RESHARE:VLA')
  okapi.addTenantSymbol('RESHARE:MVS')
}

if ( 1==1 ) {
  okapi.walkFoafGraph()
  println("Symbols....");
  okapi.listTenantSymbols().each {  it ->
    println(it.toString());
  }
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
                       patronIdentifier: '905808497',
                       patronReference: 'PR',
                       systemInstanceIdentifier:'8a6d65a3-709c-4ade-9ffa-043fb031fedd',
                       requestingInstitutionSymbol:'RESHARE:VLA']);

  okapi.createRequest([
                       title:'The darkening land',
                       patronIdentifier: '905808497',
                       systemInstanceIdentifier:'3246e0db-6d41-442b-ae61-27f1d607a8dc',
                       requestingInstitutionSymbol:'RESHARE:VLA']);

}
