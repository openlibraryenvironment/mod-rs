:load ./modrsCli.groovy
okapi=new OkapiClient('temple')
rsclient = new RSClient(okapi);

okapi.addTenantSymbol('RESHARE:TEU');
