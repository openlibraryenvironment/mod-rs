package org.olf.rs

import grails.gorm.MultiTenant;

class Patron implements MultiTenant<Patron> {

  String id
  String hostSystemIdentifier
  String givenname
  String surname
  Date dateCreated
  Date lastUpdated
  
  static constraints = {
    hostSystemIdentifier  (nullable : false, blank: false, unique: true)
    givenname             (nullable : true,  blank: false)
    surname               (nullable : true,  blank: false)
  }

  static mapping = {
    id                     column : 'pat_id', generator: 'uuid2', length:36
    version                column : 'pat_version'
    dateCreated            column : 'pat_date_created'
    lastUpdated            column : 'pat_last_updated'
    hostSystemIdentifier   column : 'pat_host_system_identifier'
    givenname              column : 'pat_given_name'
    surname                column : 'pat_surame'
  }
}
