package org.olf.templating
import com.k_int.web.toolkit.refdata.Defaults
import com.k_int.web.toolkit.refdata.RefdataValue

import grails.gorm.MultiTenant

class TemplateContainer implements MultiTenant<TemplateContainer> {
  String id;
  String name;

  @Defaults(['Handlebars'])
  RefdataValue templateResolver;

  String description;

  Date dateCreated;
  Date lastUpdated;

  String context;

  /** If it is a predefined id this is the id allocated to it */
  String predefinedId;

  static hasMany = [localizedTemplates: LocalizedTemplate];

  static constraints = {
    predefinedId (nullable: true)
  }

  static mapping = {
                     id column: 'tmc_id', generator: 'uuid2', length:36
                version column: 'tmc_version'
                   name column: 'tmc_name'
       templateResolver column: 'tmc_template_resolver'
            description column: 'tmc_description'
            dateCreated column: 'tmc_date_created'
            lastUpdated column: 'tmc_last_updated'
                context column: 'tmc_context'
           predefinedId column: 'tmc_predefined_id', length: 64
    localizedTemplates cascade: 'all-delete-orphan'
  }
}
