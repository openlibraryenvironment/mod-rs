// :set verbosity QUIET

import groovy.grape.Grape
Grape.addResolver(name:'mvnRepository', root:'http://central.maven.org/maven2/')
Grape.addResolver(name:'kint', root:'http://nexus.k-int.com/content/repositories/releases')
Grape.grab(group:'io.github.http-builder-ng', module:'http-builder-ng-core', version:'1.0.3')

:load ./Iso18626Client.groovy
// see http://biblstandard.dk/ill/dk/docs/Danish_ISO_18626_profile_for_ILL_transactions.htm

println("Make client");
Iso18626Client c = new Iso18626Client();

Map req_data = [service:'http://localhost:8081/rs/iso18626',
                supplier:'RESHARE:TEMPLEI',
                requester:'RESHARE:MILL',
                title:'Brain of the firm']

println("Dump Request");
c.dumpRequest(req_data);
println("Send Request");
c.sendNewRequest(req_data)
println("Done");
