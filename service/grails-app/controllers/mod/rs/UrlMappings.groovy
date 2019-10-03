package mod.rs


class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')

    "/rs/patronrequests" (resources:'patronRequest') {
      '/validActions' (controller: 'patronRequest', action: 'validActions')
    }

    "/rs/shipments" (resources: 'shipment' )
    "/rs/settings" (resources: 'setting')

    "/rs/directoryEntry" (resources: 'directoryEntry' )

    // Call /rs/refdata to list all refdata categories
    '/rs/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }

    "/rs/kiwt/config/$extended?" (controller: 'reshareConfig' , action: "resources")
    "/rs/kiwt/config/schema/$type" (controller: 'reshareConfig' , action: "schema")
    "/rs/kiwt/config/schema/embedded/$type" (controller: 'reshareConfig' , action: "schemaEmbedded")
    "/rs/kiwt/raml" (controller: 'reshareConfig' , action: "raml")

    "/rs/settings/tenantSymbols" (controller: 'reshareSettings', action: 'tenantSymbols');
    "/rs/settings/worker" (controller: 'reshareSettings', action: 'worker');


    // Call /rs/custprop  to list all custom properties
    '/rs/custprops'(resources: 'customPropertyDefinition')

    "500"(view: '/error')
    "404"(view: '/notFound')
  }
}
