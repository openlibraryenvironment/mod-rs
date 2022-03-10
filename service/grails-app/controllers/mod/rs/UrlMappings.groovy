package mod.rs


class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')
    "/rs/statistics" (controller: 'statistics' )

    "/rs/externalApi/statistics" (controller: 'externalApi', action:'statistics' )
    "/rs/externalApi/iso18626" (controller: 'externalApi', action:'iso18626' )
    "/rs/externalApi/statusReport" (controller: 'externalApi', action:'statusReport' )

    "/rs/patronrequests" (resources:'patronRequest') {
      '/validActions' (controller: 'patronRequest', action: 'validActions')
      '/performAction'  (controller: 'patronRequest', action: 'performAction')
    }

    "/rs/availableAction/fromStates/$stateModel/$actionCode" (controller: "availableAction", action: "fromStates")
    "/rs/availableAction/toStates/$stateModel/$actionCode" (controller: "availableAction", action: "toStates")
    "/rs/availableAction/createGraph/$stateModel" (controller: "availableAction", action: "createGraph")
	
    '/rs/noticePolicies' (resources: 'noticePolicy')

    "/rs/shipments" (resources: 'shipment' )
    "/rs/timers" (resources: 'timer' )
    "/rs/hostLMSLocations" (resources: 'hostLMSLocation' )
    "/rs/shelvingLocations" (resources: 'shelvingLocations' )
    "/rs/sharedIndexQuery" (controller: 'sharedIndexQuery', action: 'query', parseRequest: false)
    "/rs/directoryEntry" (resources: 'directoryEntry' )

    // Call /rs/refdata to list all refdata categories
    '/rs/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }
    
    '/rs/status'(resources: 'status', excludes: ['update', 'patch', 'save', 'create', 'edit', 'delete'])

    "/rs/kiwt/config/$extended?" (controller: 'reshareConfig' , action: "resources")
    "/rs/kiwt/config/schema/$type" (controller: 'reshareConfig' , action: "schema")
    "/rs/kiwt/config/schema/embedded/$type" (controller: 'reshareConfig' , action: "schemaEmbedded")
    "/rs/kiwt/raml" (controller: 'reshareConfig' , action: "raml")

    "/rs/settings/tenantSymbols" (controller: 'reshareSettings', action: 'tenantSymbols');
    "/rs/settings/worker" (controller: 'reshareSettings', action: 'worker');
    "/rs/settings/appSettings" (resources: 'setting');

    "/rs/iso18626" (controller: 'iso18626', action: 'index');


     // Call /rs/custprop  to list all custom properties
    '/rs/custprops'(resources: 'customPropertyDefinition')

    '/rs/iso18626'(controller:'iso18626', action:'index')
    "/rs/status/$symbol"(controller:'iso18626', action:'status')

    "500"(view: '/error')
    "404"(view: '/notFound')

    '/rs/template'(resources: 'template')
  }
}
