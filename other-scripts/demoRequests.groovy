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
                     pickupLocation: 'Surely this is nullable?',
                     patronType:'PT',
                     requestingInstitutionSymbol:'RESHARE:KNOWINT01',
                     rota:[
                         [ directoryId:'RESHARE:KNOWINT01', rotaPosition:'0' ]
                     ] ]);
