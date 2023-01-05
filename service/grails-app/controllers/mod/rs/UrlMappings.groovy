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
      '/manualCloseStates'  (controller: 'patronRequest', action: 'manualCloseStates')
    }

    "/rs/patronrequests/openURL" (controller: "patronRequest", action: "openURL")

    "/rs/patron/$patronIdentifier/canCreateRequest" (controller: 'patron', action: 'canCreateRequest')

    "/rs/availableAction/fromStates/$stateModel/$actionCode" (controller: "availableAction", action: "fromStates")
    "/rs/availableAction/toStates/$stateModel/$actionCode" (controller: "availableAction", action: "toStates")
    "/rs/availableAction/createGraph/$stateModel" (controller: "availableAction", action: "createGraph")

    "/rs/report/createUpdate" (controller: "report", action: "createUpdate")
    "/rs/report/execute" (controller: "report", action: "execute")
    "/rs/report/generatePicklist" (controller: "report", action: "generatePicklist")

    "/rs/fileDefinition/testFileUpload" (controller: "fileDefinition", action: "testFileUpload")
    "/rs/fileDefinition/testFileDownload/$fileId" (controller: "fileDefinition", action: "testFileDownload")

    "/rs/stateModel/export" (controller: "stateModel", action: "export")
    "/rs/stateModel/import" (controller: "stateModel", action: "ingest")

    '/rs/noticePolicies' (resources: 'noticePolicy')

    "/rs/shipments" (resources: 'shipment' )
    "/rs/timers" (resources: 'timer' )
    "/rs/hostLMSLocations" (resources: 'hostLMSLocation' )
    "/rs/hostLMSPatronProfiles" (resources: 'hostLMSPatronProfile' )
    "/rs/hostLMSItemLoanPolicies" (resources: 'hostLMSItemLoanPolicy' )
    "/rs/shelvingLocations" (resources: 'shelvingLocations' )
    "/rs/shelvingLocationSites" (resources: 'shelvingLocationSite')
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
    "/rs/settings/appSettings" (resources: 'setting')

    "/rs/iso18626" (controller: 'iso18626', action: 'index');


     // Call /rs/custprop  to list all custom properties
    '/rs/custprops'(resources: 'customPropertyDefinition')

    '/rs/iso18626'(controller:'iso18626', action:'index')
    "/rs/status/$symbol"(controller:'iso18626', action:'status')

    // Swagger document
    // This one returns what the plugin generates
//    "/rs/swagger/api"(controller: "swagger", action: "api")
    // But as it returns controllers we do not want to expose in this way, we then manipulate it to remove them
    "/rs/swagger/api"(controller: "swaggerUI", action: "api")

    // The swagger plugin dosn't supply the UI, so
    "/rs/swaggerUI"(controller: "swaggerUI", action: "index")

    "500"(view: '/error')
    "404"(view: '/notFound')

    '/rs/template'(resources: 'template')
  }
}
