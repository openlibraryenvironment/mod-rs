package org.olf.templating
import grails.gorm.MultiTenant

class LocalizedTemplate implements MultiTenant<LocalizedTemplate> {
  String id
  String locality
  Template template

  static belongsTo = [owner: TemplateContainer];

  static mapping = {
            id column: 'ltm_id', generator: 'uuid2', length:36
       version column: 'ltm_version'
      locality column: 'ltm_locality'
         owner column: 'ltm_owner_fk'
      template column: 'ltm_template_fk'
  }

}