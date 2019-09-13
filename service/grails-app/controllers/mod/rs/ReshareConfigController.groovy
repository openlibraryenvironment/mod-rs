package mod.rs;

import com.k_int.web.toolkit.ConfigController;

import grails.converters.JSON

public class ReshareConfigController extends ConfigController {

  private static String raml_text = '''
#%RAML 1.0

title: ResourceSharing API
baseUri: https://github.com/openlibraryenvironment/mod-rs
version: v1

documentation:
  - title: mod-rs API
    content: This documents the API calls that can be made to query and manage resource sharing requests

types:
  PatronRequest: !include kiwt/config/schema/PatronRequest
  Status: !include kiwt/config/schema/Status
  RefdataValue: !include kiwt/config/schema/RefdataValue
  RefdataCategory: !include kiwt/config/schema/RefdataCategory
  StateModel: !include kiwt/config/schema/StateModel
  Shipment: !include kiwt/config/schema/Shipment
  ShipmentItem: !include kiwt/config/schema/ShipmentItem

traits:
  okapiService:
    headers:
      X-Okapi-Tenant:
        description: Okapi Tenant Id
      X-Okapi-Token:
        description: Okapi JWT

/rs:
  /patronrequests:
    get:
      is: [ okapiService ]
      description: List current patron requests
      responses:
        200:
          description: "OK"
    post:
      is: [ okapiService ]
      description: Submit a new patron request
      body:
        application/json:
          type: PatronRequest
    /{requestId}:
      get:
        is: [ okapiService ]
        description: Retrieve a specific patron request
      post:
        description: Update a specific patron request
  /refdata:
    is: [ okapiService ]
    description: List all refdata categories currently known
  /settings:
    /tenantSymbols
      get:
        is: [ okapiService ]
        description: Retrieve the library symbols registered for this tenant
      post:
        description: Register a symbol as "Belonging" to this tenant.
  /shipments:
    get:
      is: [ okapiService ]
      description: List current shipments
    post:
      is: [ okapiService ]
      description: Submit new or updated shipment - post without an ID to create new, with ID to update/patch
      body:
        application/json:
          type: Shipment
'''

  def raml() {
    // yaml can be application/x-yaml or 
    render ( text: raml_text )
  }

  
}
