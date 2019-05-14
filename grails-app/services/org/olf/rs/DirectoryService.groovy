package org.olf.rs

import grails.gorm.multitenancy.Tenants
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.WithoutTenant
import grails.gorm.transactions.Transactional
import org.olf.rs.workflow.Action;
import javax.sql.DataSource
import groovy.sql.Sql
import grails.core.GrailsApplication
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase
import org.olf.rs.workflow.ReShareMessageService

/**
 * This service provides java/groovy interface to mod-directory affordances.
 */
@Transactional
public class DirectoryService {

  String getIDForSymbol(String authority, String symbol) {
    return 'DS-LOOKUP-RESULT'
  }
}
