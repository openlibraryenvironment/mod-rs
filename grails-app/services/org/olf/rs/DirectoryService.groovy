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
    // This helper needs to talk to mod-directory, and in particular, needs to return the results of
    // calling
    // /directory/entry?filters=symbols.symbol%3dDIKUA&filters=symbols.authority.value=RESHARE&stats=true
    // That call returns an array - if one row is returned, this method should return the object so that the ID can be extracted (Or
    // whatever other properties are needed). If multiple rows, an exception, if no rows, null.
    //
    // Asking steve for advice as there is no incoming OKAPI request object here so authentication and authorization are problems
    //
    return getDirectoryEntryForSymbol(authority,symbol)?.id;
  }

  Map getDirectoryEntryForSymbol(String authority, String symbol) {
    // This helper needs to talk to mod-directory, and in particular, needs to return the results of
    // calling
    // /directory/entry?filters=symbols.symbol%3dDIKUA&filters=symbols.authority.value=RESHARE&stats=true
    // That call returns an array - if one row is returned, this method should return the object so that the ID can be extracted (Or
    // whatever other properties are needed). If multiple rows, an exception, if no rows, null.
    //
    // Asking steve for advice as there is no incoming OKAPI request object here so authentication and authorization are problems
    //

    return [
      id:'1234'
    ]
  }

}
