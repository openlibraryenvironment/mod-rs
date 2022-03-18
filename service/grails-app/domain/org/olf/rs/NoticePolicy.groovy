package org.olf.rs

import grails.gorm.MultiTenant

class NoticePolicy implements MultiTenant<NoticePolicy> {

  String id
  String name
  String description
  Boolean active

  Date dateCreated
  Date lastUpdated

  /** If it is a predefined id this is the id allocated to it */
  String predefinedId;

  static hasMany = [notices: NoticePolicyNotice];

  static constraints = {
    description (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
    predefinedId (nullable: true)
  }

  static mapping = {
    id column : 'np_id', generator: 'uuid2', length:36
    version column : 'np_version'
    dateCreated column : 'np_date_created'
    lastUpdated column : 'np_last_updated'
    name column : 'np_name'
    description column : 'np_description'
    active column : 'np_active'
    predefinedId column: 'np_predefined_id', length: 64
    notices cascade : 'all-delete-orphan'
  }
}
