databaseChangeLog = {
  changeSet(author: "jskomorowski (manual)", id: "202010302233-001") {
   addPrimaryKey(columnNames: "st_id", constraintName: "app_settingPK", tableName: "app_setting")
   addPrimaryKey(columnNames: "aa_id", constraintName: "available_actionPK", tableName: "available_action")
   addPrimaryKey(columnNames: "ct_id", constraintName: "counterPK", tableName: "counter")
   addPrimaryKey(columnNames: "hll_id", constraintName: "host_lms_locationPK", tableName: "host_lms_location")
   addPrimaryKey(columnNames: "np_id", constraintName: "notice_policyPK", tableName: "notice_policy")
   addPrimaryKey(columnNames: "npn_id", constraintName: "notice_policy_noticePK", tableName: "notice_policy_notice")
   addPrimaryKey(columnNames: "pat_id", constraintName: "patronPK", tableName: "patron")
   addPrimaryKey(columnNames: "prlc_id", constraintName: "patron_request_loan_conditionPK", tableName: "patron_request_loan_condition")
   addPrimaryKey(columnNames: "prn_id", constraintName: "patron_request_notificationPK", tableName: "patron_request_notification")
   addPrimaryKey(columnNames: "str_id", constraintName: "state_transitionPK", tableName: "state_transition")
   addPrimaryKey(columnNames: "id", constraintName: "tenant_changelogPK", tableName: "tenant_changelog")
   addPrimaryKey(columnNames: "tr_id", constraintName: "timerPK", tableName: "timer")
   addPrimaryKey(columnNames: "address_tags_id, tag_id", constraintName: "address_tagPK", tableName: "address_tag")
   addPrimaryKey(columnNames: "directory_entry_tags_id, tag_id", constraintName: "directory_entry_tagPK", tableName: "directory_entry_tag")
   addPrimaryKey(columnNames: "service_tags_id, tag_id", constraintName: "service_tagPK", tableName: "service_tag")
   addPrimaryKey(columnNames: "patron_request_tags_id, tag_id", constraintName: "patron_request_tagPK", tableName: "patron_request_tag")
   addPrimaryKey(columnNames: "pr_previous_states, previous_states_idx", constraintName: "patron_request_previous_statesPK", tableName: "patron_request_previous_states")
  }
}
