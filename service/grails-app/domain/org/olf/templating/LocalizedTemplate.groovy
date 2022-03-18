package org.olf.templating
import grails.gorm.MultiTenant

class LocalizedTemplate implements MultiTenant<LocalizedTemplate> {
  String id;
  String locality;
  Template template;

  /** If it is a predefined id this is the id allocated to it */
  String predefinedId;


  static belongsTo = [owner: TemplateContainer];

  static constraints = {
    predefinedId (nullable: true)
  }

  static mapping = {
                id column: 'ltm_id', generator: 'uuid2', length:36
           version column: 'ltm_version'
          locality column: 'ltm_locality'
             owner column: 'ltm_owner_fk'
          template column: 'ltm_template_fk'
      predefinedId column: 'ltm_predefined_id', length: 64
  }
}
