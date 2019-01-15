package mod.rs


class UrlMappings {

  static mappings = {
    "/"(controller: 'application', action:'index')

    "/rs/patronrequests" (resources:'patronRequest')

    "/chas/$action" ( controller: "Chas")

    "500"(view: '/error')
    "404"(view: '/notFound')
  }
}
