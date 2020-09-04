package org.olf.rs

import grails.gorm.MultiTenant

class NoticePolicyNotice implements MultiTenant<NoticePolicy> {

  String id
  String template
  Boolean realTime
  // TBD how to store format and trigger options

  Date dateCreated
  Date lastUpdated

  static belongsTo = [noticePolicy: NoticePolicy];

  static constraints = {
    template (nullable: false)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }

  static mapping = {
    id column : 'npn_id', generator: 'uuid2', length:36
    version column : 'npn_version'
    dateCreated column : 'npn_date_created'
    lastUpdated column : 'npn_last_updated'
    noticePolicy column : 'npn_notice_policy_fk'
    template column : 'npn_template'
    realTime column : 'npn_real_time'
  }
}
