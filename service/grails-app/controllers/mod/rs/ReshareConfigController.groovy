package mod.rs;

import com.k_int.web.toolkit.ConfigController;

import grails.converters.JSON

public class ReshareConfigController extends ConfigController {

  def raml() {
    def response = [
      'one':'two'
    ]

    render response as JSON
  }

  
}
