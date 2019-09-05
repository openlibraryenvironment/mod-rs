package mod.rs

import grails.core.GrailsApplication
import grails.plugins.*
import grails.converters.JSON

class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager

    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }

  def raml() {
    def result = [ 'one':'two' ]
    render result as JSON
  }
}
