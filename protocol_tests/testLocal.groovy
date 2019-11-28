:set verbosity QUIET
:load ./Iso18626Client.groovy

Iso18626Client c = new Iso18626Client();

c.sendNewRequest(supplier:'RESHARE:TEMPLEI', requester:'RESHARE:MILL', title:'Brain of the firm');

