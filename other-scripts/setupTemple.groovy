:load ./modrsCli.groovy
okapi=new OkapiClient('temple')
rsclient = new RSClient(okapi);
okapi.listTenantSymbols()
okapi.walkFoafGraph()


// okapi.addTenantSymbol('RESHARE:TEU');
