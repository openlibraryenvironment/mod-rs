package org.olf.rs

import grails.gorm.MultiTenant

class NoticePolicy implements MultiTenant<NoticePolicy> {

  String id
  String name
  String description
  Boolean active

  Date dateCreated
  Date lastUpdated

  static hasMany = [notices: NoticePolicyNotice];

  static constraints = {
    description (nullable: true)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }

  static mapping = {
    id column : 'np_id', generator: 'uuid2', length:36
    version column : 'np_version'
    dateCreated column : 'np_date_created'
    lastUpdated column : 'np_last_updated'
    name column : 'np_name'
    description column : 'np_description'
    active column : 'np_active'
    notices cascade : 'all-delete-orphan'
  }
}
