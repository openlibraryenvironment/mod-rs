package mod.rs


class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')
    "/raml"(controller: 'application', action:'raml')

    "/rs/patronrequests" (resources:'patronRequest') {
      '/validActions' (controller: 'patronRequest', action: 'validActions')
    }

    // Call /rs/refdata to list all refdata categories
    '/rs/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }

    "/rs/kiwt/config/$extended?" (controller: 'config' , action: "resources")
    "/rs/kiwt/config/schema/$type" (controller: 'config' , action: "schema")
    "/rs/kiwt/config/schema/embedded/$type" (controller: 'config' , action: "schemaEmbedded")


    // Call /rs/custprop  to list all custom properties
    '/rs/custprops'(resources: 'customPropertyDefinition')

    "500"(view: '/error')
    "404"(view: '/notFound')
  }
}
