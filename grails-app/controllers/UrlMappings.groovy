package org.olf.licenses

class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')
    "/_/$action" (controller: 'okapi', action:'tenant')

    "/licenses/licenses"(resources:'license')

    '/licenses/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup')
      }
    }

    '/licenses/custprops'(resources: 'customPropertyDefinition')

	"/Chas/$action" ( controller: "Chas")
  }
}
