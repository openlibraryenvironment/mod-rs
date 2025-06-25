package mod.rs


class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')
    "/rs/statistics" (controller: 'statistics' )

    "/rs/localEntries" (controller: 'localEntry')

    "/rs/externalApi/statistics" (controller: 'externalApi', action:'statistics' )
    "/rs/externalApi/iso18626" (controller: 'externalApi', action:'iso18626' )
    "/rs/externalApi/statusReport" (controller: 'externalApi', action:'statusReport' )

    "/rs/patronrequests" (resources:'patronRequest') {
      '/validActions' (controller: 'patronRequest', action: 'validActions')
      '/performAction'  (controller: 'patronRequest', action: 'performAction')
      '/manualCloseStates'  (controller: 'patronRequest', action: 'manualCloseStates')
    }
    "/rs/patronrequests/bulkAction" (controller: "patronRequest", action: "bulkAction")
    "/rs/patronrequests/generatePickListBatch" (controller: "patronRequest", action: "generatePickListBatch")
    "/rs/patronrequests/markBatchAsPrinted" (controller: "patronRequest", action: "markBatchAsPrinted")
    "/rs/patronrequests/openURL" (controller: "patronRequest", action: "openURL")
    "/rs/patronrequests/editableFields/$op" (controller: "patronRequest", action: "editableFields")

    "/rs/patron/$patronIdentifier/canCreateRequest" (controller: 'patron', action: 'canCreateRequest')
    "/rs/patron/validate" (controller: 'patron', action: 'validate')

    "/rs/availableAction/fromStates/$stateModel/$actionCode" (controller: "availableAction", action: "fromStates")
    "/rs/availableAction/toStates/$stateModel/$actionCode" (controller: "availableAction", action: "toStates")
    "/rs/availableAction/createGraph/$stateModel" (controller: "availableAction", action: "createGraph")

    "/rs/report/createUpdate" (controller: "report", action: "createUpdate")
    "/rs/report/execute" (controller: "report", action: "execute")
    "/rs/report/generatePicklist" (controller: "report", action: "generatePicklist")

    "/rs/fileDefinition/fileUpload" (controller: "fileDefinition", action: "fileUpload")
    "/rs/fileDefinition/fileDownload/$fileId" (controller: "fileDefinition", action: "fileDownload")

    "/rs/stateModel/export" (controller: "stateModel", action: "export")
    "/rs/stateModel/import" (controller: "stateModel", action: "ingest")
    "/rs/stateModel/getValidActions" (controller: "stateModel", action: "getValidActions")

    '/rs/noticePolicies' (resources: 'noticePolicy')

    "/rs/batch" (resources: 'batch' )
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
    "/rs/swaggerUI"(uri: '/static/swaggerUI/index.html')

    // The swagger resources for display purposes
    group "/rs/swagger", {
        "/swagger-ui.css"(uri: '/static/swaggerUI/4.14.0/swagger-ui.css')
        "/swagger-ui-standalone-preset.js"(uri: '/static/swaggerUI/4.14.0/swagger-ui-standalone-preset.js')
        "/swagger-initializer.js"(uri: '/static/swaggerUI/4.14.0/swagger-initializer.js')
        "/index.css"(uri: '/static/swaggerUI/4.14.0/index.css')
        "/swagger-ui-bundle.js"(uri: '/static/swaggerUI/4.14.0/swagger-ui-bundle.js')
        "/favicon-32x32.png"(uri: '/static/swaggerUI/4.14.0/favicon-32x32.png')
        "/favicon-16x16.png"(uri: '/static/swaggerUI/4.14.0/favicon-16x16.png')
    }

    // For dynamically changing the logging level
    '/rs/logging'(controller:'logging', action:'index')

    // For testing the host lms
    '/rs/testHostLMS/acceptItem'(controller: 'testHostLMS', action: 'acceptItem')
    '/rs/testHostLMS/checkIn'(controller: 'testHostLMS', action: 'checkIn')
    '/rs/testHostLMS/checkOut'(controller: 'testHostLMS', action: 'checkOut')
    '/rs/testHostLMS/determineBestLocation'(controller: 'testHostLMS', action: 'determineBestLocation')
    '/rs/testHostLMS/validate'(controller: 'testHostLMS', action: 'validate')

    "500"(view: '/error')
    "404"(view: '/notFound')

    '/rs/template'(resources: 'template')
  }
}
