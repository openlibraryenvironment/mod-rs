:load ./modrsCli.groovy
okapi=new OkapiClient('reshare')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()

okapi.createRequest([
                     title:'Temeraire',
                     patronIdentifier: 'ian.ibbotson',
                     patronReference: 'IanTest04635',
                     patronSurname: 'Ibbotson',
                     patronGivenName: 'Ian',
                     patronType:'PT',
                     requestingInstitutionSymbol:'RESHARE:newSchool']);
