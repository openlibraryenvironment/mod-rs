:load ./modrsCli.groovy
okapi=new OkapiClient('reshare')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()

okapi.createRequest([
                     title:'The Heart of Enterprise',
                     patronIdentifier: 'PI',
                     patronReference: 'PR',
                     patronSurname: 'PS',
                     patronGivenName: 'PGN',
                     patronType:'PT',
                     requestingInstitutionSymbol:'RESHARE:KNOWINT']);

//okapi.createRequest([title:'The Heart of Enterprise', requestingInstitutionSymbol:'OCLC:AVL']);
okapi.createRequest([title:'Temeraire', requestingInstitutionSymbol:'OCLC:ZMU']);


