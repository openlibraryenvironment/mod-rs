package org.olf.rs
import grails.gorm.MultiTenant

import org.olf.rs.Template

import com.k_int.web.toolkit.refdata.Defaults
import com.k_int.web.toolkit.refdata.RefdataValue

class TemplateContainer implements MultiTenant<TemplateContainer> {
  String id
  String name

  @Defaults(['Handlebars'])
  RefdataValue templateResolver
  
  String description

  Date dateCreated
  Date lastUpdated

  Template template

  static mapping = {
                  id column: 'tmc_id', generator: 'uuid2', length:36
             version column: 'tmc_version'
                name column: 'tmc_name'
    templateResolver column: 'tmc_template_resolver'
         description column: 'tmc_description'
         dateCreated column: 'tmc_date_created'
         lastUpdated column: 'tmc_last_updated'
            template column: 'tmc_template_fk'
  }
}