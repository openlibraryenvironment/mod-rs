
class UrlMappings {

  static mappings = {

    "/"(controller: 'application', action:'index')
    "/_/$action" (controller: 'okapi', action:'tenant')

    "/rs/patronrequests"(resources:'patronRequest')

    "/Chas/$action" ( controller: "Chas")
  }
}
