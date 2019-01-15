package mod.rs


class UrlMappings {

  static mappings = {
    "/"(controller: 'application', action:'index')

    "/rs/patronrequests" (resources:'patronRequest')

    "/chas/$action" ( controller: "Chas")

    // Call /rs/refdata to list all refdata categories
    '/rs/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }

    // Call /rs/custprop  to list all custom properties
    '/rs/custprops'(resources: 'customPropertyDefinition')


    "500"(view: '/error')
    "404"(view: '/notFound')
  }
}
