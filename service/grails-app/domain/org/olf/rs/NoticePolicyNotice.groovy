package org.olf.rs

import grails.gorm.MultiTenant
import com.k_int.web.toolkit.refdata.RefdataValue
import org.olf.templating.TemplateContainer

class NoticePolicyNotice implements MultiTenant<NoticePolicy> {

  String id
  TemplateContainer template
  Boolean realTime
  RefdataValue format
  RefdataValue trigger

  Date dateCreated
  Date lastUpdated

  static belongsTo = [noticePolicy: NoticePolicy];

  static constraints = {
    template (nullable: false)
    format (nullable: false)
    trigger (nullable: false)
    dateCreated (nullable: true, bindable: false)
    lastUpdated (nullable: true, bindable: false)
  }

  static mapping = {
    id column : 'npn_id', generator: 'uuid2', length:36
    version column : 'npn_version'
    dateCreated column : 'npn_date_created'
    lastUpdated column : 'npn_last_updated'
    noticePolicy column : 'npn_notice_policy_fk'
    template column : 'npn_template_fk'
    realTime column : 'npn_real_time'
    format column : 'npn_format_fk'
    trigger column : 'npn_trigger_fk'
  }
}
