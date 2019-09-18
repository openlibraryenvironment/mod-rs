:load ./modrsCli.groovy
okapi=new OkapiClient('reshare')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()

// okapi.addTenantSymbol('OCLC:ZMU');
// okapi.addTenantSymbol('OCLC:PPU');
// okapi.addTenantSymbol('RESHARE:LOCALSYMBOL01');
// okapi.addTenantSymbol('RESHARE:KNOWINT');

okapi.listTenantSymbols()

okapi.createRequest([
                     title:'The Heart of Enterprise',
                     patronIdentifier: "PI",
                     patronReference: "PR",
                     patronSurname: "PS",
                     patronGivenName: 'PGN",
                     patronType:'PT',
                     requestingInstitutionSymbol:'RESHARE:KNOWINT']);

//okapi.createRequest([title:'The Heart of Enterprise', requestingInstitutionSymbol:'OCLC:AVL']);
