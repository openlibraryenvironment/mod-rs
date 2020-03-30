:load ./modrsCli.groovy
okapi=new OkapiClient('reshare')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()
// okapi.walkFoafGraph()

// okapi.createRequest([
//                      title:'The Heart of Enterprise',
//                      patronIdentifier: 'PI',
//                      patronReference: 'PR',
//                      patronSurname: 'PS',
//                      patronGivenName: 'PGN',
//                      patronType:'PT',
//                      requestingInstitutionSymbol:'RESHARE:KNOWINT']);

//okapi.createRequest([title:'The Heart of Enterprise', requestingInstitutionSymbol:'OCLC:AVL']);
okapi.createRequest([title:'The Darkening Land', systemInstanceIdentifier:'3246e0db-6d41-442b-ae61-27f1d607a8dc', requestingInstitutionSymbol:'OCLC:ZMU']);


